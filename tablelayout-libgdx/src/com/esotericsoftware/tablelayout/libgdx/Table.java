
package com.esotericsoftware.tablelayout.libgdx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.esotericsoftware.tablelayout.Cell;
import com.esotericsoftware.tablelayout.TableLayout;

public class Table extends Group {
	public final GdxTableLayout layout;

	public Table () {
		layout = new GdxTableLayout();
		layout.table = this;
	}

	public Table (TableLayout parent) {
		layout = new GdxTableLayout(parent);
		layout.table = this;
	}

	public Actor setName (String name, Actor widget) {
		return layout.setName(name, widget);
	}

	public void parse (String tableDescription) {
		layout.parse(tableDescription);
	}

	public void layout () {
		layout.layout();
	}

	public Actor getWidget (String name) {
		return layout.getWidget(name);
	}

	public List<Actor> getWidgets () {
		return layout.getWidgets();
	}

	public List<Actor> getWidgets (String namePrefix) {
		return layout.getWidgets(namePrefix);
	}

	public List<Cell> getCells (String namePrefix) {
		return layout.getCells(namePrefix);
	}

	public void setWidget (String name, Actor widget) {
		layout.setWidget(name, widget);
	}

	public Cell getCell (String name) {
		return layout.getCell(name);
	}

	public List<Cell> getCells () {
		return layout.getCells();
	}

	public Cell getCell (Actor widget) {
		return layout.getCell(widget);
	}

	/**
	 * This method is needed for the TableLayout to relayout automatically if {@link TableLayout#invalidate()} has been called. If
	 * this method is not called each frame, {@link TableLayout#layout()} must be called manually when the TableLayout is modified.
	 */
	public void update () {
		if (layout.needsLayout) layout.layout();
	}

	/**
	 * This method is needed for the TableLayout to draw the debug lines, when enabled. If this method is not called each frame, no
	 * debug lines will be drawn.
	 */
	public void drawDebug () {
		layout.drawDebug();
	}
}
