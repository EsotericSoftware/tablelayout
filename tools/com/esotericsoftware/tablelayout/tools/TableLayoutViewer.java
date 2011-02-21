
package com.esotericsoftware.tablelayout.tools;

import java.awt.EventQueue;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import com.esotericsoftware.tablelayout.swing.TableLayout;

// BOZO - Split syntax? [left|right] [top-bottom]
// BOZO - Add class prefixes.
// BOZO - Support adding a layout, which gets wrapped in a panel.

public class TableLayoutViewer extends JFrame {
	private TableLayout table;

	public TableLayoutViewer () {
		TableLayout table = new TableLayout();

		table.set("text", new JTextArea());


		JSplitPane split = new JSplitPane();
		split.add(new JLabel("table"), "left");
		split.add(new JLabel("this.table = new TableLayout()"), "right");

		setContentPane(split);
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
