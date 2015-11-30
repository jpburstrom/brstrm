NodeProxyCue : AsyncCue {

    init {
        super.init;
        parent.make {
            ~rate = \audio;
            ~numChannels = 2;
            ~fadeIn = 0.1;
            ~fadeOut = 0.1;
            ~loader = {
                ~player.removeDependant(this);
                ~player.postln;
                ~player = NodeProxy(~server, ~rate, ~numChannels, ~player.sources);
                ~player.addDependant(this);
            };
            ~play = {
                this.use {
                    if (~out.size == 0) {
                        ~player.play(~out, ~channels, ~group, false, ~vol, ~fadeIn, ~addAction);
                    }{
                        ~player.playN(~out, ~amp, ~in, ~vol, ~fadeIn, ~group, ~addAction);
                    }
                }
            }
        };
        this.use {
            ~player = NodeProxy(~server, ~rate, ~numChannels);
        }
    }

    prPlay {
        this.use {
            this.setClockQuant;
			~play.value;
		};
    }
    prStop { |now|
        this.setClockQuant;
        if (now.notNil) {
            this[\player].end(0.01, true);
        } {
            this[\player].end(this[\fadeOut]);
        }
    }

    pause {
        switch (this.state,
            \paused, { ^this.resume },
            \playing, {
                this[\player].pause;
                this.prChangeState(\paused);
        });


    }

    resume {
        this[\player].resume;
        this.prChangeState(\playing);
    }

    //Set clock & quant before play/stop
    setClockQuant { |stop = false|
        this.make {
            if (stop) {
                ~player.quant = ~stopQuant ? ~quant;
            } {
                ~player.quant = ~quant;
            };
            ~player.clock = ~clock.value;
        }
    }



    player_ { |sources|
        //So we can assign its source by doing "aCue.player = { ... }"
        this[\player][0..] = sources;
    }

    update { arg changed, what, args;
        if (changed == this[\player]) {
            if (what == \play or: { what == \playN }) {
                this.prChangeState(\playing);
            } {
                if (what == \end) {
                    this.getClock.sched(args[0] ? 0, {
                        this.prChangeState(\stopped);
                    });
                }
            }
        }
    }
}



