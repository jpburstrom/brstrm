
TagWindow {

    var <window;

    *new {
        ^super.new.init();
    }

    init {
        var lay = ScrollView();
        var classes = [\Pdef, \Tdef, \Pdefn, \Ndef];
        var pp = (cls: PopUpMenu().items_([\Pdef, \Tdef, \Pdefn, \Ndef]), tag: PopUpMenu());
        pp.cls.action_ { arg pop;
            var tags = Library.at(\brstrm, pop.item.asSymbol, \tags);
            tags = [\All] ++ (tags !? { tags.keys }).asArray;
            pp.tag.items_(tags).valueAction_(0);
        };
        pp.tag.action_ { arg pop;
            var list, tag = pop.item.asSymbol;
            if (tag == \All) {
                list = pp.cls.item.asClass.perform(\all).asArray;
            } {
                list = pp.cls.item.asClass.perform(\findByTag, tag).asArray;
            };
            lay.removeAll;
            lay.decorator.reset;
            list.sort({ arg x, y; x.key > y.key}).do { |x|
                (pp.cls.item ++ "Gui").asSymbol.asClass.perform(\new, x, 0, lay, makeSkip:false);
                DragSource(lay, Rect(0, 0, 15, 20)).object_(x).string_("•");
            };
        };
        lay.addFlowLayout.gap_(1@1);
        window = Window("Search by tags", Rect(Window.availableBounds.width - 355, 200, 305, 414), false, scroll:true).front;
        window.layout_(VLayout(HLayout(pp.cls, pp.tag), lay));
        pp.cls.valueAction_(0);

    }

    close {
        window.close;
    }

}
