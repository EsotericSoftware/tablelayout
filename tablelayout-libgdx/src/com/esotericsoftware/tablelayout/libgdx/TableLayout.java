
package com.esotericsoftware.tablelayout.libgdx;

import java.awt.Rectangle;

import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actors.Label;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.tablelayout.BaseTableLayout;
import com.esotericsoftware.tablelayout.Cell;

public class TableLayout extends BaseTableLayout<Actor> {
	static {
		addClassPrefix("com.badlogic.gdx.scenes.scene2d.");
		addClassPrefix("com.badlogic.gdx.scenes.scene2d.actors.");
	}

	static public BitmapFont font;

	Table table;
	boolean needsLayout = true;

	private Array<DebugRect> debugRects;
	private ImmediateModeRenderer debugRenderer;

	/**
	 * Calls {@link #register(String, Actor)} with the name of the actor.
	 */
	public Actor register (Actor actor) {
		if (actor.name == null) throw new IllegalArgumentException("Actor must have a name: " + actor.getClass());
		return register(actor.name, actor);
	}

	public Actor getWidget (String name) {
		Actor actor = super.getWidget(name);
		if (actor == null) actor = table.findActor(name);
		return actor;
	}

	public void layout () {
		tableLayoutWidth = (int)table.width;
		tableLayoutHeight = (int)table.height;

		super.layout();

		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;
			Actor actor = (Actor)c.widget;
			actor.x = c.widgetX;
			actor.y = c.widgetY;
			actor.width = c.widgetWidth;
			actor.height = c.widgetHeight;
			if (actor instanceof Table)
				((Table)actor).layout.layout();
			else if (actor instanceof Stack) //
				((Stack)actor).layout();
		}
		needsLayout = false;
	}

	public Actor wrap (Object object) {
		if (object instanceof String) {
			if (font == null) font = new BitmapFont();
			return new Label(null, font, (String)object);
		}
		if (object == null) return new Group();
		return super.wrap(object);
	}

	public BaseTableLayout newTableLayout () {
		TableLayout layout = new Table().layout;
		layout.setParent(this);
		return layout;
	}

	public Actor newStack () {
		return new Stack();
	}

	public void addChild (Actor parent, Actor child, String layoutString) {
		if (child.parent != null) child.remove();
		((Group)parent).addActor(child);
	}

	public void removeChild (Actor parent, Actor child) {
		((Group)parent).removeActor(child);
	}

	public int getMinWidth (Actor actor) {
		return (int)actor.width;
	}

	public int getMinHeight (Actor actor) {
		return (int)actor.height;
	}

	public int getPrefWidth (Actor actor) {
		return (int)actor.width;
	}

	public int getPrefHeight (Actor actor) {
		return (int)actor.height;
	}

	public int getMaxWidth (Actor actor) {
		return 0;
	}

	public int getMaxHeight (Actor actor) {
		return 0;
	}

	public void invalidate () {
		needsLayout = true;
	}

	public void drawDebug () {
		if (debug == null || debugRects == null) return;
		if (debugRenderer == null) debugRenderer = new ImmediateModeRenderer(64);

		int x = 0, y = 0;
		Actor parent = table;
		while (parent != null) {
			x += parent.x;
			y += parent.y;
			parent = parent.parent;
		}

		debugRenderer.begin(GL10.GL_LINES);
		for (int i = 0, n = debugRects.size; i < n; i++) {
			DebugRect rect = debugRects.get(i);
			float x1 = x + rect.x + 1;
			float y1 = y + rect.y;
			float x2 = x1 + rect.width;
			float y2 = y1 + rect.height;
			float r = rect.isCell ? 1 : 0;
			float g = rect.isCell ? 0 : 1;

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

	public void clearDebugRectangles () {
		if (debugRects != null) debugRects.clear();
	}

	public void addDebugRectangle (boolean isCell, int x, int y, int w, int h) {
		if (debugRects == null) debugRects = new Array();
		debugRects.add(new DebugRect(isCell, x, y, w, h));
	}

	public Table getTable () {
		return table;
	}

	static class Stack extends Group {
		public void layout () {
			for (int i = 0, n = children.size(); i < n; i++) {
				Actor actor = children.get(i);
				actor.width = width;
				actor.height = height;
				if (actor instanceof Table)
					((Table)actor).layout.layout();
				else if (actor instanceof Stack) //
					((Stack)actor).layout();
			}
		}
	}

	static private class DebugRect extends Rectangle {
		final boolean isCell;

		public DebugRect (boolean isCell, int x, int y, int width, int height) {
			super(x, y, width, height);
			this.isCell = isCell;
		}
	}
}
