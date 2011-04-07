
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
import javax.swing.text.Highlighter;

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

public class TableLayoutEditor extends JFrame {
	RSyntaxTextArea codeText;
	JTextArea errorText;
	Table outputTable;
	SquiggleUnderlineHighlightPainter errorPainter = new SquiggleUnderlineHighlightPainter(Color.red);

	public TableLayoutEditor () {
		super("TableLayout Editor");

		TokenMakerFactory.setDefaultInstance(new Factory());

		codeText = new RSyntaxTextArea();
		codeText.setSyntaxEditingStyle("tablelayout");
		codeText.setHighlightCurrentLine(false);
		codeText.setCloseCurlyBraces(false);
		codeText.setCaretColor(Color.black);
		codeText.setBackground(Color.white);
		codeText.setSelectionColor(new Color(0xd4eaff));
		codeText.setTextAntiAliasHint("VALUE_TEXT_ANTIALIAS_ON");
		codeText.setTabSize(3);

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
			scheme.setStyle(BRACKET, new Style(new Color(0, 0, 128), new Color(240, 240, 240), monoBold));
			scheme.setStyle(NAME, new Style(new Color(0, 0, 128), new Color(240, 240, 240), mono));
			scheme.setStyle(STRING, new Style(new Color(0, 127, 127), new Color(240, 240, 240)));
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
			+ "[codeScroll] size:300,0 expand:3 fill" //
			+ "---" //
			+ "[JScrollPane] size:300,50 expand:1 fill ([errorText:JTextArea])" //
			+ "}" //
			+ "[outputTable]" //
			+ ")");

		errorText = (JTextArea)table.layout.getWidget("errorText");
		errorText.setFont(Font.decode("monospaced"));
		errorText.setWrapStyleWord(true);
		errorText.setLineWrap(true);
		errorText.setForeground(Color.red);

		codeText.setText("[split:JSplitPane] expand fill (\n" //
			+ "   dividerSize:25\n" //
			+ "   setResizeWeight:0.4\n" //
			+ "   orientation:VERTICAL_SPLIT\n" //
			+ "   {\n" //
			+ "      debug\n" //
			+ "      'Table on the bottom!'\n" //
			+ "      ---\n" //
			+ "      [someEdit] fill\n" //
			+ "   } bottom\n" //
			+ "   {\n" //
			+ "   	'Top widget'\n" //
			+ "   	[JScrollPane] size:100,0 expand fill ([JTextArea])\n" //
			+ "   } top\n" //
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
							// if (ex.getCause() != null) ex = ex.getCause();
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
	}

	static public class Placeholder extends JLabel {
		private Dimension min = new Dimension();
		private Dimension max = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);

		public Placeholder (String text) {
			super(text);
			setOpaque(true);
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
