
package com.esotericsoftware.tablelayout.twl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.esotericsoftware.tablelayout.Cell;
import com.esotericsoftware.tablelayout.TableLayout;

import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Widget;

public class Table extends Widget {
	public final TwlTableLayout layout;

	public Table () {
		layout = new TwlTableLayout();
		layout.table = this;
		setTheme("");
	}

	public Table (TableLayout parent) {
		layout = new TwlTableLayout(parent);
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

	public List<Widget> getWidgets () {
		return layout.getWidgets();
	}

	public List<Widget> getWidgets (String namePrefix) {
		return layout.getWidgets(namePrefix);
	}

	public List<Cell> getCells (String namePrefix) {
		return layout.getCells(namePrefix);
	}

	public void setWidget (String name, Widget Widget) {
		layout.setWidget(name, Widget);
	}

	public Cell getCell (String name) {
		return layout.getCell(name);
	}

	public List<Cell> getCells () {
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
