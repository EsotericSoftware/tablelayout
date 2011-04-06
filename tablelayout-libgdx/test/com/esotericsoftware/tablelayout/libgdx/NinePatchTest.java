
package com.esotericsoftware.tablelayout.libgdx;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actors.Image;

public class NinePatchTest implements ApplicationListener {
	private Stage stage;

	public void create () {
		GdxTableLayout.font = new BitmapFont(true);

		stage = new Stage(800, 600, false);
		stage.projection.setToOrtho(0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, 0, 1);

		GdxTableLayout layout = new Table().layout;
		stage.addActor(layout.getTable());
		layout.getTable().width = 800;
		layout.getTable().height = 600;

		Image nw = new Image("nw", new TextureRegion(new Texture(Gdx.files.internal("9patch-nw.png"))));
		nw.region.flip(false, true); // Flip for use with y down coordinate system.
		layout.register(nw);

		Image ne = new Image("ne", nw.region);
		ne.region.flip(true, false);
		layout.register(ne);

		Image sw = new Image("sw", nw.region);
		sw.region.flip(false, true);
		layout.register(sw);

		Image se = new Image("se", nw.region);
		se.region.flip(true, true);
		layout.register(se);

		Image n = new Image("n", new TextureRegion(new Texture(Gdx.files.internal("9patch-n.png"))));
		n.region.flip(false, true); // Flip for use with y down coordinate system.
		layout.register(n);

		Image s = new Image("s", n.region);
		s.region.flip(false, true);
		layout.register(s);

		Image w = new Image("w", new TextureRegion(new Texture(Gdx.files.internal("9patch-w.png"))));
		w.region.flip(false, true); // Flip for use with y down coordinate system.
		layout.register(w);

		Image e = new Image("e", w.region);
		e.region.flip(true, false);
		layout.register(e);

		layout.parse("" //
			+ "" //
			+ "* fill" //
			+ "[nw] [n] [ne]" //
			+ "---" //
			+ "[w] { debug name:content * pad:10 '1' '2' --- '3' '4' } expand [e]" //
			+ "---" //
			+ "[sw] [s] [se]" //
		);
		layout.layout();

		Image center = new Image("center", new TextureRegion(new Texture(Gdx.files.internal("9patch-center.png"))));
		center.region.flip(false, true); // Flip for use with y down coordinate system.

		// Add a background image stretched to the size of the table named "content". Note the table must be already laid out.
		Table contentTable = (Table)layout.getWidget("content");
		center.width = contentTable.width;
		center.height = contentTable.height;
		contentTable.addActorAt(0, center);
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
		new LwjglApplication(new NinePatchTest(), "NinePatchTest", 800, 600, false);
	}
}
