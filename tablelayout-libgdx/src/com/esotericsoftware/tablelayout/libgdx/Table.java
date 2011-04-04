
package com.esotericsoftware.tablelayout.libgdx;

import java.util.List;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.esotericsoftware.tablelayout.Cell;
import com.esotericsoftware.tablelayout.TableLayout;

public class Table extends Group {
	public final GdxTableLayout layout;

	public Table () {
		layout = new GdxTableLayout();
		layout.table = this;
	}

	public Table (String name) {
		super(name);
		layout = new GdxTableLayout();
		layout.table = this;
	}

	public Table (TableLayout parent) {
		layout = new GdxTableLayout(parent);
		layout.table = this;
	}

	public Actor setName (String name, Actor actor) {
		return layout.setName(name, actor);
	}

	/**
	 * Calls {@link #setName(String, Actor)} with the name of the actor.
	 */
	public Actor add (Actor actor) {
		if (actor.name == null) throw new IllegalArgumentException("Actor must have a name: " + actor.getClass());
		return layout.setName(actor.name, actor);
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

	public void setWidget (String name, Actor actor) {
		layout.setWidget(name, actor);
	}

	public Cell getCell (String name) {
		return layout.getCell(name);
	}

	public List<Cell> getCells () {
		return layout.getCells();
	}

	public Cell getCell (Actor actor) {
		return layout.getCell(actor);
	}

	protected void draw (SpriteBatch batch, float parentAlpha) {
		if (layout.needsLayout) layout.layout();
		super.draw(batch, parentAlpha);
	}

	/**
	 * Draws the debug lines for all TableLayouts in the stage. If this method is not called each frame, no debug lines will be
	 * drawn.
	 */
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
}
