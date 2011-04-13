
package com.esotericsoftware.tablelayout.libgdx;

import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Layout;
import com.badlogic.gdx.scenes.scene2d.actors.FastImage;
import com.badlogic.gdx.scenes.scene2d.actors.Label;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.tablelayout.BaseTableLayout;
import com.esotericsoftware.tablelayout.Cell;

public class TableLayout extends BaseTableLayout<Actor> {
	static {
		addClassPrefix("com.badlogic.gdx.scenes.scene2d.");
		addClassPrefix("com.badlogic.gdx.scenes.scene2d.actors.");
	}

	static public BitmapFont defaultFont;
	static private HashMap<String, BitmapFont> fonts = new HashMap();

	Table table;
	boolean needsLayout = true;

	private Array<DebugRect> debugRects;
	private ImmediateModeRenderer debugRenderer;

	public void parse (FileHandle file) {
		super.parse(file.readString());
	}

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
			if (actor instanceof Layout) ((Layout)actor).layout();
		}
		needsLayout = false;
	}

	public Actor wrap (Object object) {
		if (object instanceof String) {
			if (defaultFont == null) throw new IllegalStateException("No default font has been set.");
			return new Label(null, defaultFont, (String)object);
		}
		if (object == null) return new Group();
		if (object instanceof Texture) new FastImage(null, (Texture)object);
		if (object instanceof TextureRegion) new FastImage(null, (TextureRegion)object);
		return super.wrap(object);
	}

	public void setProperty (Actor object, String name, List<String> values) {
		if (object instanceof Label) {
			Label label = ((Label)object);
			String value = values.get(0);
			if (name.equals("type")) {
				if (value.equals("multiline")) {
					label.setMultiLineText(label.text);
					return;
				}
				if (value.equals("wrapped")) {
					HAlignment alignment = HAlignment.LEFT;
					if (values.size() > 1) alignment = HAlignment.valueOf(values.get(1).toUpperCase());
					label.setWrappedText(label.text, alignment);
					return;
				}
				if (value.equals("singleline")) {
					label.setText(label.text);
					return;
				}
			}
			if (name.equals("font")) {
				label.setFont(getFont(value));
				return;
			}
		}
		super.setProperty(object, name, values);
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
		if (actor instanceof Layout) return (int)((Layout)actor).getPrefWidth();
		return (int)actor.width;
	}

	public int getMinHeight (Actor actor) {
		if (actor instanceof Layout) return (int)((Layout)actor).getPrefHeight();
		return (int)actor.height;
	}

	public int getPrefWidth (Actor actor) {
		if (actor instanceof Layout) return (int)((Layout)actor).getPrefWidth();
		return (int)actor.width;
	}

	public int getPrefHeight (Actor actor) {
		if (actor instanceof Layout) return (int)((Layout)actor).getPrefHeight();
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
			if (parent instanceof Table) {
				x += parent.x;
				y += parent.y;
			} else {
				x += parent.x;
				y += parent.y;
			}
			parent = parent.parent;
		}

		debugRenderer.begin(GL10.GL_LINES);
		for (int i = 0, n = debugRects.size; i < n; i++) {
			DebugRect rect = debugRects.get(i);
			float x1 = x + rect.x + 1;
			float y1 = y + rect.y;
			float x2 = x1 + rect.width;
			float y2 = y1 + rect.height;
			float r = rect.type.equals(DEBUG_CELL) ? 1 : 0;
			float g = rect.type.equals(DEBUG_WIDGET) ? 1 : 0;
			float b = rect.type.equals(DEBUG_TABLE) ? 1 : 0;

			debugRenderer.color(r, g, b, 1);
			debugRenderer.vertex(x1, y1, 0);
			debugRenderer.color(r, g, b, 1);
			debugRenderer.vertex(x1, y2 + 1, 0);

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

	public void clearDebugRectangles () {
		if (debugRects != null) debugRects.clear();
	}

	public void addDebugRectangle (String type, int x, int y, int w, int h) {
		if (debugRects == null) debugRects = new Array();
		debugRects.add(new DebugRect(type, x, y, w, h));
	}

	public Table getTable () {
		return table;
	}

	/**
	 * Sets the name of a font.
	 */
	static public void registerFont (String name, BitmapFont font) {
		fonts.put(name, font);
		if (defaultFont == null) defaultFont = font;
	}

	static public BitmapFont getFont (String name) {
		BitmapFont font = fonts.get(name);
		if (font == null) throw new IllegalArgumentException("Font not found: " + name);
		return font;
	}

	class Stack extends Group implements Layout {
		public void layout () {
			for (int i = 0, n = children.size(); i < n; i++) {
				Actor actor = children.get(i);
				actor.width = width;
				actor.height = height;
				if (actor instanceof Layout) ((Layout)actor).layout();
			}
		}

		public float getPrefWidth () {
			float width = 0;
			for (int i = 0, n = children.size(); i < n; i++)
				width = Math.max(width, TableLayout.this.getPrefWidth(children.get(i)));
			return width;
		}

		public float getPrefHeight () {
			float height = 0;
			for (int i = 0, n = children.size(); i < n; i++)
				height = Math.max(height, TableLayout.this.getPrefHeight(children.get(i)));
			return height;
		}
	}

	static private class DebugRect extends Rectangle {
		final String type;

		public DebugRect (String type, int x, int y, int width, int height) {
			super(x, y, width, height);
			this.type = type;
		}
	}
}
