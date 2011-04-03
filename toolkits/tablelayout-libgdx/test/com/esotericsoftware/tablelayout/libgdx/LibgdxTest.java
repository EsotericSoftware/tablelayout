
package com.esotericsoftware.tablelayout.libgdx;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Delay;
import com.badlogic.gdx.scenes.scene2d.actions.FadeIn;
import com.badlogic.gdx.scenes.scene2d.actions.FadeOut;
import com.badlogic.gdx.scenes.scene2d.actions.Forever;
import com.badlogic.gdx.scenes.scene2d.actions.MoveBy;
import com.badlogic.gdx.scenes.scene2d.actions.MoveTo;
import com.badlogic.gdx.scenes.scene2d.actions.Parallel;
import com.badlogic.gdx.scenes.scene2d.actions.Repeat;
import com.badlogic.gdx.scenes.scene2d.actions.RotateTo;
import com.badlogic.gdx.scenes.scene2d.actions.ScaleTo;
import com.badlogic.gdx.scenes.scene2d.actions.Sequence;
import com.badlogic.gdx.scenes.scene2d.actors.Image;

public class LibgdxTest implements ApplicationListener {
	private BitmapFont font;
	private Stage stage;
	private TableLayout table;
	private TextureRegion blendDownRegion;

	public void create () {
		font = new BitmapFont(true);

		stage = new Stage(640, 480, false);
		stage.projection.setToOrtho(0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, 0, 1);

		Group group = new Group();
		stage.addActor(group);
		group.width = 640;
		group.height = 480;

		Texture badlogic = new Texture(Gdx.files.internal("badlogic.jpg"));
		TextureRegion badlogicRegion = new TextureRegion(badlogic, 0, 0, 256, 256);
		badlogicRegion.flip(false, true);

		Image img1 = new Image("image1", badlogicRegion);
		img1.width = img1.height = 128;
		img1.originX = img1.originY = 64;
		img1.action(Sequence.$(FadeOut.$(1), FadeIn.$(1), ScaleTo.$(0.5f, 0.5f, 1), FadeOut.$(0.5f),
			Delay.$(Parallel.$(RotateTo.$(360, 1), FadeIn.$(1), ScaleTo.$(1, 1, 1)), 1)));
		group.addActor(img1);

		Image img2 = new Image("image2", badlogicRegion);
		img2.width = img2.height = 64;
		img2.originX = img2.originY = 32;
		img2.action(Forever.$(Sequence.$(MoveBy.$(50, 0, 1), MoveBy.$(0, 50, 1), MoveBy.$(-50, 0, 1), MoveBy.$(0, -50, 1))));
		stage.addActor(img2);

		table = new TableLayout(group, font);
		table.parse("" //
			+ "debug" //
			+ "* spacing:10" //
			+ "'Sweet' (text:'moo!!!!!!!!!!.')" //
			+ "'moo'" //
			+ "---" //
			+ "'Hi' align:bottom,right"//
			+ "[image1]" //
		);
	}

	public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		table.update();
		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30.0f));
		stage.draw();
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
		new LwjglApplication(new LibgdxTest(), "LibgdxTest", 640, 480, false);
	}
}
