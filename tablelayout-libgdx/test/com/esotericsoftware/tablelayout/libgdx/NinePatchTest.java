
package com.esotericsoftware.tablelayout.libgdx;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actors.Image;

public class NinePatchTest implements ApplicationListener {
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

		Image nw = new Image("nw", new TextureRegion(new Texture(Gdx.files.internal("9patch-nw.png"))));
		nw.region.flip(false, true); // Flip for use with y down coordinate system.
		group.addActor(nw);

		Image ne = new Image("ne", nw.region);
		ne.region.flip(true, false);
		group.addActor(ne);

		Image sw = new Image("sw", nw.region);
		sw.region.flip(false, true);
		group.addActor(sw);

		Image se = new Image("se", nw.region);
		se.region.flip(true, true);
		group.addActor(se);

		Image n = new Image("n", new TextureRegion(new Texture(Gdx.files.internal("9patch-n.png"))));
		n.region.flip(false, true); // Flip for use with y down coordinate system.
		group.addActor(n);

		Image s = new Image("s", n.region);
		s.region.flip(false, true);
		group.addActor(s);

		Image w = new Image("w", new TextureRegion(new Texture(Gdx.files.internal("9patch-w.png"))));
		w.region.flip(false, true); // Flip for use with y down coordinate system.
		group.addActor(w);

		Image e = new Image("e", w.region);
		e.region.flip(true, false);
		group.addActor(e);

		Image center = new Image("center", new TextureRegion(new Texture(Gdx.files.internal("9patch-center.png"))));
		center.region.flip(false, true); // Flip for use with y down coordinate system.
		group.addActor(center);

		table = new TableLayout(group, font);
		table.parse("" //
			// + "debug" //
			+ "* fill" //
			+ "[nw] [n] [ne]" //
			+ "---" //
			+ "[w] [center] expand [e]" //
			+ "---" //
			+ "[sw] [s] [se]" //
		);
	}

	public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		table.update();
		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30.0f));
		stage.draw();
		table.drawDebug();
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
		new LwjglApplication(new NinePatchTest(), "LibgdxTest", 640, 480, false);
	}
}
