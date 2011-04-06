
package com.esotericsoftware.tablelayout.twl;

import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Widget;

public class Table extends Widget {
	public final TwlTableLayout layout;

	public Table () {
		this(new TwlTableLayout());
	}

	public Table (TwlTableLayout layout) {
		this.layout = layout;
		layout.table = this;
		setTheme("");
	}

	protected void layout () {
		layout.layout();
	}

	public int getMinWidth () {
		return layout.tableMinWidth;
	}

	public int getMinHeight () {
		return layout.tableMinHeight;
	}

	public int getPreferredWidth () {
		return layout.tablePrefWidth;
	}

	public int getPreferredHeight () {
		return layout.tablePrefHeight;
	}

	public void invalidateLayout () {
		super.invalidateLayout();
	}

	protected void paintOverlay (GUI gui) {
		super.paintOverlay(gui);
	}
}
