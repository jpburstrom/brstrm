//Interfacing with an ofxPd patch
//FIXME: some weirdness with phones disconnecting momentarily

MyPhones {

    classvar <>size,
    <status,
    <phones,
    <times,
    <receiver,
    <broadcaster,
    <watcher,
    <>timeout = 5,
    <>port=12345,
    <>defaultURL;

    *initClass {
        defaultURL = NetAddr.localAddr.hostname ++ ":8000/main.pd";
        Event.addEventType(\phone,{ arg server;
            var ev;
            ~server = server; //Telefon
            if (~phone.notNil) {
                ~server = MyPhones.at(~phone)
            };
            ~server !? {
                ~freq = ~detunedFreq.value;
                ~amp = ~amp.value;
                ~sustain = ~sustain.value;
                ~latency = ~latency ? Server.default.latency;
                ev = currentEnvironment.copy.reject( { arg v, k;
                    //Remove client-only stuff
                    [\phone, \type, \server, \instrument, \latency].includes(k)
                });

                ev.asSortedArgsArray.multiChannelExpand.do { |voice|
                    ~server.sendMsg("/pdsched", SystemClock.seconds, ~latency, ~instrument, *voice);
                }
            }
        });
        CmdPeriod.add(this);
        phones = IdentityDictionary();
       times = IdentityDictionary();
     }
    *firstRun {

        broadcaster = SkipJack({
            //"sending".postln;
            NetAddr(NetAddr.broadcast.ip, port).sendMsg("/connect");
        }, 1, { phones.size >= size}, autostart:false);

        watcher = SkipJack({
            times.copy.keysValuesDo { arg k, x;
                if ((SystemClock.seconds - x) > timeout) {
                    phones[k] = nil;
                    times[k] = nil;
                    broadcaster.start;
                }
            };
            //phones.postln;
        }, 1, autostart: false);

        receiver = OSCFunc({ arg msg, time, addr;
            var phone = msg[1];
            //"Receiving".postln;
            //TODO, create netaddr, add port
            if (phones[phone].isNil or: { phones[phone].ip != addr.ip }) {
                phones[phone] = NetAddr(addr.ip, port);
            };
            times[phone] = SystemClock.seconds;
        }, '/ping').permanent_(true);

    }

    *watch { |size=4|
        if (broadcaster.isNil) {
            this.firstRun
        };
        this.size_(size);
        NetAddr.broadcastFlag_(true);
        broadcaster.start;
        watcher.start;
        receiver.enable;
        status = \watching;
    }


    *unwatch {
        broadcaster.stop;
        watcher.stop;
        receiver.disable;
        status = \idle;
    }

    *sendMsg { arg phone, msg ... args;
        if (phone.isNil) {
            phones.do { arg x; x.sendMsg(msg, *args) };
        }  {
            phones[phone] !? (_.sendMsg(msg, *args));
        }

    }

    *reload { |ph| this.sendMsg(ph, "/reload") }
    *open { |path, ph| this.sendMsg(ph, "/open", path) }
    *close { |ph| this.sendMsg(ph, "/close") }
    *vibrate { |ph| this.sendMsg(ph, "/vibrate") }

    *debug { |i| this.sendMsg(nil, "/debug", i) }

    *download { |url, ph|
        url = url ? defaultURL;
        this.sendMsg(ph, "/download", url)
    }

    *at { |k|
        ^phones[k]
    }

    *makeGui { arg p, b = Rect(0, 0, 150, 100);
        var skipjack, old, st, list, w = View(p, b);
        var layout = VLayout(
            st = StaticText(),
            list = ListView().selectionMode_(\none);
        );
        w.layout = layout;
        skipjack = SkipJack({
            st.string_("MyPhones: %".format(status));
            list.enabled = (status == \watching);
            if (phones != old) {
                { list.items = phones.keys.asArray.sort }.defer;
                old = phones.copy;
            }
        }, 1);
        w.onClose_({
            skipjack.stop;
        });
        w.front
    }

    *doOnCmdPeriod {
        this.reload; //Reloading patch on cmd period
    }
}


//x.sendMsg("/reload");
//x.sendMsg("/close");
//x.sendMsg("/download", "http://192.168.1.70:8000/test.pd")