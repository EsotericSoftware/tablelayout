
package com.esotericsoftware.tablelayout.swing;

import java.awt.EventQueue;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class SwingTest extends JFrame {

	public SwingTest () {
		setTitle("SwingTest");
		setSize(640, 480);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setVisible(true);

		JButton button1 = new JButton("One");
		JButton button2 = new JButton("Two");
		JButton button3 = new JButton("Three");
		JButton button4 = new JButton("Four");
		JButton button5 = new JButton("Five");
		JTextField text1 = new JTextField("One");
		JTextField text2 = new JTextField("Two");
		JTextField text3 = new JTextField("Three");

		Table table = new Table();
		getContentPane().add(table);

		table.addCell(button1);
		table.addCell(button2);
		table.row();
		table.addCell(button3).colspan(2);
		table.row();
		table.addCell(text1).colspan(2).fill();
		table.row();
		table.addCell(text2).right().width(150);
		table.addCell(text3).width(250);
		table.row();
		table.addCell(button4).expand().colspan(2);
		table.debug();
	}

	public static void main (String[] args) throws Exception {
		EventQueue.invokeLater(new Runnable() {
			public void run () {
				new SwingTest();
			}
		});
	}
}
