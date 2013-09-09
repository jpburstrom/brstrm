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
    classvar states;
    var <>cueName,
    <number,
    <state,
    <ev,
    <defaultParentEvent,
    <cleanup, <cleanupList,
    <>preWait,
    <>postWait,
    <>continueMode;

    *initClass {
        states = IdentityDictionary[
            \stopped -> 1,
            \loading -> 2,
            \ready -> 4,
            \playing -> 8,
            \paused -> 16,
            \error -> 128
        ];
    }

    *new {
        ^super.new.init;
    }

    init {
        cleanup = EventStreamCleanup.new;
        this.parent = (
            type: \cue,
            server: Server.default,
            play: {
                "hello".postln;
            }
        )
    }

    load {
        //Load/Cleanup code from Pproto
        var loader, event, ev;
		var proto;			// temporary proto event used in allocation
		var makeRoutine;	// routine wrapper for function that makes protoEvent
        var protoEvent;		// protoEvent created by function


        state = states[\loading];

        loader = Routine( { this.make( this[\loader] )  }  );
        proto = (
            delta: 0, 						// events occur simultaneously
            finish: { ev = currentEnvironment } 	// get copy of event object actually played
        );

        while {
            (ev = loader.next(ev)).notNil;

        } {
            event = ev.proto_(proto).play;
            cleanupList = cleanupList.add(ev)
        };

        //Add all functions to cleanup
		cleanup.addFunction(event, { | flag |
			cleanupList.do { | ev |
				EventTypesWithCleanup.cleanup(ev, flag)
			}
		});

        //Use this[\addToCleanup] to add extra cleanup functions
        //
        this.removeFromCleanup = nil;
        cleanup.update(this);

        //Fork & Sync
        fork {
            this[\server] !?  { this[\server].sync };
            state = states[\ready];
            "Done loading".postln;
        }


    }

    play { |time|
        state = states[\playing];
        ^super.play;
    }

    stop {
        state = states[\stopped];
        cleanupList.do { | ev | cleanup.exit(ev) };
        cleanup.exit(this);
        cleanupList = nil;
    }



    isStopped { ^this.prCheckState(\stopped) }
    isLoading { ^this.prCheckState(\loading) }
    isReady { ^this.prCheckState(\ready) }
    isPlaying { ^this.prCheckState(\playing) }
    isPaused { ^this.prCheckState(\paused) }

    prCheckState { arg ... st;
        ^(st.collect(states[_]).sum & state == state);
    }

}

DurationCue : AbstractCue {

    var <>duration,
    <>startTime,
    <>endTime,
    <>loopTimes,
    <>loopStart,
    <>loopEnd,
    player;

    *new { |cueName, duration=inf|
        ^super.new(cueName).duration_(duration).setDefaults();
    }

    setDefaults {
        startTime = loopTimes = loopStart = 0;
        endTime = loopEnd = inf;
        ^this
    }

    play {
        player.play
        ^this
    }

    stop {
        player.stop
        ^this
    }

    pause {
        player.pause
        ^this
    }

    isPlaying {
        player.isPlaying
    }
}

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

