//Fix for not using rate when calculating duration
//Allow to set target

+RedSFPlayer {
    loop {|out= 0, rate= 1, fadeTime= 0, target|
		this.prPlay(out, rate, fadeTime, true, target);
	}

    play {|out= 0, rate= 1, fadeTime= 0, target|
		this.prPlay(out, rate, fadeTime, false, target);
	}
    prSendDefs {
        8.do{|i|
            SynthDef("redSFPlayer"++(i+1), {|out= 0, rate= 1, atk= 0, rel= 0, loop= 1, amp= 1, buf, gate= 1|
                var env= EnvGen.kr(Env.asr(atk, 1, rel), gate, doneAction:2);
                var src= PlayBuf.ar(i+1, buf, rate*BufRateScale.ir(buf), 1, 0, loop);
                FreeSelfWhenDone.kr( src );
                Out.ar(out, src * env*amp);
            }).send(server);
        };
    }
    prPlay {|out, rate, fadeTime, loop, target|
        if(this.isPlaying, {
            this.stop(fadeTime);
        });
        synth= Synth("redSFPlayer"++channels, [
            \out, out,
            \rate, rate,
            \atk, fadeTime,
            \rel, fadeTime,
            \loop, loop,
            \amp, amp,
            \buf, buffer
        ]);
        NodeWatcher.register(synth);

    }

}

+RedSFPlayerDisk {
    prSendDefs {
        8.do{|i|
            SynthDef("redSFPlayerDisk"++(i+1), {|out= 0, rate= 1, atk= 0, rel= 0, loop= 1, amp= 1, buf, gate= 1|
                var env= EnvGen.kr(Env.asr(atk, 1, rel), gate, doneAction:2);
                var src= VDiskIn.ar(i+1, buf, rate*BufRateScale.ir(buf), loop);
                FreeSelfWhenDone.kr( src );
                Out.ar(out, src*env*amp);
            }).send(server);
        };
    }
    prPlay {|out, rate, fadeTime, loop, target|
        var attackTime, cond= Condition(true);
        fork{
            if(this.isPlaying, {
                attackTime= 0;
                cond.test= false;
                this.prStop(0, cond);
                }, {
                    attackTime= fadeTime;
            });
            cond.wait;
            buffer.cueSoundFile(buffer.path, 0, {
                synth= Synth("redSFPlayerDisk"++channels, [
                    \out, out,
                    \rate, rate.max(0),				//need to block negative rates
                    \atk, attackTime,
                    \rel, fadeTime,
                    \loop, loop,
                    \amp, amp,
                    \buf, buffer
                ]);
                NodeWatcher.register(synth);
            });
        };
    }

}