
package com.esotericsoftware.tablelayout.libgdx;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actors.Label;

public class AddCellTest implements ApplicationListener {
	private Stage stage;
	private Table table;
	private TextureRegion blendDownRegion;

	public void create () {
		BitmapFont font = new BitmapFont(true);
		GdxToolkit.setFont(font);

		stage = new Stage(640, 480, false);
		stage.projection.setToOrtho(0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, 0, 1);

		table = new Table();
		stage.addActor(table);
		table.width = 640;
		table.height = 480;

		table.parse("debug * fill:x expand space:15 align:top");
		table.layout.addCell(new Label(null, font, "cow"));
		table.layout.addCell(new Label(null, font, "meow"));
		table.layout.addCell(new Label(null, font, "moo"));
		table.layout();
	}

	public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30.0f));
		stage.draw();
		Table.drawDebug(stage);
	}

	public void resize (int arg0, int arg1) {
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
