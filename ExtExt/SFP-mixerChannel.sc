+AbstractSFP {
    playToMixer { arg m, atTime = nil, callback;
		var	bundle = MixedBundle.new,
			timeOfRequest = Main.elapsedTime;
		this.group = m.synthgroup;
			// generate synthdef to check numChannels
		//this.loadDefFileToBundle(bundle, m.server);
		if(this.numChannels > m.inChannels) {
			"Playing a %-channel patch on a %-input mixer. Output may be incorrect."
				.format(this.numChannels, m.inChannels).warn;
		};
		this.prPlayToBundle(atTime,
			SharedBus(this.rate, m.inbus.index, this.numChannels, m.server),
			timeOfRequest, bundle);
		callback !? { bundle.addFunction(callback) };
		bundle.sendAtTime(this.server, atTime, timeOfRequest);
	}

}


