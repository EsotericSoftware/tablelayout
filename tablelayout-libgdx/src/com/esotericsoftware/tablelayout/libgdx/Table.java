
package com.esotericsoftware.tablelayout.libgdx;

import java.util.List;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.esotericsoftware.tablelayout.Cell;
import com.esotericsoftware.tablelayout.BaseTableLayout;

public class Table extends Group {
	public final TableLayout layout;

	public Table () {
		this(new TableLayout());
	}

	public Table (TableLayout layout) {
		this.layout = layout;
		layout.table = this;
	}

	public Table (String name) {
		super(name);
		layout = new TableLayout();
		layout.table = this;
	}

	public Table (String name, TableLayout layout) {
		super(name);
		this.layout = layout;
		layout.table = this;
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
