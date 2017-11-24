/*

Reaper
Funktioner:


*/

ReaperServer {
    var <key, <server, playState;
    classvar <instances, <>defaultHost, <>defaultPort;

    *initClass {
		instances = IdentityDictionary.new;
        defaultHost = "localhost";
        defaultPort = 9999;
	}

	*new { arg key = \default, host="localhost", port=57121;
		var res = this.instances.at(key);
		if(res.isNil) {
            res = super.new.prAdd(key).connect(reconnect:false);
		}
		^res

	}

    connect { arg host, port, reconnect=true;

        if (server.notNil) {
            if (host.isNil) { host = server.hostname };
            if (port.isNil) { port = server.port };
            if (reconnect or: (host != server.hostname or: (port != server.port))) {
                server.free;
            } {
                ^this
            }
        } {
            if (host.isNil) { host = defaultHost };
            if (port.isNil) { port = defaultPort };
        };
        server = NetAddr(host, port);
        CmdPeriod.add(this);
        ^this
    }

    connectDefault {
        ^connect(defaultHost, defaultPort)
    }

    play { arg ts;
        ts ?? { ts = Server.default.latency };
        if (playState != 1) {
            server.sendBundle(ts, [\play]);
            playState = 1;
        }
        ^this
    }

    stop { arg ts = 0;
        //Always stop
        server.sendBundle(ts, [\stop]);
        playState = 0;
        ^this
    }

    time { arg time, ts = 0;
        if (time.isKindOf(Point)) {
            time = (time.x * 60) + time.y
        };
        server.sendBundle(ts, [\time, time]);
        ^this
    }

    send { arg msg, ts;
        server.sendBundle(ts, msg);
        ^this;
    }

    prAdd { arg argKey;
		key = argKey;
		instances.put(argKey, this);
	}

    doOnCmdPeriod {
        this.stop
    }
}

PReaper : Pattern {
    var <>pattern, <>fastForward, <>offset, routineFunc, server;

    *new { arg pattern, fastForward=0, offset=0, key = \default;
        ^super.newCopyArgs(pattern, fastForward, offset).init(key);
	}

    init { arg key;
        if (offset.isKindOf(Point)) {
            offset = this.pointToSeconds(offset)
        };
        if (fastForward.isKindOf(Point)) {
            fastForward = this.pointToSeconds(fastForward)
        };
        server = ReaperServer(key);
    }

    pointToSeconds { arg p;
        ^((p.x * 60) + p.y)
    }

    asStream {
        var latency = Server.default.latency;
        ^Routine.new({ |ev|
            server.play(latency); //TODO: latency??
            server.time(fastForward + offset, latency);
            pattern.ff(fastForward).embedInStream(ev);
            server.stop(latency);
        })
    }

	embedInStream { arg inval; ^routineFunc.value(inval) }

}


+Pattern {
    reap { arg fastForward=0, offset=0, key=\default;
        ^PReaper(this, fastForward, offset, key);
    }
}

/*


a.key
a.port

if (2 == 2 and: 3 == 3) { "test".postln }
Pattern
Stream().asPattern

Prout({ var stream;
        stream = Pbind(\note, Pseries(1, 1, 4)).asStream;
        loop {
        stream.yield
        }
        }).asStream.next(())

*/

/*
ReaperServer() === ReaperServer()

ReaperServer().connect(port:57121)

ReaperServer().server.port

ReaperServer().stop

ReaperServer.instances

Pbind(\note, Pseries(1, 1, 4)).reap(2).play
p.trace.play


a = ReaperServer()

a.play
a.time(20)
[0,0.2].asPoint
1 ?? 2
*/