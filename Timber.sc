Timber : List {
    classvar <>prototype;
    var <weights, do_calc_weights=true;

    *initClass {
        prototype = (
            prob: 0.5,
            playcount: 0
        );
    }

    wchoose {
        if (do_calc_weights) {
            this.calc_weights;
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


    asPattern { |times=128, func, cleanup|
        ^Pfset(
            func,
            Pspawner({ arg sp;
                var p;
                times.do {
                    p = this.wchoose;
                    p.playcount = p.playcount + 1;
                    p.at(\pre).(p, sp);
                    sp.seq(p.thing);
                    p.at.(\post).(p, sp);
                }
            }),
            {
                cleanup.value;
                this.reset;
            }
        );
    }

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






