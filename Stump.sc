//Ex: prep routine


Stump {
    var <>pprep, <>pplay, <>maxTime, <prepDurs, <playDurs, <prepDur, <playDur, <>prob;
    var player=false;

    *new { arg pprep, pplay, maxTime=300;
        ^super.newCopyArgs(pprep, pplay, maxTime);
    }

    init {
        this.makePrepStream;
        this.makePlayStream;
    }

    makePrepStream {
        var p = Pfindur(maxTime, pprep).asStream.all(());
        prepDurs = Pseq(p.collect(_.delta));
        ^prepDur = p.reduce { |a, b| a.delta + b.delta } ;
    }

    makePlayStream {
        var p = Pfindur(maxTime, pplay).asStream.all(());
        playDurs = Pseq(p.collect(_.delta));
        ^playDur = p.reduce { |a, b| a.delta + b.delta } ;
    }

    asPattern {
        ^Pspawner({ arg sp;
            sp.seq(Pbindf(pprep, \dur, prepDurs));
            // this.changed(\prep);
            sp.seq(Pbindf(pplay, \dur, playDurs));
            // this.changed(\donePlaying);
        })
    }

    play {
        if (player) {
            player = this.asPattern.play;
        }
    }

    stop {
        player.stop;
        player = false;
    }

}

/*

a = Stump(Pbind(\dur, Pwhite(1.2,1.3, 1), \hallo, Pwhite()), Pbind(\dur, 1, \stretch, Pwhite(1, 2.0), \note, Pseries(0, 1, 4))).init
a.makePrepStream

Pchain(a.prepStream.asStream.next(()), a.pprep.asStream.next(())).asStream.next(())


(a.prepStream <> a.pprep ).trace.play
a.pprep.trace.play

f = a.asPattern.trace.play

f.postcs
a.prepStream.trace.play

Pfindur(300, a.pprep).asStream.all(())

ASR

*/

