//
Pfsym : Psym {

    lookUp { arg key;
		^(dict.value ?? { this.lookupClass.all }).at(key) ?? { this.lookupClass.default }
	}

}

