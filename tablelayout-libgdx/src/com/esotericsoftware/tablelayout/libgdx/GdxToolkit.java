
package com.esotericsoftware.tablelayout.libgdx;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actors.Label;
import com.esotericsoftware.tablelayout.TableLayout;
import com.esotericsoftware.tablelayout.Toolkit;

public class GdxToolkit extends Toolkit<Actor> {
	static {
		addClassPrefix("com.badlogic.gdx.scenes.scene2d.");
		addClassPrefix("com.badlogic.gdx.scenes.scene2d.actors.");
	}

	static public GdxToolkit instance = new GdxToolkit();

	private BitmapFont font;

	public GdxToolkit () {
		super(Actor.class);
	}

	public void addChild (Actor parent, Actor child, String layoutString) {
		((Group)parent).addActor(child);
	}

	public void removeChild (Actor parent, Actor child) {
		((Group)parent).removeActor(child);
	}

	public TableLayout newTableLayout () {
		Table table = new Table();
		return table.layout;
	}

	public Actor newLabel (String text) {
		if (font == null) font = new BitmapFont();
		return new Label(null, font, text);
	}

	public Actor newEmptyWidget () {
		return new Group(null);
	}

	public void setTitle (Actor parent, String title) {
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

	static public void setFont (BitmapFont font) {
		instance.font = font;
	}
}
