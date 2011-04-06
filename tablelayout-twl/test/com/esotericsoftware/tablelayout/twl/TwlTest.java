
package com.esotericsoftware.tablelayout.twl;

import java.io.File;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.FPSCounter;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.ProgressBar;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;
import de.matthiasmann.twl.theme.ThemeManager;

public class TwlTest {
	public TwlTest () throws Exception {
		LWJGLRenderer renderer = new LWJGLRenderer();

		ThemeManager theme = ThemeManager.createThemeManager(TwlTest.class.getResource("/widgets.xml"), renderer);

		Widget root = new Widget() {
			protected void layout () {
				layoutChildrenFullInnerArea();
			}
		};
		root.setTheme("");

		GUI gui = new GUI(root, renderer);
		gui.setSize();
		gui.applyTheme(theme);

		final HTMLTextAreaModel htmlText = new HTMLTextAreaModel();
		TextArea textArea = new TextArea(htmlText);
		htmlText
			.setHtml("<div style='text-align:center'>TWL TextAreaTest</div>Lorem ipsum dolor sit amet, douchebagus joglus. Sed fermentum gravida turpis, sit amet gravida justo laoreet non. Donec ultrices suscipit metus a mollis. Mollis varius egestas quisque feugiat pellentesque mi, quis scelerisque velit bibendum eget. Nulla orci in enim nisl mattis varius dignissim fringilla.<br/><br/>Curabitur purus leo, ultricies ut cursus eget, adipiscing in quam. Duis non velit vel mauris vulputate fringilla et quis.<br/><br/>Suspendisse lobortis iaculis tellus id fermentum. Integer fermentum varius pretium. Nullam libero magna, mattis vel placerat ac, dignissim sed lacus. Mauris varius libero id neque auctor a auctor odio fringilla.<br/><br/><div>Mauris orci arcu, porta eget porttitor luctus, malesuada nec metus. Nunc fermentum viverra leo eu pretium. Curabitur vitae nibh massa, imperdiet egestas lectus. Nulla odio quam, lobortis eget fermentum non, faucibus ac mi. Morbi et libero nulla. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Aliquam sit amet rhoncus nulla. Morbi consectetur ante convallis ante tristique et porta ligula hendrerit. Donec rhoncus ornare augue, sit amet lacinia nulla auctor venenatis.</div><br/><div>Etiam semper egestas porta. Proin luctus porta faucibus. Curabitur sagittis, lorem nec imperdiet ullamcorper, sem risus consequat purus, non faucibus turpis lorem ut arcu. Nunc tempus lobortis enim vitae facilisis. Morbi posuere quam nec sem aliquam eleifend.</div>");
		ScrollPane scrollPane = new ScrollPane(textArea);
		scrollPane.setFixed(ScrollPane.Fixed.HORIZONTAL);
		FPSCounter fpsCounter = new FPSCounter(4, 2);

		Table table = new Table();
		table.layout.register("scrollPane", scrollPane);
		table.layout.register("fpsCounter", fpsCounter);
		table.layout.parse("" //
			+ "[scrollPane] expand fill" //
			+ "---" //
			+ "<[fpsCounter]> align:right" //
			+ "---" //
			+ "[ProgressBar] fill (value:0.4)" //
		);
		root.add(table);

		while (!Display.isCloseRequested()) {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
			gui.update();
			Display.update();
		}
	}

	public static void main (String[] args) throws Exception {
		System.setProperty("org.lwjgl.librarypath", new File("lib/natives").getAbsolutePath());
		Display.setTitle("TWL Examples");
		Display.setDisplayMode(new DisplayMode(800, 600));
		Display.setVSyncEnabled(true);
		Display.create();
		new TwlTest();
	}
}
