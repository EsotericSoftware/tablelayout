
package com.esotericsoftware.tablelayout.libgdx;

import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Layout;
import com.badlogic.gdx.scenes.scene2d.actors.Button;
import com.badlogic.gdx.scenes.scene2d.actors.Image;
import com.badlogic.gdx.scenes.scene2d.actors.Label;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.tablelayout.Toolkit;

public class LibgdxToolkit extends Toolkit<Actor, Table, TableLayout> {
	static {
		addClassPrefix("com.badlogic.gdx.scenes.scene2d.");
		addClassPrefix("com.badlogic.gdx.scenes.scene2d.actors.");
	}

	static public LibgdxToolkit instance = new LibgdxToolkit();
	static public BitmapFont defaultFont;
	static private HashMap<String, BitmapFont> fonts = new HashMap();

	public Actor wrap (Object object) {
		if (object instanceof String) {
			if (defaultFont == null) throw new IllegalStateException("No default font has been set.");
			return new Label(null, defaultFont, (String)object);
		}
		if (object == null) return new Group();
		return super.wrap(object);
	}

	public Actor newWidget (TableLayout layout, String className) {
		if (layout.atlas != null) {
			AtlasRegion region = layout.atlas.findRegion(className);
			if (region != null) return new Image(className, region);
		}
		if (className.equals("button")) return new Button(null);
		return super.newWidget(layout, className);
	}

	public TableLayout getLayout (Table table) {
		return table.getTableLayout();
	}

	public Actor newStack () {
		return new Stack();
	}

	public void setProperty (TableLayout layout, Actor object, String name, List<String> values) {
		if (object instanceof Label) {
			Label label = ((Label)object);
			String value = values.get(0);
			if (name.equals("font")) {
				label.setFont(getFont(value));
				return;
			}
		}

		super.setProperty(layout, object, name, values);
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

	public void clearDebugRectangles (TableLayout layout) {
		if (layout.debugRects != null) layout.debugRects.clear();
	}

	public void addDebugRectangle (TableLayout layout, int type, int x, int y, int w, int h) {
		if (layout.debugRects == null) layout.debugRects = new Array();
		layout.debugRects.add(new DebugRect(type, x, y, w, h));
	}

	/** Sets the name of a font. */
	static public void registerFont (String name, BitmapFont font) {
		fonts.put(name, font);
		if (defaultFont == null) defaultFont = font;
	}

	static public BitmapFont getFont (String name) {
		BitmapFont font = fonts.get(name);
		if (font == null) throw new IllegalArgumentException("Font not found: " + name);
		return font;
	}

	static class DebugRect extends Rectangle {
		final int type;

		public DebugRect (int type, int x, int y, int width, int height) {
			super(x, y, width, height);
			this.type = type;
		}
	}
}
