
package com.esotericsoftware.tablelayout.libgdx;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Layout;
import com.badlogic.gdx.scenes.scene2d.actors.Image;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.tablelayout.BaseTableLayout;
import com.esotericsoftware.tablelayout.Cell;
import com.esotericsoftware.tablelayout.libgdx.LibgdxToolkit.DebugRect;

public class TableLayout extends BaseTableLayout<Actor, Table, LibgdxToolkit> {
	/** The atlas to use to find texture regions. */
	public TextureAtlas atlas;

	boolean needsLayout = true;
	Array<DebugRect> debugRects;
	private ImmediateModeRenderer debugRenderer;

	public TableLayout () {
		super(LibgdxToolkit.instance);
	}

	public TableLayout (LibgdxToolkit toolkit) {
		super(toolkit);
	}

	public void parse (FileHandle file) {
		super.parse(file.readString());
	}

	/** Calls {@link #register(String, Actor)} with the name of the actor. */
	public Actor register (Actor actor) {
		if (actor.name == null) throw new IllegalArgumentException("Actor must have a name: " + actor.getClass());
		return register(actor.name, actor);
	}

	/** Finds the texture region in the {@link #atlas}, creates an {@link Image} and registers it with the specified name. */
	public Actor registerImage (String name) {
		return register(new Image(name, atlas.findRegion(name)));
	}

	public Actor getWidget (String name) {
		Actor actor = super.getWidget(name);
		if (actor == null) actor = getTable().findActor(name);
		return actor;
	}

	public void layout () {
		if (!needsLayout) return;
		needsLayout = false;

		Table table = getTable();
		int height = (int)table.height;
		setLayoutSize(0, 0, (int)table.width, height);

		super.layout();

		List<Cell> cells = getCells();
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.getIgnore()) continue;
			Actor actor = (Actor)c.getWidget();
			actor.x = c.getWidgetX();
			int widgetHeight = c.getWidgetHeight();
			actor.y = height - c.getWidgetY() - widgetHeight;
			actor.width = c.getWidgetWidth();
			actor.height = widgetHeight;
			if (actor instanceof Layout) {
				Layout layout = (Layout)actor;
				layout.invalidate();
				layout.layout();
			}
		}
	}

	public void invalidate () {
		needsLayout = true;
	}

	public void drawDebug () {
		if (getDebug() == DEBUG_NONE || debugRects == null) return;
		if (debugRenderer == null) debugRenderer = new ImmediateModeRenderer(64);

		Actor parent = getTable();
		float x = 0, y = parent.height;
		while (parent != null) {
			if (parent instanceof Table) {
				x += parent.x;
				y += parent.y;
			} else {
				x += parent.x;
				y += parent.y;
			}
			parent = parent.parent;
		}

		int viewHeight = Gdx.graphics.getHeight();

		debugRenderer.begin(GL10.GL_LINES);
		for (int i = 0, n = debugRects.size; i < n; i++) {
			DebugRect rect = debugRects.get(i);
			float x1 = x + rect.x;
			float y1 = y - rect.y - rect.height;
			float x2 = x1 + rect.width;
			float y2 = y1 + rect.height;
			float r = (rect.type & DEBUG_CELL) != 0 ? 1 : 0;
			float g = (rect.type & DEBUG_WIDGET) != 0 ? 1 : 0;
			float b = (rect.type & DEBUG_TABLE) != 0 ? 1 : 0;

			debugRenderer.color(r, g, b, 1);
			debugRenderer.vertex(x1, y1, 0);
			debugRenderer.color(r, g, b, 1);
			debugRenderer.vertex(x1, y2, 0);

			debugRenderer.color(r, g, b, 1);
			debugRenderer.vertex(x1, y2, 0);
			debugRenderer.color(r, g, b, 1);
			debugRenderer.vertex(x2, y2, 0);

			debugRenderer.color(r, g, b, 1);
			debugRenderer.vertex(x2, y2, 0);
			debugRenderer.color(r, g, b, 1);
			debugRenderer.vertex(x2, y1, 0);

			debugRenderer.color(r, g, b, 1);
			debugRenderer.vertex(x2, y1, 0);
			debugRenderer.color(r, g, b, 1);
			debugRenderer.vertex(x1, y1, 0);

			if (debugRenderer.getNumVertices() == 64) {
				debugRenderer.end();
				debugRenderer.begin(GL10.GL_LINES);
			}
		}
		debugRenderer.end();
	}
}
