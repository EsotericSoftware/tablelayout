
package com.esotericsoftware.tablelayout.libgdx;

import java.util.List;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Layout;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class Table extends Group implements Layout {
	private final TableLayout layout;

	public Table () {
		this(null, new TableLayout());
	}

	public Table (TableLayout layout) {
		this(null, layout);
	}

	public Table (String name) {
		this(name, new TableLayout());
	}

	public Table (String name, TableLayout layout) {
		super(name);
		this.layout = layout;
		layout.setTable(this);
	}

	protected void draw (SpriteBatch batch, float parentAlpha) {
		if (layout.needsLayout) layout.layout();
		super.draw(batch, parentAlpha);
	}

	public void layout () {
		layout.layout();
	}

	public void invalidate () {
		layout.invalidate();
	}

	public float getPrefWidth () {
		layout.setLayoutSize(0, 0, 0, 0);
		layout.layout();
		return layout.getMinWidth();
	}

	public float getPrefHeight () {
		layout.setLayoutSize(0, 0, 0, 0);
		layout.layout();
		return layout.getMinHeight();
	}

	/** Draws the debug lines for all TableLayouts in the stage. If this method is not called each frame, no debug lines will be
	 * drawn. */
	static public void drawDebug (Stage stage) {
		drawDebug(stage.getActors());
	}

	static private void drawDebug (List<Actor> actors) {
		for (int i = 0, n = actors.size(); i < n; i++) {
			Actor actor = actors.get(i);
			if (actor instanceof Table) ((Table)actor).layout.drawDebug();
			if (actor instanceof Group) drawDebug(((Group)actor).getActors());
		}
	}

	public TableLayout getTableLayout () {
		return layout;
	}
}
