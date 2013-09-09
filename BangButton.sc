BangButton {
    var fg, bg, text, <button, <>time;

    *new { arg parent, bounds, fg, bg, text;
        fg = fg ?? Color.white;
        bg = bg ?? Color.black;
        text = text ?? "";
        ^super.newCopyArgs(fg, bg, text).init(parent, bounds);
    }

    init { arg parent, bounds;
        time = 0.1;
        button = Button(parent, bounds).mouseDownAction_ { this.bang };
        this.remakeStates();

    }

    remakeStates {
        button.states_([[text, fg, bg], [text, bg, fg]]);
    }

    text_ { |t|
        text = t;
        this.remakeStates;
    }

    color_ { |color|
        fg = color;
        this.remakeStates;
    }

    background_ { |color|
        bg = color;
        this.remakeStates;
    }

    bang {
        {
            button.valueAction = 1;
            time.wait;
            button.valueAction = 0;
        }.fork(AppClock);
        ^true
    }

}

