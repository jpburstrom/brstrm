Ppatch : FilterPattern {
    var	<patch, <readyForPlay, <envir, <>freeOnCleanup;

	*new { |patch, pattern, freeOnCleanup=true|
        ^super.new(pattern).patch_(patch).freeOnCleanup_(freeOnCleanup);
	}
    patch_ { |p|
        patch = p;
        readyForPlay = false;
    }
    prepareForPlay { |ev|
        var server = Server.default;
        if (ev.notNil) {
            server = ev[\server].asTarget.server;
            if(ev.bus.notNil,{
                if(ev.group.isNil,{
                    server = ev[\bus].server;
                    },{
                        server = ev[\group].server;
                })
                },{
                    server = ev[\group].asGroup.server;
            });
        };

        if (server.serverRunning) {
            if (readyForPlay != true) {
                patch.onReady({
                    readyForPlay = true;
                });
                patch.prepareForPlay;
                patch.asSynthDef.add; //We need this, i think
                envir = (instrument: patch.defName)
                .putAll(patch.asSynthDef.secretDefArgs);
            }
            ^true;
        } {
            "You need to boot your server before playing a Ppatch".error;
            ^false;
        }

    }
	embedInStream { arg inevent;
		var event, cleanup = EventStreamCleanup.new;
		var cleanupFunc = {
            if (freeOnCleanup) {
                this.free;
            };
            readyForPlay = false;
        };
		var stream = pattern.asStream;
		var once = true;

        if (readyForPlay.not and: { this.prepareForPlay(inevent).not } ) {
            ^cleanup.exit(inevent);
        };


		loop {
            inevent.putAll(envir);
			event = stream.next(inevent);
			if(once) {
				cleanup.addFunction(event, { |flag|
					envir.use({ cleanupFunc.value(flag) });
				});
				once = false;
			};
			if (event.isNil) {
				^cleanup.exit(inevent)
			} {
				cleanup.update(event);
			};
			inevent = yield(event);
			if(inevent.isNil) { ^cleanup.exit(event) }
		};
	}

    free {
        patch.free;
    }
}
/*

if(server.serverRunning.not,{
			/*
				the hole here is if you manually stop and then restart the server.
				I have no way of knowing that you did that, so the Instr defs are not cleared */
			server.startAliveThread(0.1,0.4);
			server.waitForBoot({
				if(server.dumpMode != 0,{
					server.stopAliveThread;
				});
				InstrSynthDef.clearCache(server);
				if(server.isLocal,{
					InstrSynthDef.loadCacheFromDir(server);
				});
				this.prPlay(atTime,bus,timeOfRequest);
				nil
			});
		},{
			this.prPlay(atTime,bus,timeOfRequest)
		});
*/