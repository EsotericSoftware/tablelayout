
package com.esotericsoftware.tablelayout.tools;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import com.esotericsoftware.tablelayout.swing.TableLayout;

// BOZO - Add class prefixes.
// BOZO - Support adding a layout, which gets wrapped in a panel.

public class TableLayoutViewer extends JFrame {
	private TableLayout table;

	public TableLayoutViewer () {

		TableLayout table = new TableLayout();

		table.setName("left", new JLabel("Left!"));
		table.setName("text", new JTextArea("Right!"));

		table.parse("padding:10 debug " //
			+ "[tabs:JSplitPane] expand fill (dividerSize:4 "//
			+ "{ debug *space:10 [JLabel] (text:'l  eftmoo!') 'and stuff' (text:omnomnomnom) } left" //
			+ "'right' right" //
			+ ")");

		setContentPane(new JPanel(table));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(800, 600);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public static void main (String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run () {
				new TableLayoutViewer();
			}
		});
	}
}
