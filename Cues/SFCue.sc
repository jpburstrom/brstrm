SFCue : AsyncCue {
    var rout;

    init {
        super.init;
        parent.make {
            ~fadeIn = 0.1;
            ~fadeOut = 0.1;
            ~rate = 1;
            ~outbus = 0;
            ~volume = 1;
            ~loader = {
                if (~player.respondsTo(\server).not) {
                    ~player = RedSFPlayerDisk(~server);
                };
                ~player.read(~path);
                ~duration = if (~loop != true) {
                    ~player.duration * (1 / ~rate);
                } {
                    inf
                }
            };
            ~play = {
                this.use {
                    ~player.amp_(~volume);
                    //MixerChannel support
                    if (~mixerChannel.notNil) {
                        var target = ~mixerChannel.synthgroup;
                        this.proto ?? { this.proto = () };
                        this.proto.putAll((
                            chan: ~mixerChannel,
                            server: ~mixerChannel.server,
                            group: target.tryPerform(\nodeID) ?? { target },
                            bus: ~mixerChannel.inbus,
                            outbus: ~mixerChannel.inbus.index,
                            out: ~mixerChannel.inbus.index,
                            i_out: ~mixerChannel.inbus.index
                        ));

                    };
                    rout = Routine ({
                        ~preWait.wait;
                        ~server.latency.wait;
                        if (~loop == true) {
                            ~player.loop(~outbus, ~rate, ~fadeIn, ~group);
                        } {
                            ~player.play(~outbus, ~rate, ~fadeIn, ~group);
                            1.wait;
                            while { ~player.isPlaying } {
                                0.5.wait;
                            };
                            this.prChangeState(\stopped);
                        };
                    }).play(quant:~quant);
                };
            }
        }
    }

    *fromFile { |file, name, func|
        ^this.new(name, func).make {
            ~path = file;
        }
    }

    prStop { |now|
        var q;
        rout.stop;
        now.debug;
        if (now == true) {
            //"NOW".postln;
            this[\player].stop(0);
            ~player.free;
            ^this.prChangeState(\stopped);
        };
        this.use {
            this.getClock.play( inEnvir {
                var p = ~player;
                this.getClock.sched(~server.latency ? 0.2, inEnvir {
                    ~player.stop(~fadeOut);
                });
                this.getClock.sched(~fadeOut + ~server.latency ? 0.2, inEnvir {
                    var buffer = ~player.buffer;
                    buffer.close({buffer.free});
                    this.prChangeState(\stopped);
                });
            }, ~stopQuant ? ~quant);

        }
    }

    volume_ { arg vol;
        this.use {
            ~player.amp = vol;
            ~volume = vol;
        }
    }

}

SampleCue : SFCue {
    init {
        super.init;
        parent.make {
            ~loader = {
                if (~player.respondsTo(\server).not) {
                    ~player = RedSFPlayer(~server);
                };
                ~player.read(~path);
                ~duration = if (~loop != true) {
                    ~player.duration * (1 / ~rate);
                } {
                    inf
                }
            };
        }
    }
}