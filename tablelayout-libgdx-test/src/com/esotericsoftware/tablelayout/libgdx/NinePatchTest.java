
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

	@Override public void create () {
		TableLayout.defaultFont = new BitmapFont();

		stage = new Stage(800, 600, false);

		TableLayout layout = new Table().layout;
		stage.addActor(layout.getTable());
		layout.getTable().width = 800;
		layout.getTable().height = 600;

		Image nw = new Image("nw", new TextureRegion(new Texture(Gdx.files.internal("9patch-nw.png"))));
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
		layout.register(n);

		Image s = new Image("s", n.region);
		s.region.flip(false, true);
		layout.register(s);

		Image w = new Image("w", new TextureRegion(new Texture(Gdx.files.internal("9patch-w.png"))));
		layout.register(w);

		Image e = new Image("e", w.region);
		e.region.flip(true, false);
		layout.register(e);

		Image bg = new Image("bg", new TextureRegion(new Texture(Gdx.files.internal("9patch-center.png"))));
		layout.register(bg);

		Image footerBg = new Image("footerBg", new TextureRegion(new Texture(Gdx.files.internal("footer-bg.png"))));
		layout.register(footerBg);

		Image buttonBg = new Image("buttonBg", new TextureRegion(new Texture(Gdx.files.internal("button-bg.png"))));
		layout.register(buttonBg);

		String longText = "0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 "
			+ "0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 "
			+ "0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 "
			+ "0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789";

		// @off
		layout.parse(""
			+ "* fill"
			+ "[sw] [s] [se]"
			+ "---"
			+ "[w]"
			+ "<"
			+ "   [bg]"
			+ "   { debug:widgets"
			+ "      <"
			+ "         [footerBg]"
			+ "         [footerLayout:{"
			+ "            debug * align:top"
			+ "            <[buttonBg] 'Button Text'>"
			+ "         }]"
			+ "      > fill:x"
			+ "      ---"
			+ "      'Content\nMultiple lines\nof content.\n" + longText + "' fill expand (type:wrapped,left valign:top)"
			+ "      ---"
			+ "      'headerBg' fill:x"
			+ "   }"
			+ "> expand"
			+ "[e]"
			+ "---"
			+ "[nw] [n] [ne]"
		);
		// @on

	}

	@Override public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30.0f));
		stage.draw();
		Table.drawDebug(stage);
	}

	@Override public void resize (int width, int height) {
	}

	@Override public void resume () {
	}

	@Override public void pause () {
	}

	@Override public void dispose () {
	}

	public static void main (String[] args) throws Exception {
		new LwjglApplication(new NinePatchTest(), "NinePatchTest", 800, 600, false);
	}
}
