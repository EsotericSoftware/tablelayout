
package com.esotericsoftware.tablelayout.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.io.StringWriter;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

// BOZO - Save last markup.

public class TableLayoutEditor extends JFrame {
	JTextArea codeArea, errorArea;
	Table outputTable;

	public TableLayoutEditor () {
		super("TableLayout Editor");

		outputTable = new Table(new OutputLayout());

		Table table = new Table();
		table.layout.register("outputTable", outputTable);
		table.layout.parse("padding:10 " //
			+ "[JSplitPane] expand fill ( "//
			+ "{" //
			+ "[JScrollPane] size:300,0 expand fill ([codeArea:JTextArea])" //
			+ "---" //
			+ "[JScrollPane] size:300,0 expand fill ([errorArea:JTextArea])" //
			+ "}" //
			+ "[outputTable]" //
			+ ")");

		errorArea = (JTextArea)table.layout.getWidget("errorArea");
		errorArea.setFont(Font.decode("monospaced"));
		errorArea.setWrapStyleWord(true);
		errorArea.setLineWrap(true);
		errorArea.setForeground(Color.red);

		codeArea = (JTextArea)table.layout.getWidget("codeArea");
		codeArea.setText("[split:JSplitPane] expand fill (\n" //
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
		outputTable.layout.parse(codeArea.getText());

		codeArea.setFont(Font.decode("monospaced"));
		codeArea.getDocument().addDocumentListener(new DocumentListener() {
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
						errorArea.setText("");
						outputTable.layout.clear();
						TableLayout.debugLayouts.clear();
						try {
							outputTable.layout.parse(codeArea.getText());
						} catch (Throwable ex) {
							ex.printStackTrace();
							StringWriter buffer = new StringWriter(1024);
							if (ex.getCause() != null) ex = ex.getCause();
							while (ex != null) {
								buffer.append(ex.getMessage());
								buffer.append('\n');
								ex = ex.getCause();
							}
							errorArea.setText(buffer.toString());
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
		setSize(800, 600);
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
