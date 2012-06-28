
package com.esotericsoftware.tablelayout.twl;

import com.esotericsoftware.tablelayout.Cell;

import de.matthiasmann.twl.Widget;

public class Table extends Widget {
	public final TableLayout layout;

	public Table () {
		this(new TableLayout());
	}

	public Table (TableLayout layout) {
		this.layout = layout;
		layout.setTable(this);
		setTheme("");
	}

	public Cell addCell (Widget widget) {
		return layout.add(widget);
	}

	public Cell row () {
		return layout.row();
	}

	public Cell columnDefaults (int column) {
		return layout.columnDefaults(column);
	}

	public Cell defaults () {
		return layout.defaults();
	}

	protected void layout () {
		layout.layout();
	}

	public int getMinWidth () {
		return (int)layout.getMinWidth();
	}

	public int getMinHeight () {
		return (int)layout.getMinHeight();
	}

	public int getPreferredWidth () {
		return (int)layout.getPrefWidth();
	}

	public int getPreferredHeight () {
		return (int)layout.getPrefHeight();
	}

	public void invalidateLayout () {
		super.invalidateLayout();
	}
}
