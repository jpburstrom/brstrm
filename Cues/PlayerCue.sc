//A wrapper for crucial players
PlayerCue : AsyncCue {

    init {
        super.init;
        parent.make {
            ~play = {
                ~player.spawn(this.quantToAtTime(~quant))
            }
        };

    }

    prLoad {
        this.use {
            ~server.waitForBoot {
                ~player.onReady({
                    this.prChangeState(\ready);
                    if (waitForPlay == true) {
                        waitForPlay = false;
                        this.prPlay;
                    };
                });

                ~player.prepareForPlay;
            }
        }
    }

    pause {
        //Not implemented in players?
        /*
        switch (this.state,
            \paused, { ^this.resume },
            \playing, {
                //FIXME
                //conductor !? { conductor.pause };
                this.prChangeState(\paused);
        });
        */

    }

    resume {
        //FIXME
        //conductor !? { conductor.resume };
        //this.prChangeState(\playing);
    }

    prStop { |now|
        var tmp, q, time;
        this.use {
            now ?? {
                tmp = ~releaseTime;
                q = ~stopQuant ? ~quant
            };
            // q.debug("Quant");
            time = q !? (this.getClock ? TempoClock.default).timeToNextBeat(q);
            // time.debug("Time");
            ~player.onStop({
                this[\player].free;
                this.prChangeState(\stopped);
            });
            //[tmp, this.quantToAtTime(q)];
            ~player.release(tmp, this.quantToAtTime(q))
        };

    }

    quantToAtTime { |t|
        ^(t !? { round(4/t) }).asInt;
    }

    update { arg changed, what;

        //TODO: Detect when stopped
        if (changed == this[\player] and: { what == \stopped }, {
            //This should be ok
            this.stop;
            [changed, "stopped"].postln;
        });
    }

    getClock {
        //TODO: get clock from player?
        ^this[\clock].value;
    }

}
