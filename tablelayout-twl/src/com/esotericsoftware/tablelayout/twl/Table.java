
package com.esotericsoftware.tablelayout.twl;

import java.util.ArrayList;

import com.esotericsoftware.tablelayout.Cell;

import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Widget;

public class Table extends Widget {
	public final TwlTableLayout layout = new TwlTableLayout();

	public Table () {
		layout.table = this;
		setTheme("");
	}

	public Widget setName (String name, Widget widget) {
		return layout.setName(name, widget);
	}

	public void parse (String tableDescription) {
		layout.parse(tableDescription);
	}

	public Widget getWidget (String name) {
		return layout.getWidget(name);
	}

	public void setWidget (String name, Widget Widget) {
		layout.setWidget(name, Widget);
	}

	public Cell getCell (String name) {
		return layout.getCell(name);
	}

	public ArrayList<Cell> getCells () {
		return layout.getCells();
	}

	public Cell getCell (Widget widget) {
		return layout.getCell(widget);
	}

	protected void layout () {
		layout.layout();
	}

	public int getMinWidth () {
		return layout.totalMinWidth;
	}

	public int getMinHeight () {
		return layout.totalMinHeight;
	}

	public int getPreferredWidth () {
		return layout.totalPrefWidth;
	}

	public int getPreferredHeight () {
		return layout.totalPrefHeight;
	}

	public void invalidateLayout () {
		super.invalidateLayout();
	}

	protected void paintOverlay (GUI gui) {
		super.paintOverlay(gui);
	}
}
