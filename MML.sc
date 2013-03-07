MML {

    *note { arg st;
        var notes = (c: 0, d:2, e:4, f:5, g:7, a:9, b:11),
        oct = 0,
        index = -1,
        octave = 0,
        neu = [],
        do = (
            '-': { neu[index] = neu[index] - 1 },
            '+': { neu[index] = neu[index] + 1 },
            '>': { octave = octave + 1; },
            '<': { octave = octave - 1; }
        );
        st.do { |x|
            x = x.asSymbol;
            if (notes[x.asSymbol].notNil) {
                index = index + 1;
                neu = neu ++ (notes[x.asSymbol] + (octave * 12));
            } {
                do.at(x).value
            }
        };
        ^neu
    }


}


+String {
    asMML {
        ^MML.note(this)
    }
}

