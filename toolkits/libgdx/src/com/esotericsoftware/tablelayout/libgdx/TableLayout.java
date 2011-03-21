
package com.esotericsoftware.tablelayout.libgdx;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actors.Label;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.esotericsoftware.tablelayout.BaseTableLayout;

public class TableLayout extends BaseTableLayout<Actor> {
	static {
		addClassPrefix("com.badlogic.gdx.scenes.scene2d.");
		addClassPrefix("com.badlogic.gdx.scenes.scene2d.actors.");
	}

	public Group group;
	public BitmapFont font;

	private Array<DebugRect> debugRects;
	private ImmediateModeRenderer debugRenderer;

	public TableLayout (Group group) {
		this.group = group;
	}

	public TableLayout (Group group, BitmapFont font) {
		this.group = group;
		this.font = font;
	}

	public TableLayout (Group group, BitmapFont font, String tableText) {
		super(tableText);
		this.group = group;
		this.font = font;
	}

	private TableLayout (TableLayout parent) {
		super(parent);
		this.group = parent.group;
		this.font = parent.font;
	}

	public Actor getWidget (String name) {
		Actor actor = super.getWidget(name);
		if (actor == null) actor = group.findActor(name);
		return actor;
	}

	public void layout () {
		tableLayoutX = (int)group.x;
		tableLayoutY = (int)group.y;
		tableLayoutWidth = (int)group.width;
		tableLayoutHeight = (int)group.height;
		ArrayList<Cell> cells = getCells();
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;
			Actor actor = (Actor)c.widget;
			if (actor.parent == null)
				group.addActor(actor);
			else if (actor.parent != group) //
				throw new IllegalStateException("Actor has wrong parent: " + actor);
		}
		super.layout();
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;
			Actor actor = (Actor)c.widget;
			actor.x = c.widgetX;
			actor.y = c.widgetY;
			actor.width = c.widgetWidth;
			actor.height = c.widgetHeight;
		}
	}

	public void drawDebug () {
		if (debug == null || debugRects == null) return;
		if (debugRenderer == null) debugRenderer = new ImmediateModeRenderer(64);
		debugRenderer.begin(GL10.GL_LINES);
		for (int i = 0, n = debugRects.size; i < n; i++) {
			DebugRect rect = debugRects.get(i);
			float x1 = rect.x + 1;
			float y1 = rect.y;
			float x2 = x1 + rect.width;
			float y2 = y1 + rect.height;
			float r = rect.dash ? 0 : 1;
			float g = rect.dash ? 1 : 0;

			debugRenderer.color(r, g, 0, 1);
			debugRenderer.vertex(x1, y1, 0);
			debugRenderer.color(r, g, 0, 1);
			debugRenderer.vertex(x1, y2 + 1, 0);

			debugRenderer.color(r, g, 0, 1);
			debugRenderer.vertex(x1, y2, 0);
			debugRenderer.color(r, g, 0, 1);
			debugRenderer.vertex(x2, y2, 0);

			debugRenderer.color(r, g, 0, 1);
			debugRenderer.vertex(x2, y2, 0);
			debugRenderer.color(r, g, 0, 1);
			debugRenderer.vertex(x2, y1, 0);

			debugRenderer.color(r, g, 0, 1);
			debugRenderer.vertex(x2, y1, 0);
			debugRenderer.color(r, g, 0, 1);
			debugRenderer.vertex(x1, y1, 0);

			if (debugRenderer.getNumVertices() == 64) {
				debugRenderer.end();
				debugRenderer.begin(GL10.GL_LINES);
			}
		}
		debugRenderer.end();
	}

	protected TableLayout newTableLayout () {
		return new TableLayout(this);
	}

	protected Label newLabel (String text) {
		if (font == null) throw new GdxRuntimeException("Cannot add label, no font specified in TableLayout constructor.");
		return new Label(null, font, text);
	}

	protected void setTitle (Actor parent, String title) {
		// BOZO - Add title for libgdx?
	}

	protected void addChild (Actor parent, Actor child, String layoutString) {
		((Group)parent).addActor(child);
	}

	protected Actor wrap (Object object) {
		if (object instanceof Actor) return (Actor)object;
		if (object instanceof String) return newLabel((String)object);
		if (object == null) return new Group(null);
		throw new IllegalArgumentException("Unknown object: " + object);
	}

	protected int getMinWidth (Actor actor) {
		return (int)actor.width;
	}

	protected int getMinHeight (Actor actor) {
		return (int)actor.height;
	}

	protected int getPrefWidth (Actor actor) {
		return (int)actor.width;
	}

	protected int getPrefHeight (Actor actor) {
		return (int)actor.height;
	}

	protected int getMaxWidth (Actor actor) {
		return 0;
	}

	protected int getMaxHeight (Actor actor) {
		return 0;
	}

	protected TableLayout getTableLayout (Object object) {
		if (object instanceof TableLayout) return (TableLayout)object;
		return null;
	}

	protected void drawDebugRect (boolean dash, int x, int y, int w, int h) {
		if (debugRects == null) debugRects = new Array();
		debugRects.add(new DebugRect(dash, x, y, w, h));
	}

	public Group getGroup () {
		return group;
	}

	static private class DebugRect extends Rectangle {
		final boolean dash;

		public DebugRect (boolean dash, int x, int y, int width, int height) {
			super(x, y, width, height);
			this.dash = dash;
		}
	}
}