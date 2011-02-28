
package com.esotericsoftware.tablelayout.tools;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;

import com.esotericsoftware.tablelayout.swing.TableLayout;

public class TableLayoutEditor extends JFrame {
	JTextArea textArea;
	TableLayout outputTable;
	JPanel outputPanel;
	Border redBorder, emptyBorder;

	public TableLayoutEditor () {
		super("TableLayout Editor");

		outputTable = new TableLayout() {
			public Component getWidget (String name) {
				Component widget = super.getWidget(name);
				if (widget != null) return widget;
				if (name.endsWith("Edit")) return new JTextField();
				if (name.endsWith("Button")) return new JButton("Center");
				return new JLabel(name);
			}
		};
		outputPanel = new JPanel(outputTable);

		redBorder = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.red);
		emptyBorder = BorderFactory.createEmptyBorder();

		TableLayout table = new TableLayout();
		table.setName("textArea", textArea = new JTextArea());
		table.setName("outputPanel", outputPanel);
		table.parse("padding:10 " //
			+ "[JSplitPane] expand fill ( "//
			+ "{ [textArea] width:300 expand fill } left" //
			+ "[outputPanel] right" //
			+ ")");

		textArea.setText("[split:JSplitPane] expand fill (\n" //
			+ "   dividerSize:25\n" //
			+ "   orientation:VERTICAL_SPLIT\n" //
			+ "   'Top widget' top\n" //
			+ "   {\n" //
			+ "      debug\n" //
			+ "      'Table on the bottom!'\n" //
			+ "      ---\n" //
			+ "      [someEdit] fill\n" //
			+ "   } bottom\n" //
			+ ")");
		outputTable.parse(textArea.getText());

		textArea.setFont(Font.decode("monospaced"));
		textArea.addKeyListener(new KeyAdapter() {
			public void keyTyped (KeyEvent e) {
				EventQueue.invokeLater(new Runnable() {
					public void run () {
						outputPanel.removeAll();
						try {
							outputTable.parse(textArea.getText());
							textArea.setBorder(emptyBorder);
						} catch (Exception ex) {
							ex.printStackTrace();
							textArea.setBorder(redBorder);
							outputPanel.removeAll();
							outputTable.clear();
						}
						outputPanel.revalidate();
						outputPanel.repaint();
					}
				});
			}
		});

		setContentPane(new JPanel(table));
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
