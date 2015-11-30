//Non-existent args in dictionary shouldn't become hardcoded
+Instr {
    convertArgs { arg args;
        if(args.isKindOf(Dictionary),{
            ^this.argNames.collect({ arg an,i;
                //If nil, just return nil
                args.at(an) !? { this.convertArg(args.at(an),i) }
            })
        });
        ^args ? []
    }
}

