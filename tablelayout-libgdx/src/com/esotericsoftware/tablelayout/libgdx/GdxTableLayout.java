
package com.esotericsoftware.tablelayout.libgdx;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.tablelayout.Cell;
import com.esotericsoftware.tablelayout.TableLayout;
import com.esotericsoftware.tablelayout.Toolkit;

public class GdxTableLayout extends TableLayout<Actor> {
	Table table;
	boolean needsLayout = true;

	private Array<DebugRect> debugRects;
	private ImmediateModeRenderer debugRenderer;

	public GdxTableLayout () {
		super();
	}

	public GdxTableLayout (TableLayout parent) {
		super(parent);
	}

	public Toolkit getToolkit () {
		return GdxToolkit.instance;
	}

	public Actor getWidget (String name) {
		Actor actor = super.getWidget(name);
		if (actor == null) actor = table.findActor(name);
		return actor;
	}

	public void layout () {
		tableLayoutX = (int)table.x;
		tableLayoutY = (int)table.y;
		tableLayoutWidth = (int)table.width;
		tableLayoutHeight = (int)table.height;

		super.layout();

		List<Cell> cells = getCells();
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;
			Actor actor = (Actor)c.widget;
			actor.x = c.widgetX;
			actor.y = c.widgetY;
			actor.width = c.widgetWidth;
			actor.height = c.widgetHeight;
		}
		needsLayout = false;
	}

	public void invalidate () {
		needsLayout = true;
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

	static private class DebugRect extends Rectangle {
		final boolean isCell;

		public DebugRect (boolean isCell, int x, int y, int width, int height) {
			super(x, y, width, height);
			this.isCell = isCell;
		}
	}
}
