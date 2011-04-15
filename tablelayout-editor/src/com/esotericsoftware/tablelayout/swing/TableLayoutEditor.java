
package com.esotericsoftware.tablelayout.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.ShorthandCompletion;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SquiggleUnderlineHighlightPainter;
import org.fife.ui.rsyntaxtextarea.Style;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.esotericsoftware.tablelayout.ParseException;

import static com.esotericsoftware.tablelayout.swing.TableLayoutTokenizer.*;

// BOZO - Save last markup.
// BOZO - Support paste/copy of Java strings.

public class TableLayoutEditor extends JFrame {
	RSyntaxTextArea codeText;
	JTextArea errorText;
	Table outputTable;
	SquiggleUnderlineHighlightPainter errorPainter = new SquiggleUnderlineHighlightPainter(Color.red);
	private AutoCompletion autoCompletion;

	public TableLayoutEditor () {
		super("TableLayout Editor");

		TokenMakerFactory.setDefaultInstance(new Factory());

		codeText = new RSyntaxTextArea();
		codeText.setSyntaxEditingStyle("tablelayout");
		codeText.setCloseCurlyBraces(false);
		codeText.setAutoIndentEnabled(true);
		codeText.setHighlightCurrentLine(false);
		codeText.setLineWrap(true);
		codeText.setCaretColor(Color.black);
		codeText.setBackground(Color.white);
		codeText.setSelectionColor(new Color(0xd4eaff));
		codeText.setTextAntiAliasHint("VALUE_TEXT_ANTIALIAS_ON");
		codeText.setTabSize(3);

		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.addCompletion(new ShorthandCompletion(provider, "debug", "debug"));
		provider.addCompletion(new ShorthandCompletion(provider, "size", "size"));
		provider.addCompletion(new ShorthandCompletion(provider, "width", "width"));
		provider.addCompletion(new ShorthandCompletion(provider, "height", "height"));
		provider.addCompletion(new ShorthandCompletion(provider, "expand", "expand"));
		provider.addCompletion(new ShorthandCompletion(provider, "fill", "fill"));
		provider.addCompletion(new ShorthandCompletion(provider, "align", "align"));
		provider.addCompletion(new ShorthandCompletion(provider, "colspan", "colspan"));
		provider.addCompletion(new ShorthandCompletion(provider, "uniform", "uniform"));
		provider.addCompletion(new ShorthandCompletion(provider, "padding", "padding"));
		provider.addCompletion(new ShorthandCompletion(provider, "paddingTop", "paddingTop"));
		provider.addCompletion(new ShorthandCompletion(provider, "paddingLeft", "paddingLeft"));
		provider.addCompletion(new ShorthandCompletion(provider, "paddingBottom", "paddingBottom"));
		provider.addCompletion(new ShorthandCompletion(provider, "paddingRight", "paddingRight"));
		provider.addCompletion(new ShorthandCompletion(provider, "spacing", "spacing"));
		provider.addCompletion(new ShorthandCompletion(provider, "spacingTop", "spacingTop"));
		provider.addCompletion(new ShorthandCompletion(provider, "spacingLeft", "spacingLeft"));
		provider.addCompletion(new ShorthandCompletion(provider, "spacingBottom", "spacingBottom"));
		provider.addCompletion(new ShorthandCompletion(provider, "spacingRight", "spacingRight"));
		provider.addCompletion(new ShorthandCompletion(provider, "ignore", "ignore"));
		provider.addCompletion(new ShorthandCompletion(provider, "pad", "pad"));
		provider.addCompletion(new ShorthandCompletion(provider, "padTop", "padTop"));
		provider.addCompletion(new ShorthandCompletion(provider, "padLeft", "padLeft"));
		provider.addCompletion(new ShorthandCompletion(provider, "padBottom", "padBottom"));
		provider.addCompletion(new ShorthandCompletion(provider, "padRight", "padRight"));
		provider.addCompletion(new ShorthandCompletion(provider, "space", "space"));
		provider.addCompletion(new ShorthandCompletion(provider, "spaceTop", "spaceTop"));
		provider.addCompletion(new ShorthandCompletion(provider, "spaceLeft", "spaceLeft"));
		provider.addCompletion(new ShorthandCompletion(provider, "spaceBottom", "spaceBottom"));
		provider.addCompletion(new ShorthandCompletion(provider, "spaceRight", "spaceRight"));
		autoCompletion = new AutoCompletion(provider);
		autoCompletion.setChoicesWindowSize(200, 300);
		autoCompletion.setAutoCompleteSingleChoices(true);
		autoCompletion.install(codeText);

		RTextScrollPane codeScroll = new RTextScrollPane(codeText);

		try {
			Font mono = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/VeraMono.ttf")).deriveFont(12f);
			Font monoBold = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/VeraMonoBold.ttf")).deriveFont(12f);

			codeText.setFont(mono);
			codeScroll.getGutter().setLineNumberFont(mono);

			SyntaxScheme scheme = new SyntaxScheme(true);
			codeText.setSyntaxScheme(scheme);
			scheme.setStyle(PLAIN, new Style(new Color(0, 0, 0), null));
			scheme.setStyle(STRUCTURE, new Style(new Color(0, 0, 128), null, monoBold));
			scheme.setStyle(SYMBOL, new Style(new Color(0, 0, 255), null, monoBold));
			scheme.setStyle(BRACKET, new Style(new Color(0, 0, 128), new Color(230, 230, 230), monoBold));
			scheme.setStyle(NAME, new Style(new Color(0, 0, 128), new Color(230, 230, 230), mono));
			scheme.setStyle(STRING, new Style(new Color(0, 127, 127), new Color(230, 230, 230)));
			scheme.setStyle(KEYWORD, new Style(new Color(0, 0, 128), null));
			scheme.setStyle(PROPERTY, new Style(new Color(105, 0, 191), null));
			scheme.setStyle(CONSTANT, new Style(new Color(0, 0, 255), null));
			scheme.setStyle(VALUE, new Style(new Color(0, 127, 127), null));
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		outputTable = new Table(new OutputLayout());

		Table table = new Table();
		table.layout.register("codeScroll", codeScroll);
		table.layout.register("outputTable", outputTable);
		table.layout.parse("padding:10 " //
			+ "[JSplitPane] expand fill ( setResizeWeight:0.4 background:white"//
			+ "{" //
			+ "[codeScroll] size:300,0 expand fill" //
			+ "---" //
			+ "[JScrollPane] size:300,100 fill ([errorText:JTextArea])" //
			+ "}" //
			+ "[outputTable]" //
			+ ")");

		errorText = (JTextArea)table.layout.getWidget("errorText");
		errorText.setFont(Font.decode("monospaced"));
		errorText.setWrapStyleWord(true);
		errorText.setLineWrap(true);
		errorText.setForeground(Color.red);

		codeText.setText("[split:JSplitPane] expand fill (\n" //
			+ "\tdividerSize:25\n" //
			+ "\tsetResizeWeight:0.4\n" //
			+ "\torientation:VERTICAL_SPLIT\n" //
			+ "\t{\n" //
			+ "\t\tdebug\n" //
			+ "\t\t'Table on the bottom!' width:50%\n" //
			+ "\t\t---\n" //
			+ "\t\t[someEdit] fill\n" //
			+ "\t} bottom\n" //
			+ "\t{\n" //
			+ "\t\t'Top widget'\n" //
			+ "\t\t[JScrollPane] expand fill ([JTextArea])\n" //
			+ "\t} top\n" //
			+ ")");
		outputTable.layout.parse(codeText.getText());

		codeText.getDocument().addDocumentListener(new DocumentListener() {
			public void removeUpdate (DocumentEvent e) {
				changed();
			}

			public void insertUpdate (DocumentEvent e) {
				changed();
			}

			public void changedUpdate (DocumentEvent e) {
				changed();
			}

			private void changed () {
				EventQueue.invokeLater(new Runnable() {
					public void run () {
						System.out.println();
						errorText.setText("");
						outputTable.layout.clear();
						TableLayout.debugLayouts.clear();
						codeText.getHighlighter().removeAllHighlights();
						try {
							outputTable.layout.parse(codeText.getText());
						} catch (Throwable ex) {
							ex.printStackTrace();

							StringWriter buffer = new StringWriter(1024);
							while (ex != null) {
								buffer.append(ex.getMessage());
								buffer.append('\n');

								if (ex instanceof ParseException) {
									ParseException parseEx = (ParseException)ex;
									try {
										int start = codeText.getLineStartOffset(parseEx.line - 1) + parseEx.column;
										int end = codeText.getLineEndOffset(parseEx.line - 1);
										if (start == end) start -= parseEx.column;
										codeText.getHighlighter().addHighlight(start, end, errorPainter);
									} catch (BadLocationException ignored) {
									}
								}

								ex = ex.getCause();
							}
							errorText.setText(buffer.toString());
							outputTable.layout.clear();
						}
						outputTable.revalidate();
						outputTable.repaint();
					}
				});
			}
		});

		setContentPane(table);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(850, 600);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	static public class OutputLayout extends TableLayout {
		public Component getWidget (String name) {
			Component widget = super.getWidget(name);
			if (widget != null) return widget;
			try {
				return newWidget(name);
			} catch (Exception ignored) {
			}
			if (name.toLowerCase().endsWith("edit")) return new JTextField();
			if (name.toLowerCase().endsWith("button")) return new JButton("Button");
			return new Placeholder("[" + name + "]");
		}

		protected String validateSize (String size) {
			if (!size.equals(MIN) && !size.equals(PREF) && !size.equals(MAX)) width(size);
			return size;
		}
	}

	static public class Placeholder extends JLabel {
		private Dimension min = new Dimension();
		private Dimension max = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);

		public Placeholder (String text) {
			super(text);
			setOpaque(true);
		}

		public Dimension getMinimumSize () {
			return min;
		}

		public Dimension getMaximumSize () {
			return max;
		}
	}

	static public class Factory extends AbstractTokenMakerFactory {
		protected Map createTokenMakerKeyToClassNameMap () {
			HashMap map = new HashMap();
			map.put("tablelayout", TableLayoutTokenizer.class.getName());
			return map;
		}
	}

	public static void main (String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run () {
				new TableLayoutEditor();
			}
		});
	}
}
