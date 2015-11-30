+InstrSynthDef {
    *makeDefName { arg instr, args, outClass=\Out;
        var name, longName, firstName;

        longName = [instr.dotNotation, outClass];
        args.do { arg obj,i;
            var r;
            r = obj.rate;
            if([\audio, \control].includes(r).not) {
                longName = longName.add(obj);
            };

        };

        firstName = instr.name.last.asString;
        if(firstName.size > 18, {
            firstName = firstName.copyRange(0, 16);
        });
        name = firstName ++ "#" ++ this.hashEncode(longName);
        ^[longName, name]
    }
}