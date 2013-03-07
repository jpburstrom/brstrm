Toolbar {
    classvar instance;
    var b, w, h, <win, <tools, <container;

    *new {
        if (instance.isNil) {
            instance = super.new.init;
        }
        ^instance
    }

    *doesNotUnderstand { |sel...args|
        if (instance.isNil) {
            instance = super.new.init;
        }
        ^instance.performList(sel, args);
    }

    init {
        b = Window.screenBounds;
        w = 50;
        h = b.height;
        tools = IdentityDictionary[];
        this.makeWindow;
        ^this
    }

    makeWindow {
        var btn;
        if (win.notNil) {
            win.close()
        };
        btn = View().layout_(HLayout(nil,
            Button()
            .states_([["…", Color.white, Color.black], ["√", Color.green, Color.black]])
            .action_( e { arg b;
                if (b.value == 0) {
                    this.minimize(false);
                } {
                    this.unminimize(false);
                }
            }).maxSize_(22@22).value_(1);
        ).margins_(0).spacing_(0)).background_(Color.gray);
        container = View().layout_(VLayout(nil).margins_(0).spacing_(0));
        win = Window("Toolbar", Rect(b.width - w, b.height - h, w, h), false, false)
        .background_(Color.fromHexString("111111"))
        .alwaysOnTop_(true)
        .layout_(VLayout(btn, container).margins_(0).spacing_(0))
        .front;
        this.minimize();
    }

    add { arg name, func, tgl=false;
        if (win.isNil) {
            this.makeWindow;
        };
        name = this.prCheckName(name);
        ^this.prAdd(name, func, tgl);
    }

    replace { arg name, func, tgl=false;
        name = this.prCheckName(name, func);
        this.remove(name[0]);
        ^this.prAdd(name, func, tgl);

    }

    prCheckName { arg name, func;
        if (name.isArray.not or: { name.isString } ) {
            if (func.notNil) {
                name = [name, Color.black, Color.white];
            } {
                name = [name, Color.white, Color.black];
            }
        };
        name[0] = name[0].asSymbol;
        ^name
    }

    prAdd { arg name, func, tgl;
        var b, index, states;
        name = this.prCheckName(name);
        if (tools[name[0]].notNil) {
            ^nil
        };
        index = tools.size;
        tools[name[0]] = [index, name, func, tgl];

        states = [name];
        if (tgl) {
            states = states ++ [[name[0], name[2], name[1]]]
        };

        if (func.isNil) {
            container.layout.insert (
                b = StaticText().string_(name[0]).stringColor_(name[1]).font_(Font(size:10))
                .background_(name[2]).align_(\center).minHeight_(24), index
            );
        } {
            container.layout.insert (
                b = Button().states_(states).action_(func).font_(Font(size:10))
                , index
            );
        };
        ^b
    }



    rebuild {
        win.close();
        this.makeWindow();
        this.rebuildButtons();

    }

    rebuildButtons {
        var t = tools.copy.asArray.sort { arg x, y; x[0] < y[0] };
        container.removeAll;
        tools.clear;
        t.do { |y| this.add(y[1], y[2], y[3]) };
        IdentityDictionary
    }

    remove { arg name;
        name = name.asSymbol;
        if (tools[name].notNil) {
            tools[name] = nil;
            this.rebuildButtons();
        }
    }


    removeAll {
        tools.clear;
        this.rebuildButtons();
    }

    show  {
        win.front;
    }

    hide  {
        win.visible = false;
    }

    close  {
        win.close;
    }

    minimize  { arg setButton = true;
        var tw = 23, th = 23;
        container.visible = false;
        //Small square
        win.bounds = Rect(b.width - tw, b.height - th, tw, th);
        if (setButton) { win.view.children[0].children[0].value = 0 }
    }

    unminimize  { arg setButton = true;
        container.visible = true;
        win.bounds = Rect(b.width - w, b.height - h, w, h);
        if (setButton) { win.view.children[0].children[0].value = 1 }
    }

}
