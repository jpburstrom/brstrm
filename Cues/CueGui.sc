//TODO: Get current state from CueList when creating the window

CueGuiBase : SCViewHolder {
    var <model,
    fixedHeight = 35,
    bgAlpha = 0.5,
    bg = "000",
    selectBg = "0A2",
    color = "FFF";

    view_ { arg v;
		// subclasses need to ALWAYS use this method to set the view
		view = v;
        model.addDependant(this);
        //Attach this to child;
        this.view.onClose = {
            this.viewDidClose;
            model.removeDependant(this);
        };
    }

    prLabel { arg st, minWidth;
        ^StaticText()
        .align_(\left)
        .stringColor_(Color.fromHexString(color))
        .background_(Color.fromHexString(bg).alpha_(0.5))
        .fixedHeight_(fixedHeight)
        .maxWidth_(100)
        .string_(st)
        .minWidth_(minWidth)
    }
    prTimeLabel { arg v;
        var timeLabel = TimeLabel().value_(v);
        timeLabel.view.align_(\center)
        .background_(Color.fromHexString(bg).alpha_(bgAlpha))
        .stringColor_(Color.fromHexString(color))
        .fixedHeight_(fixedHeight);
        ^timeLabel
    }

    setActive { |active = true|
        var b = bg;
        if (active) { b = selectBg } ;
        this.background_(Color.fromHexString(b));
    }

    remove {

        ^super.remove;
    }

}

CueGui : CueGuiBase {
    classvar stateLabels;
    var num, <things, timeLabel, stateLabel;

    *initClass {
        stateLabels = IdentityDictionary[
            \stateless -> "•",
            \stopped -> "◼",
            \loading -> "✇",
            \ready -> "√",
            \playing -> "▶",
            \paused -> "▷",
            \error -> "?",
            \stopping -> "◻"
        ]
    }

    *new { |num, model, parent, bounds|
        ^super.new.init(model, num, parent, bounds);
    }
    init { |amodel, anum, parent, bounds|
        model = amodel; num = anum;
        bounds = bounds ?? { Rect(0,0,600,35) };
        this.view = View(nil, bounds);

        things = IdentityDictionary[
            \number -> this.prLabel(num, 35),
            \state -> this.prLabel(stateLabels[model.state], 35),
            \name -> this.prLabel(model.name).maxWidth_(9999),
            \time -> timeLabel = this.prTimeLabel(nil),
            \preWait -> this.prTimeLabel(model.preWait),
            \dur -> this.prTimeLabel(model.duration)
        ];

        view.layout = HLayout().spacing_(0).margins_(0);
        view.layout.add(things[\number], 0);
        view.layout.add(things[\state], 0);
        view.layout.add(things[\name], 4);
        view.layout.add(things[\time].view, 1);
        view.layout.add(things[\preWait].view, 1);
        view.layout.add(things[\dur].view, 1);

        this.setActive(false); //init color

    }


    update { arg cue, what, args;
        var z = cue.state, n = cue.name;

        defer {
            if (what == \state) {
                things[\state].string_( stateLabels[z]);

                switch (z,
                    \stopped, { things[\time].stop },
                    \playing, { things[\time].play },
                    \paused, { things[\time].pause }
                );
                this.reset;

            };

            if (what == \name) {
                things[\name].string = n;
            }
        }
    }

    reset {
        things[\preWait].value_(model.preWait);
        things[\dur].value_(model.duration);
    }


}

CueListGui : CueGuiBase {
    var canvas, current, <items, <>keyActions, <scroll;

    *new { |model, parent, bounds|
        ^super.new.init(model, parent, bounds);
    }

    populate {
        model.do { |item, i|
            var g = CueGui(i + 1, item);
            g.mouseDownAction = { arg ... args;
                model.setIndex(i);
            };
            items.add(g);
            canvas.layout.add(g.view);
            g.setActive(model.current == i);
        }
    }

    init { | amodel, parent, bounds |
        var header;
        model = amodel;
        current = model.current.max(0);
        items = List();

        bounds = bounds ?? { Rect(0, 0, 800, 600) };
        this.view = View(parent, bounds);
        this.view.keyDownAction_({});
        view.layout = VLayout().margins_(1).spacing_(1);
        view.layout.add( this.makeHeader() );
        scroll = ScrollView(parent, bounds)
        .hasHorizontalScroller_(false).hasVerticalScroller_(false);
        view.layout.add(scroll);

        //Scroll
        canvas = View().maxWidth_(1200);
        canvas.layout = VLayout().margins_(0).spacing_(1);
        this.populate;
        canvas.layout.add( nil );
        scroll.canvas = canvas;
        this.setScroll;
        scroll.keyDownAction_({});

        keyActions = IdentityDictionary[
            $V -> { model.play; false },
            $S -> { model.stop; false },
            $D -> { model.stop(true); false },
            $P -> { model.pause; false },
            $L -> { model.load; false },
            $E -> { model[model.current].document; false },
            $R -> { model.document; false },
            //Down
            $J -> { model.next; false },
            //UP
            $K -> { model.prev; false },
            //Enter
            QKey.return -> {

            };
            //Esc
            QKey.escape -> {
                items.do(_.reset);
                model.reset;
                false
            }
        ];

        this.keyDownAction = { arg v, c, mod, unicode, kcode, k;
            if (k < 128) { k = k.asAscii };
            //Pass key to cue itself,
            //Using a ~keyActions dictionary in its environment
            keyActions[k].value ?? true;
        };

        View.globalKeyDownAction = { arg v, c, mod, unicode, kcode, k;
            var current;
            var actions = IdentityDictionary[
                Char.space -> { model.go; false },

            ];
            if (k < 128) { k = k.asAscii };
            if (actions[k].value != false) {
                current = model[model.current];
                if (current.notNil) {
                    current[\keyActions] !? { current[\keyActions][k].value};
                }

            };
            false;
        };

        //Front if needed
        if(parent.isNil, {
            this.front;
        });

    }

    makeHeader {
        var parent  = View(nil, Rect(0, 0, 600, 35)).background_(Color.gray);
        parent.layout = HLayout().spacing_(0).margins_(0);
        fixedHeight = 20;
        bg = "#444";
        parent.layout.add(this.prLabel("#", 35), 0);
        parent.layout.add(this.prLabel("!", 35), 0);
        parent.layout.add(this.prLabel("Name").maxWidth_(9999), 4);
        parent.layout.add(this.prLabel("Time").maxWidth_(9999), 1);
        parent.layout.add(this.prLabel("Pre"), 1);
        parent.layout.add(this.prLabel("Dur"), 1);
        ^parent

    }

    update { arg what, signal, i;
        if (signal == \current) {
            current !?  { items[current].setActive(false) };
            current = i;
            current !? { items[current].setActive(true) };
            this.setScroll

        }
    }

    setScroll {
        scroll.visibleOrigin = Point(0, items[current].bounds.top - (scroll.bounds.height / 2)) //SPACING
    }

}