//HACK
PdropDo : FilterPattern {
	var <>count, <>callback;
	*new { arg count, pattern, callback;
		^super.new(pattern).count_(count).callback_(callback)
	}
	storeArgs { ^[count,pattern,callback] }
	embedInStream { arg event;
		var inevent, totalDelta=0;
		var stream = pattern.asStream;

		count.value(event).do {
			inevent = stream.next(event);
			if (inevent.isNil, { ^event });
			inevent.delta !? { totalDelta = totalDelta + inevent.delta };
		};
		callback.value(totalDelta, inevent);
		loop {
			inevent = stream.next(event);
			if (inevent.isNil, { ^event });
			event = inevent.yield;
		};
	}
}

+ Pattern {
	dropDo { arg n, cb; ^PdropDo(n, this, cb) }
}

