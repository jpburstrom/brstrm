//Things that would be good:
//1. Soundfile player
//2. Function (SynthDef)
//3. SynthDef
//4. Pattern
/*
Instr, NodeProxy etc can be added later (routing gets messy)
CueWrap (base class) should implement:
preWait time
postWait time
--------------

A cue plays within its own group. It has LFO's/Envelopes on top and volume/pan controllers at the bottom:
-----------
Controllers (LFO's)
Cue
Volume envelope
Pan
FX
-----------
Mycket som mixerChannel, men det krånglar till det med


*/


CueWrap {
    classvar <states, <defaultWrappers;
    var <player, <actions, statenum, waitForPlay=false;

    *initClass {
        states = IdentityDictionary[
            \stateless -> 0,
            \stopped -> 1,
            \loading -> 2,
            \ready -> 4,
            \playing -> 8,
            \paused -> 16,
            \stopping -> 32,
            \error -> 128
        ];

        defaultWrappers = IdentityDictionary[
            AbstractPlayer -> CueWrapNodeProxy,
            Function -> CueWrapFunction,
            NodeProxy -> CueWrapNodeProxy,
            Instr -> CueWrapInstr,
            SoundFile -> CueWrapSoundFile,
            String -> CueWrapSoundFile,
            Pattern -> CueWrapSpawner,
            SynthDef -> CueWrapSynthDef
        ];

    }

    *new { |player|
        ^super.new.init(player).addActions;
    }

    *fromPlayer { |player|
        var cl;
        ^(defaultWrappers[player] ?? this.class).new(player);
    }

    init { |p|
        player = p;
        player.addDependant(this);
    }

    addActions {
        this.subclassResponsibility(\addActions);
    }

    remove {
        player.removeDependant(this);
    }

    //The following functions should be called within environment

    load { |ev|
        //load all necessary resources and send ready signal
        //can use forkIfNeeded?
        ev.use {
            this.prChangeState(\loading);
            this.prLoad;
            this.prChangeState(\ready);
        }
    }

    play { |ev|
        var c = Condition();
        ev.use {
            switch(statenum,
                states[\stopped], { waitForPlay = true; this.load; },
                states[\loading], { waitForPlay = true; },
                states[\paused], { this.resume },
                states[\ready], {
                    this.prPlay(c);
                    c.wait;
                    this.prChangeState(\playing);
                }
            );

        }
    }

    stop { |ev|
        if (this.isStopped.not) {
            var c = Condition();
            ev.use {
                this.prChangeState(\stopping);
                this.prStop(c);
                c.wait;
                this.free;
                this.prChangeState(\stopped);
            }
        }
    }

    stopNow { |ev|
        if (this.isStopped.not) {
            var c = Condition();
            ev.use {
                this.prStopNow(c);
                c.wait;
                this.free;
                this.prChangeState(\stopped);
            }
        }
    }

    pause { |ev|
        var c = Condition();
        ev.use {
            this.prPause(c);
            c.wait;
            this.prChangeState(\paused);
        }
    }

    resume { |ev|
        var c = Condition();
        ev.use {
            this.prResume(c);
            c.wait;
            this.prChangeState(\playing);
        }
    }

    reset { |ev|
        var c = Condition();
        ev.use {
            this.prReset(c);
            c.wait;
        };
    }

    free {
        var c = Condition();
        this.prFree(c);
        c.wait;
    }

    update { | p, what ... args |
        if (p === player) {
            actions[what].value(args);
        }
    }
    //----------------

    prPlay { |c| this.subclassResponsibility(\prPlay) }
    prStop { |c| this.subclassResponsibility(\prStop) }
    prStopNow { |c| this.subclassResponsibility(\prStopNow) }
    prPause { |c| this.subclassResponsibility(\prPause) }
    prResume { |c|  this.subclassResponsibility(\prResume) }
    prReset { |c|
        c.test = true;
        this.stopNow;
    }

    state { ^states.findKeyForValue(statenum) }

    checkState { arg ... st;
        ^(st.collect(states[_]).sum & statenum == statenum);
    }

    prChangeState { arg st;
        statenum = states[st];
        this.changed(\state);
    }
}

CueWrapAbstractPlayer : CueWrap {

    addActions {
        actions[\stopped] = {
            this.prChangeState(\stopped);
        };
    }

    prLoad { |c|
        ~player.onReady { c.unhang };
        ~player.prepareForPlay;
    }

    prPlay { |c| ~player.spawn(this.quantToAtTime(~quant)); c.test = true; }

    prStop { |c|
        ~player.onStop { c.unhang };
        ~player.release(~releaseTime, this.quantToAtTime(~stopQuant ? ~quant))
    }

    prStopNow { |c|
        ~player.onStop { c.unhang };
        ~player.release(0,0);
    }

    prPause { |c|
        "Not implemented".warn;
    }

    prResume { |c|
        "Not implemented".warn;
    }

    quantToAtTime { |t|
        ^(t !? { round(4/t) }).asInt;
    }


}
/*
CueWrapFunction : CueWrap {
    var synth;

    addActions {
    }

    prLoad { |c|
    }

    prPlay { |c|
    }

    prStop { |c|
    }

    prStopNow { |c|
    }

    prPause { |c|
    }

    prResume { |c|
    }

}

CueWrapInstr : CueWrap {

}

CueWrapNodeProxy : CueWrap {

}

CueWrapSoundFile : CueWrap {

}

CueWrapSpawner : CueWrap {

}

CueWrapSynthDef : CueWrap {
}

*/