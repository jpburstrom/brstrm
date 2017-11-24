//CUE
/*
//TODO: Make pause/resume call ~pause/~resume


*/

//A cue that can play, but doesn't have a state
AbstractCue : Event {
    classvar <all;
    var <name,
    <number,
    <ev,
    <defaultParentEvent;

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
        ^super.new(size).name_(name).init.makeFunc(func);
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
        //TODO: lookup in dict
        n = n.asSymbol;
        if (name.notNil) {
            all[name] = nil;
        };
        all[n] = this;
        name = n;
        this.changed(\name);
    }

    all {
        ^all //because is classvar, but is it needed?
    }


    init {
        parent = parent ? ();
        parent.putAll((
            type: \abstractCue,
            clock: { TempoClock.default },
            play: {
                ~player.value;
            },
            player: {},
            preWait: 0,
            quant: 0,
            postWait: 0,
            continue:false
        ));

    }

    makeFunc { |func|
        this.make(func);
    }


    syncTo { |other|
        other !? {
            this[\clock] = { other.getClock }
        }
    }

    getClock {
        ^(this[\clock].value ? TempoClock.default);
    }

    printOn { arg stream, itemsPerLine = 5;
//        this.storeOn(stream, itemsPerLine);
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

    state { ^\stateless }
}

//StatefulCue -- adding player state
StatefulCue : AbstractCue {

    classvar <states;
    var <stateNum,
    <cleanup,
    <waitForPlay;


    *initClass {
        states = IdentityDictionary[
            \stopped -> 1,
            \loading -> 2,
            \ready -> 4,
            \playing -> 8,
            \paused -> 16,
            \stopping -> 32,
            \error -> 128
        ];
    }

    init {
        super.init;
        cleanup = EventStreamCleanup.new;
        stateNum = 1;
        parent.putAll((
            duration: nil,
            loader: {},
        ));

    }

    load {
        //Synchronous
        if (this.prCheckState(\stopped, \error)) {
            this.prChangeState(\loading);
            this.prLoad;
            this.prChangeState(\ready);
        }

    }

    prLoad {
        this[\loader].value;
        if (waitForPlay == true) {
            waitForPlay = false;
            this.prPlay;
        }
    }

    //Play handles logic, and makes sure prPlay is called at some point
    play { |time|
        switch(stateNum,
            states[\stopped], { waitForPlay = true; this.load; },
            states[\loading], { waitForPlay = true; },
            states[\ready], { this.prPlay },
            states[\paused], { this.resume }
        )
    }

    prPlay {
        this.prChangeState(\playing);
        //^super.play; //Event behaviour
        this.use {
			this[\play].value;
		};
    }

    stop { |now|
        if (this.isStopped.not) {
            this.prChangeState(\stopping);

            cleanup.exit(this);
            this.prStop(now);
        }
    }

    reset {
        this.stop(true);
    }

    prStop { |now| this.prChangeState(\stopped); }
    pause { "pause not implemented".warn; }
    resume { "resume not implemented".warn; }


    state { ^states.findKeyForValue(stateNum) }

    isStopped { ^this.prCheckState(\stopped) }
    isLoading { ^this.prCheckState(\loading) }
    isReady { ^this.prCheckState(\ready) }
    isPlaying { ^this.prCheckState(\playing) }
    isPaused { ^this.prCheckState(\paused) }


    prChangeState { arg st;
        stateNum = states[st];
        this.changed(\state);
    }

    prCheckState { arg ... st;
        ^(st.collect(states[_]).sum & stateNum == stateNum);
    }

}



//A StatefulCue with asynchronous loading
//Load will boot server, if needed
AsyncCue : StatefulCue {

    init {
        super.init;
        parent[\server] = Server.default;

    }

    prLoad {

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

}


GroupCue : StatefulCue {

    makeFunc { |func|
        super.makeFunc(func);
        //this[\cues].postln;
        this.cues.do(_.addDependant(this));
    }

    cues_ { arg c;
        this.cues.do(_.removeDependant(this));
        this[\cues] = c;
        this.cues.do(_.addDependant(this));
    }

    update { arg cue, what, args;
        var z;

        if (what == \state) {
            z = this.cues.collect(_.state);
            [\stopped, \ready, \loading, \stopping, \paused, \playing, \error].do { arg x;
                if (z.includes(x)) {
                    stateNum = states[x];
                }
            };
            this.changed(\state);
        };
    }

    load {
        this.cues.do(_.load);
    }

    play {
        this.cues.do(_.play);
    }

    stop { |now|
        this.cues.do(_.stop(now));
    }
    pause {
        this.cues.do(_.pause);
    }
    resume {
        this.cues.do(_.resume);
    }
}



/*


PatternCue : DurationCue {

    var <pattern,
    <>maxStartTime = 1024;

    *new { |cueName, pattern, duration=inf|
        ^super.new(cueName, duration).pattern_(pattern);

    }

    startTime_ { arg s;
        (s > maxStartTime).if  {
            (this.class.asString ++
                ": Can't set start time > maxStartTime. Setting it to " ++ maxStartTime).warn;
        };
        startTime = min(maxStartTime, s);

    }

    pattern_ { |p|
        pattern = Pevent(p, (dur:1)).ff(startTime, maxStartTime).finDur(max(0, endTime - startTime));
    }

    play {
        player = pattern.play;
    }

}
*/

