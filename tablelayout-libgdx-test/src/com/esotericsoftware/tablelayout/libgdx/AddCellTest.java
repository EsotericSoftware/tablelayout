
package com.esotericsoftware.tablelayout.libgdx;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actors.Label;

public class AddCellTest implements ApplicationListener {
	private Stage stage;

	public void create () {
		LibgdxToolkit.defaultFont = new BitmapFont();

		stage = new Stage(640, 480, false);

		Table table = new Table();
		TableLayout layout = table.getTableLayout();
		stage.addActor(layout.getTable());
		layout.getTable().width = 640;
		layout.getTable().height = 480;

		layout.parse("debug * fill:x expand space:15 align:top");
		layout.add(new Label(null, LibgdxToolkit.defaultFont, "cow"));
		layout.add(new Label(null, LibgdxToolkit.defaultFont, "meow"));
		layout.add(new Label(null, LibgdxToolkit.defaultFont, "moo"));
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
