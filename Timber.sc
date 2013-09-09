Timber : List {
    classvar <>prototype;
    var <weights, do_calc_weights=true, spawner, pcs;

    *initClass {
        prototype = (
            prob: 0.5,
            playcount: 0,
            prewait: 0,
            postwait:0

        );
    }

    *new { arg ... args;
        ^super.new(*args).initpcs;
    }

    initpcs {
        pcs = List[];
    }

    wchoose {
        if (do_calc_weights) {
            this.calc_weights;
            this.changed(\weights);
        }
        ^super.wchoose(weights);
	}

    calc_weights {
        weights = this.collect(_.prob);
        if (weights.includes(1)) {
            weights = weights.floor;
        };
        weights = weights.normalizeSum;
        do_calc_weights = false;
    }

    add { |key ... val|
        val.unbubble;
        if (this.collect(_.thing).includes(key)) {
            ^this
        };

        val = Backpack[ key ].putAll(prototype, val);
        val.parent_(
            (
                prob_: { arg ev, a;
                    ev.make { ~prob = a.clip(0,1) };
                    do_calc_weights = true;
                    this.changed(\weights);
                    ev
                }

            )
        ).proto_(val.copy);

        do_calc_weights = true;

        ^super.add(val);
    }

    reset {
        this.do { arg x;  x.putAll(x.proto) };
    }

    suspendAll {
        spawner !?  { spawner.suspendAll };
        pcs.do (_.stop);
        pcs.clear;
    }

    asPattern { |times=128, func, cleanup|
        ^Pfset(
            func,
            Pspawner({ arg sp;
                var p;
                spawner = sp;
                times.do {
                    var pc;
                    p = this.wchoose;
                    p.cond ?? { p.cond = Condition() } ;
                    this.changed(\current, p);
                    p.playcount = p.playcount + 1;
                    p.at(\prefunc).(p, sp);
                    if (p.when.notNil) {
                        p.use { p.when.fork };
                        sp.seq((play: { p.cond.hang }, dur:0));
                    };
                    p.prewait.wait;
                    if (p.at(\until).notNil) {
                        var test = true;

                        if (p.breaknote == true) {
                            pc = PatternConductor(p.thing).play;
                            pcs.add(pc);
                            fork {
                                p.cond.hang;
                                test = false;
                                pc.stop;
                            };
                            p.use { p.at(\until).fork }; //unhang p.cond
                            while {test == true} {
                                0.01.wait;
                            };

                        } {
                            fork {
                                p.cond.hang;
                                test = false;
                            };
                            p.use { p.at(\until).fork }; //unhang p.cond
                            //run p.thing until test = false
                            sp.seq(Pif( Pfunc { test }, p.thing, nil));
                        };
                    } {
                        sp.seq(p.thing);
                    };
                    p.postwait.wait;
                    p.at(\postfunc).(p, sp);
                }
            }),
            {
                cleanup.value;
                this.reset;
            }
        );
    }

    //TODO: playSingle, with same args as add


}


//A thing with a backpack environment

Backpack : Environment {
    var <thing;

    *new { arg n=8, proto, parent, know=true, thing;
        ^super.new(n).proto_(proto).parent_(parent).know_(know).init(thing)
	}

    *newFrom { | aCollection |
		var newCollection;
        newCollection = this.new(aCollection.takeAt(0), aCollection.size);
        aCollection.keysValuesDo({ arg k,v, i; newCollection.put(k,v) });
		^newCollection
	}

    *with { | ... args |
		var newColl;
		// answer a collection of my class of the given arguments
		// the class Array has a simpler implementation
       newColl = this.new(args[0].size);
		newColl.addAll(args);
		^newColl
	}

    add { |v|
        if (thing.isNil) {
            thing = v;
            ^this
        }
        ^super.add(v);
    }

    value {
        ^thing
    }


    init { |t|
        thing = t;
    }

    storeOn { | stream |
		if (stream.atLimit) { ^this };
		stream << this.class.name << "[ " ;
        thing.storeOn(stream);
        stream.comma.space;
		this.storeItemsOn(stream);
		stream << " ]" ;
	}
    printOn { | stream |
		if (stream.atLimit) { ^this };
		stream << this.class.name << "[ " ;
        thing.printOn(stream);
        stream.comma.space;
		this.printItemsOn(stream);
		stream << " ]" ;
	}

}






