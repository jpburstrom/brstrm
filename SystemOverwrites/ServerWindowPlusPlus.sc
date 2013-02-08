+ Server {

	// splitting makeWindow and makeGui, makes sure that makeWindow can be overridden
	// while maintaining the availability of the GUI server window

	calculateViewBounds {
		var width = 288, height = 98, taskBarHeight = 27; // the latter should be in SCWindow
		var keys = set.asArray.collect(_.name).sort;
		^Rect(1280 - width - 5, keys.indexOf(name) * (height + taskBarHeight) + 5, width, height)
	}
	
}