
package com.esotericsoftware.tablelayout.libgdx;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actors.Label;

// BOZO - Add support for <> special widget that lays out children same size.

public class AddCellTest implements ApplicationListener {
	private Stage stage;

	public void create () {
		TableLayout.defaultFont = new BitmapFont(true);

		stage = new Stage(640, 480, false);
		stage.projection.setToOrtho(0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, 0, 1);

		TableLayout layout = new Table().layout;
		stage.addActor(layout.getTable());
		layout.getTable().width = 640;
		layout.getTable().height = 480;

		layout.parse("debug * fill:x expand space:15 align:top");
		layout.addCell(new Label(null, TableLayout.defaultFont, "cow"));
		layout.addCell(new Label(null, TableLayout.defaultFont, "meow"));
		layout.addCell(new Label(null, TableLayout.defaultFont, "moo"));
	}

	public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30.0f));
		stage.draw();
		Table.drawDebug(stage);
	}

	public void resize (int width, int height) {
	}

	public void resume () {
	}

	public void pause () {
	}

	public void dispose () {
	}

	public static void main (String[] args) throws Exception {
		new LwjglApplication(new AddCellTest(), "AddCellTest", 640, 480, false);
	}
}
