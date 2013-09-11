CueGuiBase : SCViewHolder {
    var fixedHeight = 35,
    background = "000",
    selectBackground = "0A2",
    color = "FFF";

    prLabel { arg st, minWidth;
        ^StaticText()
        .align_(\center)
        .stringColor_(Color.fromHexString(color))
        .background_(Color.fromHexString(background).alpha_(0.5))
        .fixedHeight_(fixedHeight)
        .maxWidth_(100)
        .string_(st)
        .minWidth_(minWidth)
    }
}

CueGui : CueGuiBase {
    var num, cue, <things, timeLabel, stateLabel;

    *new { |num, cue, parent, bounds|
        ^super.new.init(num, cue, parent, bounds);
    }

    init { |n, c, parent, bounds|
        cue = c;
        num = n;
        bounds = bounds ?? { Rect(0,0,600,35) };
        timeLabel = TimeLabel();
        timeLabel.view.align_(\center);
        timeLabel.view
        .background_(Color.fromHexString(background).alpha_(0.5))
        .stringColor_(Color.fromHexString(color))
        .fixedHeight_(fixedHeight);

        things = IdentityDictionary[
            \number -> this.prLabel(num, 35),
            \state -> this.prLabel("[]", 35),
            \name -> this.prLabel(cue.name).maxWidth_(9999),
            \time -> timeLabel,
            \preWait -> this.prLabel(cue.preWait ? ""),
            \dur -> this.prLabel(cue.duration ? "")
        ];

        view = View(nil, bounds).background_(Color.gray);
        view.layout = HLayout().spacing_(0).margins_(0);
        view.layout.add(things[\number], 0);
        view.layout.add(things[\state], 0);
        view.layout.add(things[\name], 4);
        view.layout.add(things[\time].view, 1);
        view.layout.add(things[\preWait], 1);
        view.layout.add(things[\dur], 1);

        cue.addDependant(this);
    }

    update { arg cue, what, args;
        defer {
            if (what == \state) {
                things[\state].string_( IdentityDictionary[
                    \stopped -> "[]",
                    \loading -> "...",
                    \ready -> ":)",
                    \playing -> ">",
                    \paused -> "||",
                    \error -> "!!!"
                ][cue.state]);

                switch (cue.state,
                    \stopped, { things[\time].stop },
                    \playing, { things[\time].play },
                    \paused, { things[\time].pause }
                );
            };

            if (what == \name) {
                things[\name].string = cue.name;
            }
        }
    }

}

CueListGui : CueGuiBase {
    var cueList, canvas, current, <items, <>keyActions;

    *new { |list, parent, bounds|
        ^super.new.init(list, parent, bounds);
    }

    init { | list, parent, bounds |
        var header;
        items = List();

        cueList = list;
        cueList.addDependant(this);

        bounds = bounds ?? { Rect(0, 0, 800, 600) };
        view = ScrollView(parent, bounds);
        canvas = View().maxWidth_(1200);
        canvas.layout = VLayout().margins_(0).spacing_(1);
        canvas.layout.add( this.makeHeader() );
        //TODO: this should be a method
        cueList.do { |item, i|
            var g = CueGui(i + 1, item);
            g.mouseDownAction = { arg ... args;
                cueList.setIndex(i);
            };
            items.add(g);
            canvas.layout.add(g.view)
        };
        canvas.layout.add(nil);
        view.canvas = canvas;

        keyActions = IdentityDictionary[
            Char.space -> { cueList.go; false },
            $V -> { cueList.play; false },
            $S -> { cueList.stop; false },
            $P -> { cueList.pause; false },
            $L -> { cueList.load; false },
            $E -> { cueList[cueList.current].document; false },
            $R -> { cueList.document; false },
            //Esc
            QKey.escape -> { cueList.reset; false },
            //Down
            QKey.down -> { cueList.next; false },
            //UP
            QKey.up -> { cueList.prev; false },
            //Enter
            QKey.return -> {

            }
        ];

        this.keyDownAction = { arg v, c, mod, unicode, kcode, k;
            var current;
            if (k < 128) { k = k.asAscii };
            //Pass key to cue itself,
            //Using a ~keyActions dictionary in its environment
            if (keyActions[k].value != false) {
                current = cueList[cueList.current];
                if (current.notNil) {
                    current[\keyActions] !? { current[\keyActions][k].value};
                }

            }
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
        background = "#866";
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

            current !?  { this.selectItem(false) };
            current = i;
            current !? { this.selectItem };
        }
    }

    selectItem { arg select = true;
        if (select == true) {
            items[current].background = Color.fromHexString(selectBackground)
        } {
            items[current].background = Color.fromHexString(background)
        }
    }


}