PFastForward : FilterPattern {
	var <>seconds, <>maxdur;
	*new { arg pattern, seconds, maxdur=1024;
		^super.newCopyArgs(pattern, seconds, maxdur)
	}
	storeArgs { ^[pattern, seconds, maxdur] }
	embedInStream { arg event, cleanup;
		var item, delta, elapsed = 0.0, inevent, first;
		var stream = pattern.asStream;

		if (seconds > maxdur) {
            (this.class.name ++ ": Maximum duration reached. Exiting.").warn;
			^event;
		};
		cleanup ?? { cleanup = EventStreamCleanup.new };
		first = true;
		loop {
			while { (elapsed < seconds) and:  {
							(inevent = stream.next(event.copy)).notNil
			} } {
							elapsed = elapsed + inevent.delta;
			};
			if (first and: { elapsed != seconds and: { (elapsed - seconds) > 0 } } ) {
				first = false;
                (delta: (elapsed - seconds), type: \rest).yield
                //TODO: smth like this, but adjust note dur as well?
                //Event(proto: (delta: (elapsed - seconds)), parent:inevent)).yield;
			};
			inevent = stream.next(event).asEvent ?? { ^event };
			cleanup.update(inevent);

			event = inevent.yield;

		}
	}
}

+Pattern {

    fastForward { arg seconds, maxdur=1024;
        ^PFastForward(this, seconds, maxdur);
    }

    ff { arg seconds, maxdur=1024;
        ^this.fastForward(seconds, maxdur);
    }

}
