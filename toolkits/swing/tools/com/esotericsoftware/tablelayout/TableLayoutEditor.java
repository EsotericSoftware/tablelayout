
package com.esotericsoftware.tablelayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.esotericsoftware.tablelayout.swing.TableLayout;

public class TableLayoutEditor extends JFrame {
	JTextArea codeArea, errorArea;
	TableLayout outputTable;

	public TableLayoutEditor () {
		super("TableLayout Editor");

		outputTable = new TableLayout() {
			public Component getWidget (String name) {
				Component widget = super.getWidget(name);
				if (widget != null) return widget;
				try {
					return (Component)wrap(newWidget(name));
				} catch (Exception ignored) {
				}
				if (name.endsWith("Edit")) return new JTextField();
				if (name.endsWith("Button")) return new JButton("Center");
				return new JLabel(name);
			}
		};

		TableLayout table = new TableLayout();
		table.setName("outputPanel", outputTable.getContainer());
		table.parse("padding:10 " //
			+ "[JSplitPane] expand fill ( "//
			+ "{" //
			+ "[JScrollPane] size:300,0 expand fill ([codeArea:JTextArea])" //
			+ "---" //
			+ "[JScrollPane] size:300,0 expand fill ([errorArea:JTextArea])" //
			+ "}" //
			+ "[outputPanel]" //
			+ ")");

		errorArea = (JTextArea)table.getWidget("errorArea");
		errorArea.setFont(Font.decode("monospaced"));
		errorArea.setWrapStyleWord(true);
		errorArea.setLineWrap(true);
		errorArea.setForeground(Color.red);

		codeArea = (JTextArea)table.getWidget("codeArea");
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
		outputTable.parse(codeArea.getText());

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
						errorArea.setText("");
						outputTable.clear();
						try {
							outputTable.parse(codeArea.getText());
						} catch (Throwable ex) {
							// ex.printStackTrace();
							StringWriter buffer = new StringWriter(1024);
							if (ex.getCause() != null) ex = ex.getCause();
							while (ex != null) {
								buffer.append(ex.getMessage());
								buffer.append('\n');
								ex = ex.getCause();
							}
							errorArea.setText(buffer.toString());
							outputTable.clear();
						}
						outputTable.getContainer().doLayout();
						outputTable.getContainer().repaint();
					}
				});
			}
		});

		setContentPane(table.getContainer());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(800, 600);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public static void main (String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run () {
				new TableLayoutEditor();
			}
		});
	}
}
