
package com.esotericsoftware.tablelayout.twl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import com.esotericsoftware.tablelayout.TableLayout;
import com.esotericsoftware.tablelayout.Toolkit;

import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.Widget;

public class TwlToolkit extends Toolkit<Widget> {
	static {
		addClassPrefix("de.matthiasmann.twl.");
	}

	static public TwlToolkit instance = new TwlToolkit();

	public TwlToolkit () {
		super(Widget.class);
	}

	public void addChild (Widget parent, Widget child, String layoutString) {
		parent.add(child);
	}

	public void removeChild (Widget parent, Widget child) {
		parent.removeChild(child);
	}

	public TableLayout newTableLayout (TableLayout parent) {
		return new Table(parent).layout;
	}

	public Widget newLabel (String text) {
		return new Label(text);
	}

	public Widget newEmptyWidget () {
		return new Widget();
	}

	public void setTitle (Widget parent, String title) {
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
}
