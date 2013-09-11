CueList : List {
    var <current, <list;

    *new { |size|
        ^super.new(size).init;
    }

    *newFrom { arg aCollection;
        var newCollection = this.new(aCollection.size);
		aCollection.do {| item | newCollection.add(item) };
        ^newCollection;

    }

    init {
        current = -1;
    }

    put { arg i, item;
        if (this.prprCheckItem(item)) {
            array.put(i, item)
        }
    }
	clipPut { arg i, item; i = i.asInteger.clip(0, this.size - 1); this.put(i, item);}
	wrapPut { arg i, item; i = i.asInteger.wrap(0, this.size - 1); this.put(i, item);  }
	foldPut { arg i, item; i = i.asInteger.fold(0, this.size - 1); this.put(i, item) }

	add { arg item;
        if (this.prCheckItem(item)) {
            array = array.add(item);
            this.changed(\items)
        }
    }
	addFirst { arg item;
        if (this.prCheckItem(item)) {
            array = array.addFirst(item);
            this.changed(\items)
        }
    }
    insert { arg index, item;
        if (this.prCheckItem(item)) {
            array = array.insert(index, item);
            this.changed(\items)
        }
    }
	removeAt { arg index;
        array.removeAt(index);
        this.changed(\items);
    }
	pop { array.pop; this.changed(\items) }
	first { if (this.size > 0, { ^array.at(0) }, { ^nil }) }
	last { if (this.size > 0, { ^array.at(this.size - 1) }, { ^nil }) }
	fill { arg item; array.fill(item); this.changed(\items) }

    go {
        this.play;
        this.next;
        this.load;
    }

    play { this.performOnCue(\play) }

    load { this.performOnCue(\load) }

    stop { this.performOnCue(\stop) }

    pause { this.performOnCue(\pause) }

    stopAll { this.do(_.stop) }

    performOnCue { |what|
        if (this.size > 0) {
            current !? {
                this.at(current) !? {
                    this.at(current).perform(what);
                }
            }
        }
    }

    next {
        ^this.prPrevNext(1)
    }

    prev {
        ^this.prPrevNext(-1)
    }

    setIndex { |index|
        current = index.wrap(0, this.size - 1);
        this.changed(\current, current);
        ^this.at(current);
    }
    setFirst { this.setIndex(0) }

    setLast { this.setIndex(this.size - 1) }

    reset {
        this.stopAll;
        this.setIndex(0);
        this.load;
    }

    prPrevNext { |dir|
        if (current.isNil) {
            if (this.size > 0) {
                ^this.setIndex(0);
            } {
                ^nil;
            }
        };
        ^this.setIndex(current + dir);
    }

    prCheckItem { |item|
        var test = item.isKindOf(AbstractCue);
        if (test == false) { "CueList needs AbstractCues".warn }
        ^test
    }


}
