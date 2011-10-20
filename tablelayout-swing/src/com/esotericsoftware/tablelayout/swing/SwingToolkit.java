
package com.esotericsoftware.tablelayout.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import com.esotericsoftware.tablelayout.Toolkit;

public class SwingToolkit extends Toolkit<Component, Table, TableLayout> {
	static {
		addClassPrefix("javax.swing.");
		addClassPrefix("java.awt.");
	}

	static SwingToolkit instance = new SwingToolkit();
	static Timer timer;
	static ArrayList<TableLayout> debugLayouts = new ArrayList(0);

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

	public Component wrap (TableLayout layout, Object object) {
		if (object instanceof String) return new JLabel((String)object);
		if (object == null) return new JPanel();
		if (object instanceof LayoutManager) return new JPanel((LayoutManager)object);
		return super.wrap(layout, object);
	}

	public Component newStack () {
		return new Stack();
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

	public TableLayout getLayout (Table table) {
		return table.getTableLayout();
	}

	public void clearDebugRectangles (TableLayout layout) {
		if (layout.debugRects != null) debugLayouts.remove(this);
		layout.debugRects = null;
	}

	public void addDebugRectangle (TableLayout layout, int type, int x, int y, int w, int h) {
		if (layout.debugRects == null) {
			layout.debugRects = new ArrayList();
			debugLayouts.add(layout);
		}
		layout.debugRects.add(new DebugRect(type, x, y, w, h));
	}

	static void startDebugTimer () {
		if (timer != null) return;
		timer = new Timer("TableLayout Debug", true);
		timer.schedule(newDebugTask(), 100);
	}

	static TimerTask newDebugTask () {
		return new TimerTask() {
			public void run () {
				if (!EventQueue.isDispatchThread()) {
					EventQueue.invokeLater(this);
					return;
				}
				for (TableLayout layout : debugLayouts)
					layout.drawDebug();
				timer.schedule(newDebugTask(), 250);
			}
		};
	}

	static class DebugRect extends Rectangle {
		final int type;

		public DebugRect (int type, int x, int y, int width, int height) {
			super(x, y, width - 1, height - 1);
			this.type = type;
		}
	}
}
