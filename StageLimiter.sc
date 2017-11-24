//Batuhan Bozkurt 2009
StageLimiter
{
	classvar <activeSynth, <numChannels, wait;

	*activate { arg channels=2;
        numChannels = channels.asArray;
        if (activeSynth.isPlaying) {
            activeSynth.free;
        };
        Server.default.doWhenBooted({
            this.prActivate;
            CmdPeriod.add(this);
            }, 1, {
                CmdPeriod.add(this);
                "Now boot your server and hit Cmd+Period".inform;
        });
	}
    *doOnCmdPeriod {
        this.prActivate;
    }

    *prActivate {
        var s = Server.default;
        fork {
            var name = "stageLimiter%".format(numChannels.join("+")).asSymbol;
            //Sending one single synthdef
            SynthDef(name,
                {
                    var input, offset = 0;
                    numChannels.do { arg i;
                        "StageLimiter: % ch, offset %".format(i, offset).postln;
                        input = In.ar(offset, i);
                        input = Select.ar(CheckBadValues.ar(input, 0, 0), [input, DC.ar(0), DC.ar(0), input]);
                        ReplaceOut.ar(offset, Limiter.ar(input, 1)) ;
                        offset = offset + i;
                    }
            }).add;
            s.sync;
            activeSynth = Synth(name,
                target: RootNode(s),
                addAction: \addToTail
            );
            NodeWatcher.register(activeSynth);

        };
    }

	*deactivate
	{
		activeSynth.free;
		CmdPeriod.remove(this);
        "StageLimiter inactive...".postln;
	}
}