+ MixerPresendWidget {
    makeView { |layout, bounds|
        var slbounds = this.getSliderBounds(bounds);
        var mbounds = this.getMenuBounds(bounds);
        //Create a container for the popup
        var ppcontainer = View(layout, mbounds);
        slider = GUI.slider.new(layout, slbounds)
        .thumbSize_(min(16, slbounds.width * 0.2))
        .action_({ |view| this.doSliderAction(view) });
        //Making popup wider than container
        menu = GUI.popUpMenu.new(ppcontainer, Rect().height_(20).width_(mbounds.width + 18))
        .action_({ |view| this.doMenuAction(view) })
        .items_(gui.menuItems)
        .value_(gui.menuItems.size-1)
        .font_(this.class.font);
        oldValue = menu.value;
    }

    refresh { |bounds|
        slider.bounds = this.getSliderBounds(bounds);
        //Setting bounds of parent container
        menu.parent.bounds = this.getMenuBounds(bounds);
        this.updateMenu;
        this.update;
    }


}

+ MixerOutbusWidget {
    makeView { |layout, bounds|
        var ppcontainer = View(layout, bounds);
        view = GUI.popUpMenu.new(ppcontainer, Rect().height_(20).width_(bounds.width + 18))
        .items_(gui.menuItems)
        .value_(gui.menuItems.size-1)
        .font_(this.class.font);
    }
	refresh { |bounds|
		view.parent.bounds = bounds;
		mixer.notNil.if({
			this.restoreView;
			this.update;
		}, {
			view.notClosed.if({ this.clearView; });
		});
	}
}