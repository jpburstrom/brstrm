//Interfacing with nodeproxy/proxychain

+Pattern {
    playToProxy { |c ... args |
        ^this.buildForProxy(c).play(*args);
    }
    playToPC { |c ... args|
        ^this.buildForProxy(c.proxy).play(*args);
    }
}

+EventPatternProxy {
    playToProxy { |c, argClock, protoEvent, quant, doReset = false|
        protoEvent ?? { protoEvent = () };
        protoEvent.buildForProxy(c);
        ^this.play(argClock, protoEvent, quant, doReset);
    }
    playToPC { |c ... args |
        ^this.playToProxy(c.proxy, *args);
    }
}

