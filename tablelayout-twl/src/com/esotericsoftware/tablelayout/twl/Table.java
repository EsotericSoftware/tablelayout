
package com.esotericsoftware.tablelayout.twl;

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

	protected void layout () {
		layout.layout();
	}

	public int getMinWidth () {
		return layout.getMinWidth();
	}

	public int getMinHeight () {
		return layout.getMinHeight();
	}

	public int getPreferredWidth () {
		return layout.getPrefWidth();
	}

	public int getPreferredHeight () {
		return layout.getPrefHeight();
	}

	public void invalidateLayout () {
		super.invalidateLayout();
	}
}
