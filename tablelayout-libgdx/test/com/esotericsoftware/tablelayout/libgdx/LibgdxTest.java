
package com.esotericsoftware.tablelayout.libgdx;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Delay;
import com.badlogic.gdx.scenes.scene2d.actions.FadeIn;
import com.badlogic.gdx.scenes.scene2d.actions.FadeOut;
import com.badlogic.gdx.scenes.scene2d.actions.Forever;
import com.badlogic.gdx.scenes.scene2d.actions.MoveBy;
import com.badlogic.gdx.scenes.scene2d.actions.Parallel;
import com.badlogic.gdx.scenes.scene2d.actions.RotateTo;
import com.badlogic.gdx.scenes.scene2d.actions.ScaleTo;
import com.badlogic.gdx.scenes.scene2d.actions.Sequence;
import com.badlogic.gdx.scenes.scene2d.actors.Image;

public class LibgdxTest implements ApplicationListener {
	private Stage stage;

	public void create () {
		TableLayout.font = new BitmapFont(true);

		stage = new Stage(640, 480, false);
		stage.projection.setToOrtho(0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, 0, 1);

		TableLayout layout = new Table().layout;
		stage.addActor(layout.getTable());
		layout.getTable().width = 640;
		layout.getTable().height = 480;

		Texture badlogic = new Texture(Gdx.files.internal("badlogic.jpg"));
		TextureRegion badlogicRegion = new TextureRegion(badlogic, 0, 0, 256, 256);
		badlogicRegion.flip(false, true);

		Image image1 = new Image("image1", badlogicRegion);
		image1.width = image1.height = 128;
		image1.originX = image1.originY = 64;
		image1.action(Sequence.$(FadeOut.$(1), FadeIn.$(1), ScaleTo.$(0.5f, 0.5f, 1), FadeOut.$(0.5f),
			Delay.$(Parallel.$(RotateTo.$(360, 1), FadeIn.$(1), ScaleTo.$(1, 1, 1)), 1)));
		layout.register(image1);

		Image image2 = new Image("image2", badlogicRegion);
		image2.width = image2.height = 64;
		image2.originX = image2.originY = 32;
		image2.action(Forever.$(Sequence.$(MoveBy.$(50, 0, 1), MoveBy.$(0, 50, 1), MoveBy.$(-50, 0, 1), MoveBy.$(0, -50, 1))));
		stage.addActor(image2);

		layout.parse("" //
			+ "debug" //
			+ "* spacing:10" //
			+ "'Sweet' (text:'moo!!!!!!!!!!.')" //
			+ "'moo'" //
			+ "---" //
			+ "'Hi' align:bottom,right" //
			+ "[image1]" //
		);
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
		new LwjglApplication(new LibgdxTest(), "LibgdxTest", 640, 480, false);
	}
}
