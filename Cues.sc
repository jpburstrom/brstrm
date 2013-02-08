AbstractCue {
    var <>cueName,
    <number,
    <>preWait,
    <>postWait,
    <>continueMode;


    *new { |cueName|
        ^super.newCopyArgs(cueName)
    }

    play { |time|
        ^SubclassResponsibilityError(this, "play", this.class).throw;
    }

    isPlaying {
        ^false
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

