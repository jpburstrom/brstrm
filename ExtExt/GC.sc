//A GenericGlobalControl wrapper
//Differences:
//Swapped arguments
//A dictionary classvar, like Pdef etc

GC : GenericGlobalControl {
    classvar <>all;

    storeArgs { ^[name, value, spec, bus] }

    *new { arg name, value, spec, bus, allowGUI = true ... extraArgs;
        var res = this.at(name);
        if(res.isNil) {
            res = super.new(name, bus, value, spec, allowGUI, *extraArgs);
        } {
            if (value.notNil) {
                res.gcSetArgs(bus, value, spec, allowGUI);
            }
        }
        ^res

    }

    *at { |key|
        ^this.all.at(key);
    }

    *initClass {
        all = IdentityDictionary.new;
    }

    init { arg ... args;
        super.init(*args);
        all[name] = this;
    }

    gcSetArgs { arg b, val, sp, guiOK;
        if (b.notNil) { bus = b };
        if (sp.notNil) {
            spec = sp.asSpec;
        };
        allowGUI = guiOK;
        this.set(val ? spec.default)
    }
}

/*
*/

GCM : VoicerMIDIController {
    classvar <all;

    *initClass {
        all = IdentityDictionary.new;
    }

    *new { arg key, chan, num ... args;
        var destination, res = this.at(key);
        if(res.isNil) {
            destination = GC(key);
            res = super.new.prInit(chan.asChannelIndex, num, destination).performList(\init, args);
            this.all[key] = res;
        } {
        }
        ^res
	}

    init {
        super.init;
        syncSign = 0;
    }


    *at { |key|
        ^this.all.at(key);
    }

    free {
		parent.removeControl(this);
		this.clear;
        all.remove(this);
	}

}
