Setup  {

	var	<>rebuildOn, server, waitForBoot, <functions, <env, cb, rebuilding=false;

	*new { arg initFunc, rebuildOn, waitForBoot=true, server=\default;

        ^super.newCopyArgs(rebuildOn, server, waitForBoot).init(initFunc);
	}

    *clearAllServerActions {
        [ServerTree, ServerBoot, ServerQuit].do({ |action|
            action.objects.copy.do({ |list|
                list.copy.do({ |obj|
                    if (obj.isKindOf(Setup)) {
                        obj.rebuildOn = nil;
                        // obj.postln;
                        action.remove(obj);
                    }
                })
            })
        })
    }

		// init clears the environment
	init { arg f;
        var c;
        if (env.notNil) {
            this.freeAll;
        } {
            env = ();
            cb = ();
            env.know = true;
            ServerTree.add( this, server);
            ServerBoot.add( this, server);
            ServerQuit.add( this, server);
        };
        functions = List();
        this.addFunction(f);

        this.addDependant(this);

        {
            if (waitForBoot) {
                Server.perform(server).bootSync
            };
            this.make(f);
            c = Condition();
            Server.perform(server).sync(c);
            this.changed(\init, 1);
        }.fork
    }

    update { arg changed, changer;
        var result;
        cb[changer].do { |x|
            result = env.use(x);
            if (result.isKindOf(Symbol)) {
                this.changed(result);
            }
        };
        if (rebuilding and: { changer == \freeAll } ) {
            this.build();
            rebuilding = false;
        }
    }

    on { arg changer, func;
        cb[changer] ?? { cb[changer] = [] };
        cb[changer] = cb[changer].add(func);
    }

    doOnServerTree {
        if (rebuildOn == \tree) {
            this.build()
        }
    }

    doOnServerBoot {
        if (rebuildOn == \boot) {
            this.build()
        }
    }

    doOnServerQuit {
        if (rebuildOn == \quit) {
            this.build()
        }
    }

    addFunction { |f|
       functions.add(f);
    }

    free {
        ServerTree.remove( this, server);
        ServerBoot.remove( this, server);
        ServerQuit.remove( this, server);
        this.freeAll;
        functions = nil;
        env = nil;
        rebuildOn = nil;
    }

	freeAll { arg ... args;
        var e = env.copy;
        forkIfNeeded {
            e.do { |x|
                if (Node.allSubclasses.includes(x.class)) {
                    if (x.isRunning) { x.free; x.waitForFree; };
                } {
                    x.free
                }
            };
            this.changed(\freeAll)
        };
        env = ();
	}

    reset { this.rebuild }
    rebuild {
        rebuilding = true;
        this.freeAll;
    }

    build {
        functions.do ({ |f|
            this.use(f)
        })
    }

    make { arg function;
        this.use(function);
        ^this.env
    }

    makeAdd { arg function;
        this.addFunction(function);
        this.use(function);
        ^this.env
    }

    use { arg function;
        // temporarily replaces the currentEnvironment with this,
        // executes function, returns the result of the function
        var result, saveEnvir, c, s;

        s = Server.perform(server);
        saveEnvir = currentEnvironment;
        currentEnvironment = env;
        {
            c = Condition.new;
            if (Server.allRunningServers.findMatch(s).notNil) {
                s.sync(c)
            };
            protect {
                result = function.value;
                env.do { |child|
                    if (Node.allSubclasses.includes(child.class)) {
                        child.register
                    }
                };
            }{
                currentEnvironment = saveEnvir;
            };
            c.test = false;
            s.sync(c);

            this.changed(\build);

        }.forkIfNeeded;
        ^result
    }


    at { |sel|
        ^env.at(sel)
    }

    doesNotUnderstand { arg selector ... args;
		var result, item;
        if (env.isNil) { ^nil };
        (item = env.at(selector)).isFunction.if({
            env.use({ result = item.valueArray(args) });
            }, {
                selector.isSetter.if({
                    DoesNotUnderstandError(this, selector, args).throw
                    }, {
                        result = item
                });
		});
		^result
	}

	perform { arg selector ... args;
        super.respondsTo(selector).if({ ^super.perform(selector, *args) });
		^this.performList(\doesNotUnderstand, [selector] ++ args);
	}

	tryPerform { arg selector ... args;	// for sth like draggedInto...GUI
		^this.perform(selector, *args);
	}

	respondsTo { arg selector;
		super.respondsTo(selector).if({ ^true });
		^this.envRespondsTo(selector)
	}

	envRespondsTo { |selector|
		var	recursivetest = { |environment, method|
				block { |break|
					environment.keysDo({ |key|
						(key === method).if({ break.(true) });
					});
					environment.parent.notNil.if(
						{ recursivetest.(environment.parent, method) },
						{ false });
				};
			};
		^recursivetest.(env, selector)
	}

    printOn { arg stream, itemsPerLine = 5;
		stream << "a " << this.class.asString;
        if (this.env.notNil) {
            this.env.printOn(stream, itemsPerLine);
        }
    }
}
