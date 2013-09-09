+PatternProxy {

    tags {
        var tags = Library.at(\brstrm, this.class.asSymbol, \tags)
        .select({ arg x, y; x.includes(this) });
        ^tags !? { tags.keys }
    }

    tags_ { arg ...args;
        args = args.flatten;
        this.removeTag(this.tags);
        this.addTag(*args);
    }

    addTag { arg ...args;
        var tags = Library.at(\brstrm, this.class.asSymbol, \tags);
        args = args.flatten.collect(_.asSymbol);
        args.do { arg sym;
            var set = Library.at(\brstrm, this.class.asSymbol, \tags, sym);
            set = set.add(this).asSet;
            Library.put(\brstrm, this.class.asSymbol, \tags, sym, set);
        }
    }

    removeTag { arg ...args;
        args = args.flatten.collect(_.asSymbol);
        args.do { arg sym;
            Library.at(\brstrm, this.class.asSymbol, \tags, sym).remove(this);
        }
    }

    *findByTag { |tag|
        ^Library.at(\brstrm, this.asSymbol, \tags, tag)
    }
}

+NodeProxy {

    tags {
        var tags = Library.at(\brstrm, this.class.asSymbol, \tags)
        .select({ arg x, y; x.includes(this) });
        ^tags !? { tags.keys }
    }

    tags_ { arg ...args;
        args = args.flatten;
        this.removeTag(this.tags);
        this.addTag(*args);
    }

    addTag { arg ...args;
        var tags = Library.at(\brstrm, this.class.asSymbol, \tags);
        args = args.collect(_.asSymbol).flatten;
        args.do { arg sym;
            var set = Library.at(\brstrm, this.class.asSymbol, \tags, sym);
            set = set.add(this).asSet;
            Library.put(\brstrm, this.class.asSymbol, \tags, sym, set);
        }
    }

    removeTag { arg ...args;
        args = args.collect(_.asSymbol).flatten;
        args.do { arg sym;
            Library.at(\brstrm, this.class.asSymbol, \tags, sym).remove(this);
        }
    }

    *findByTag { |tag|
        ^Library.at(\brstrm, this.asSymbol, \tags, tag)
    }
}



