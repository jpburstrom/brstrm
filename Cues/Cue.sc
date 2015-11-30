//Trying to simplify things
//One class for all cues

//A cue is an event, that can play something and reports its current state..

Cue : Event {
    classvar <all;
    var <name, wrapper, cleanup, waitForPlay;

    *initClass {
        all = IdentityDictionary();

    }

    clone { |n|
        var new;
        n = this.class.nextName(n ? name);
        //this
        ^this.class.newFrom([n] ++ this.asKeyValuePairs)
    }

    *new { |name, func, size=8|
        if (name.isNil) { name = this.nextName };
        all[name] !? {
            if (func.notNil) { all[name].makeFunc(func) };
            ^all[name]
        };
        ^super.new(size).name_(name).init.make(func);
    }

    *newFrom { | aCollection |
        var newCollection;
        newCollection = this.new(aCollection[0], aCollection.size - 1);
        aCollection[1..].keysValuesDo({ arg k,v, i; newCollection.put(k,v) });
        ^newCollection
    }

    *nextName { |n|
        if (n.isNil) {
            n = "Untitled Cue 1"
        };
        n = n.asSymbol;
        while ( { all[n].notNil }, {
            n = PathName(n.asString.trim).nextName2.asSymbol
        });
        ^n
    }

    name_ { |n|
        if (all[n].notNil) {
            Error(n ++ " is taken. Choose another name").throw;
        };
        n = n.asSymbol;
        if (name.notNil) {
            all[name] = nil;
        };
        all[n] = this;
        name = n;
        this.changed(\name);
    }

    init {
        parent = parent ? ();
        parent.putAll((
            type: \soundcue, //Shouldn't clash with regular cue ev type
            clock: TempoClock.default,
            preWait: 0,
            postWait: 0,
            quant: 0,
            loader: {},
            play: { ~player.value; },
            player: {},
            continue:false, //FIXME: continue
            server: Server.default
        ));

        cleanup = EventStreamCleanup.new;
        //statenum = 1;
        parent.putAll((
            duration: nil,
            loader: {},
        ));
    }

    load { wrapper.load(this) }

    //Play handles logic, and makes sure prPlay is called at some point
    play { wrapper.play(this) }

    stop { |now|
        if (now) {
            wrapper.stopNow(this);
        } {
            wrapper.stop(this)
        };
    }
    pause { wrapper.pause(this); }
    resume { wrapper.resume(this); }
    reset { wrapper.reset(this); }

    state { ^wrapper.state }

    isStopped { ^wrapper.checkState(\stopped) }
    isLoading { ^wrapper.checkState(\loading) }
    isReady { ^wrapper.checkState(\ready) }
    isPlaying { ^wrapper.checkState(\playing) }
    isPaused { ^wrapper.checkState(\paused) }


     //------CLOCK-----------

    syncTo { |other|
        other !? {
            this[\clock] = { other.clock }
        }
    }

    clock {
        //Need this -- if syncTo should work we need to call .value on clock
        ^(this[\clock].value ? TempoClock.default);
    }

    prSyncload {
        this[\loader].value;
        if (waitForPlay == true) {
            waitForPlay = false;
            this.prPlay;
        }
    }

    prAsyncload {
        //Fork & Sync
        this.use {
            ~server.waitForBoot {
                ~loader.asRoutine.embedInStream;
                ~server.sync;
                this.prChangeState(\ready);
                if (waitForPlay == true) {
                    waitForPlay = false;
                    this.prPlay;
                };
                "Done loading".debug;

            }
        }
    }

    prPlay {
        this.prChangeState(\playing);
        this.use {
            this[\play].value;
        };
    }

//-----PRINT---------------
    printOn { arg stream, itemsPerLine = 5;
        stream <<< this.class << "(" <<< this.name;
        stream << ")";
    }
    storeOn { arg stream, itemsPerLine = 1;
        var max, itemsPerLinem1, i=0;
        itemsPerLinem1 = itemsPerLine - 1;
        max = this.size;
        stream <<< this.class << "[ " <<< this.name;
        stream.comma.space;
        this.keysValuesDo({ arg key, val;
            stream <<< key << " -> " <<< val;
            if ((i=i+1) < max, { stream.comma.space;
                if (i % itemsPerLine == itemsPerLinem1, { stream.nl.space.space });
            });
        });
        stream << " ]";
    }

    document { |name|
        var str;
        str = this.asCompileString;
        ^str.newTextWindow((name ? this.class.name).asString)
    }
}
