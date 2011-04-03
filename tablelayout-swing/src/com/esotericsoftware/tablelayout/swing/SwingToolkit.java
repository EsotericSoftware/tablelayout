
package com.esotericsoftware.tablelayout.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import com.esotericsoftware.tablelayout.TableLayout;
import com.esotericsoftware.tablelayout.Toolkit;

public class SwingToolkit extends Toolkit<Component> {
	static {
		addClassPrefix("javax.swing.");
		addClassPrefix("java.awt.");
	}

	static public SwingToolkit instance = new SwingToolkit();

	public SwingToolkit () {
		super(Component.class);
	}

	public void addChild (Component parent, Component child, String layoutString) {
		if (parent instanceof JSplitPane && layoutString == null) {
			if (((JSplitPane)parent).getLeftComponent() instanceof JButton)
				layoutString = "left";
			else if (((JSplitPane)parent).getRightComponent() instanceof JButton) //
				layoutString = "right";
		}

		if (parent instanceof JScrollPane)
			((JScrollPane)parent).setViewportView(child);
		else
			((Container)parent).add(child, layoutString);
	}

	public void removeChild (Component parent, Component child) {
		((Container)parent).remove(child);
	}

	public TableLayout newTableLayout () {
		Table table = new Table();
		return table.layout;
	}

	public Component newLabel (String text) {
		return new JLabel(text);
	}

	public Component newEmptyWidget () {
		return new JPanel();
	}

	public void setTitle (Component parent, String title) {
		if (!(parent instanceof JComponent)) return;
		((JComponent)parent).setBorder(BorderFactory.createTitledBorder(title));
	}

	public Component wrap (Object object) {
		if (object instanceof LayoutManager) return new JPanel((LayoutManager)object);
		return super.wrap(object);
	}

	public int getMinWidth (Component widget) {
		return widget.getMinimumSize().width;
	}

	public int getMinHeight (Component widget) {
		return widget.getMinimumSize().height;
	}

	public int getPrefWidth (Component widget) {
		return widget.getPreferredSize().width;
	}

	public int getPrefHeight (Component widget) {
		return widget.getPreferredSize().height;
	}

	public int getMaxWidth (Component widget) {
		return widget.getMaximumSize().width;
	}

	public int getMaxHeight (Component widget) {
		return widget.getMaximumSize().height;
	}
}
