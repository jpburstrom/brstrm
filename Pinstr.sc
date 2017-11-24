//Pinstr can be used in patterns to play an Instr synthdef
//It loads the def to the server on *new
//and returns the defname on -next

//Not sure how to implement this...
//TODO: Needs a booted server. How do we deal with that?
//And what happens on reboot? Should there be a safety net?

Pinstr {
    var <name, <args, <defName;
    *new { arg name, args;
        ^super.new.init(name, args);
    }

    storeArgs { ^[name, args] }

    init { |na, ar|
        name = na;
        args = ar;
        this.reset;
    }

    next { ^defName }
    value { ^defName }

    reset {
        defName = Instr(name).add(args).name;
    }
}
