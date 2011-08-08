
package com.esotericsoftware.tablelayout.twl;

import de.matthiasmann.twl.Widget;

public class Stack extends Widget {
	public Stack () {
		setTheme("");
	}

	protected void layout () {
		layoutChildrenFullInnerArea();
	}

	public int getMinWidth () {
		int width = 0;
		for (int i = 0, n = getNumChildren(); i < n; i++)
			width = Math.max(width, getChild(i).getMinWidth());
		return width;
	}

	public int getMinHeight () {
		int height = 0;
		for (int i = 0, n = getNumChildren(); i < n; i++)
			height = Math.max(height, getChild(i).getMinHeight());
		return height;
	}
}
