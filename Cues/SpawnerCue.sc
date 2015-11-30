SpawnerCue : AsyncCue {
    var spawner, player, <conductor;

    init {
        var p;
        super.init;
        parent.make {

            ~play = {
                conductor = PatternConductor(

                    PFastForward(
                        Pfindur(~duration ? inf, //Set
                            Pspawner({ arg sp;
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
                            })
                        ),
                        ~fastForward ? 0
                    )
                );
                conductor.quant_(~quant);
                conductor.play(this.getClock);
            }
        }
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
        var tmp = 1, q, func = {
            conductor !? { conductor.clock !? { conductor.stop(tmp) } };
            spawner = nil;
            player = nil;
            this.prChangeState(\stopped);
        };
        this.use {
            tmp = ~stopTempo;
            q = ~stopQuant ? ~quant
        };
        if (now == true) {
            func.value;
        } {
            (this.getClock ? TempoClock.default).play(func, q);
        };



    }

    update { arg changed, what;
        if (changed == player and: { what == \stopped }, {
            //This should be ok
            this.stop(true);
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