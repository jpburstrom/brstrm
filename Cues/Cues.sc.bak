//CUE
/*
Statuses:
-1 error
0 stopped
1 loading
2 loaded
3 playing
4 paused

a = AbstractCue()

a.playAndDelta(nil, false)
a.dur = 23

a[\play].value

a.play

*/


AbstractCue : Event {
    classvar states, <all;
    var <name,
    <number,
    stateNum,
    waitForPlay,
    <ev,
    <defaultParentEvent,
    <cleanup;

    *initClass {
        states = IdentityDictionary[
            \stopped -> 1,
            \loading -> 2,
            \ready -> 4,
            \playing -> 8,
            \paused -> 16,
            \error -> 128
        ];
        all = IdentityDictionary();
    }

    clone { |n|
        var new;
        n = this.class.nextName(n ? name);
        this
        ^this.class.newFrom([n] ++ this.asKeyValuePairs)
    }

    *new { |name, size=8|
        if (name.isNil) { name = this.nextName };
        all[name] !? { ^all[name] };
        ^super.new(size).name_(name).init;
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
        ^all
    }


    init {
        cleanup = EventStreamCleanup.new;
        stateNum = 1;
        //Abstract cue sets parent, subclasses can set proto, ok?
        parent = (
            type: \abstractCue,
            server: Server.default,
            play: {
                ~player.value;
            },
            player: {},
            loader: {}
        )
    }

    load {
        stateNum = states[\loading];
        this.changed(\state);

        //Fork & Sync
        this[\server].waitForBoot {
            this.use {
                this[\loader].asRoutine.embedInStream;
                this[\server] !?  { this[\server].sync };
                this.prChangeState(\ready);
                if (waitForPlay == true) {
                    waitForPlay = false;
                    this.prPlay;
                };
                currentEnvironment.postln;
                "Done loading".postln;
            }

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
        ^super.play;
    }

    stop { |now|
        if (this.isStopped.not) {
            this.prStop(now);
            cleanup.exit(this);
            this.prChangeState(\stopped);
        }
    }

    //Implemented in subclasses
    prStop { }
    pause { "pause not implemented".warn; }
    resume { "resume not implemented".warn; }

    syncTo { |other|
        other !? {
            this[\clock] = { other.getClock }
        }
    }

    getClock {
        ^this[\clock].value;
    }

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

    printOn { arg stream, itemsPerLine = 5;
        this.storeOn(stream, itemsPerLine);
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

SpawnerCue : AbstractCue {
    var spawner, player, <conductor;

    init {
        var p;
        proto = (
            type: \spawnerCue,
            play: {
                p = Pspawner({ arg sp;
                    player = conductor.eventStreamPlayers[0];
                    player.addDependant(this);
                    spawner = sp;
                    sp.wait(~preWait);
                    //Shortcut -- embed pattern automatically
                    if (~player.isKindOf(Pattern)) {
                        sp.seq(~player);
                    } {
                        { ~player.(sp) }.asRoutine.embedInStream;
                    }
                });
                conductor = PatternConductor(p);
                conductor.quant_(~quant);
                conductor.play(this.getClock);

            }
        )
        ^super.init;
    }

    pause {
        switch (this.state,
            \paused, { ^this.resume },
            \playing, {
                conductor !? { conductor.pause };
                this.prChangeState(\paused);
        });


    }

    resume {
        conductor !? { conductor.resume };
        this.prChangeState(\playing);
    }

    prStop { |now|
        var tmp, q;
        this.use {
            now ?? {
                tmp = ~stopTempo;
                q = ~stopQuant ? ~quant
            };
        };
        (this.getClock ? TempoClock.default).play({
            conductor !? { conductor.clock !? { conductor.stop(tmp) } };
            spawner = nil;
            player = nil;
        }, q)

    }

    update { arg changed, what;
        if (changed == player and: { what == \stopped }, {
            //This should be ok
            this.stop;
            [changed, "stopped"].postln;
        });
        /*
        if (changed == player and: { what == \userStopped }, {
            //This signal would cause a race
            [changed, "userstopped"].postln;
        });
        */
    }

    getClock {
        //If conductor has a clock, get it
        //otherwise get clock var
        ^(conductor !? {conductor.clock}) ? this[\clock].value;
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

/*
x = PatternCue("test", Pbind(), 3)
x.pattern = Pbind()
x.play
x.pattern
x.stop

Pfindur(10, Pevent(Pbind(), (dur:1))).asStream.all(())
.asStream.nextN(2, ())

v = min(2, 54)

Event
(2>1).if { "hello".warn }
Pseq(({ 4.0.rand } ! 4).round(0.125).sort, 1).asStream.all

( { 4.0.rand } ! 4 ).round(0.125).sort.differentiate.

Array

*/


