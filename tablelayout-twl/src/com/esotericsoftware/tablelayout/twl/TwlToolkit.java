
package com.esotericsoftware.tablelayout.twl;

import com.esotericsoftware.tablelayout.Toolkit;

import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.Widget;

public class TwlToolkit extends Toolkit<Widget, Table, TableLayout> {
	static {
		addClassPrefix("de.matthiasmann.twl.");
	}

	static public final TwlToolkit instance = new TwlToolkit();

	public Table newTable (Table parent) {
		return new Table();
	}

	public TableLayout getLayout (Table table) {
		return table.layout;
	}

	public void addChild (Widget parent, Widget child, String layoutString) {
		parent.add(child);
	}

	public void removeChild (Widget parent, Widget child) {
		parent.removeChild(child);
	}

	public Widget wrap (TableLayout layout, Object object) {
		if (object instanceof String) return new Label((String)object);
		if (object == null) return new Widget();
		return super.wrap(layout, object);
	}

	public Widget newStack () {
		return new Stack();
	}

	public int getMinWidth (Widget widget) {
		return widget.getMinWidth();
	}

	public int getMinHeight (Widget widget) {
		return widget.getMinHeight();
	}

	public int getPrefWidth (Widget widget) {
		return widget.getPreferredWidth();
	}

	public int getPrefHeight (Widget widget) {
		return widget.getPreferredHeight();
	}

	public int getMaxWidth (Widget widget) {
		return widget.getMaxWidth();
	}

	public int getMaxHeight (Widget widget) {
		return widget.getMaxHeight();
	}

	public void clearDebugRectangles (TableLayout layout) {
	}

	public void addDebugRectangle (TableLayout layout, int type, int x, int y, int w, int h) {
	}
}
