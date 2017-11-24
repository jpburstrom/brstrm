//One-shot function
OneShotCue : AbstractCue {
    makeFunc { arg func;
        this[\player] = func;
    }
    init {
        super.init;

        this.parent[\play] = {
            if (~preWait > 0) {
                ~clock.value.sched(~preWait, {
                    ~player.value;
                    nil
                });
            } {
                ~player.value;
            }
        }
    }

}

OneShotOtherCue : OneShotCue {
    classvar <actions;
    *initClass {
        actions= IdentityDictionary[];

    }
    *new { arg name, func, size=8;
        ^super.new((this.asString ++ " | " ++ name).asSymbol, func, size).make {
            ~other = name;
        }
    }
}

StopCue : OneShotOtherCue {

    *initClass {
        actions[\stop] = this;
    }
    makeFunc { arg func;

        this[\player] = {
            AbstractCue(~other).stop;
        }
    }
}


LoadCue : OneShotOtherCue {
    *initClass {
        actions[\load] = this;
    }
    makeFunc { arg func;
        this[\player] = {
            AbstractCue(~other).load;
        }
    }
}

PauseCue : OneShotOtherCue {
    *initClass {
        actions[\pause] = this;
    }

    makeFunc { arg func;
        this[\player] = {
            AbstractCue(~other).pause;
        }
    }
}