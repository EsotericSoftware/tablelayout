
package com.esotericsoftware.tablelayout.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Insets;
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

import com.esotericsoftware.tablelayout.BaseTableLayout;
import com.esotericsoftware.tablelayout.Cell;

public class TableLayout extends BaseTableLayout<Component> {
	static {
		addClassPrefix("javax.swing.");
		addClassPrefix("java.awt.");
	}

	static Timer timer;
	static ArrayList<TableLayout> debugLayouts = new ArrayList(0);

	Table table;
	ArrayList<DebugRect> debugRects;

	public void layout () {
		Insets insets = table.getInsets();
		tableLayoutX = insets.left;
		tableLayoutY = insets.top;
		tableLayoutWidth = table.getWidth() - insets.left - insets.right;
		tableLayoutHeight = table.getHeight() - insets.top - insets.bottom;

		super.layout();

		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;
			Component component = (Component)c.widget;
			component.setLocation(c.widgetX, c.widgetY);
			component.setSize(c.widgetWidth, c.widgetHeight);
		}

		if (debug != null && timer == null) {
			timer = new Timer("TableLayout Debug", true);
			timer.schedule(newDebugTask(), 100);
		}
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

	public Component wrap (Object object) {
		if (object instanceof String) return new JLabel((String)object);
		if (object == null) return new JPanel();
		if (object instanceof LayoutManager) return new JPanel((LayoutManager)object);
		return super.wrap(object);
	}

	public BaseTableLayout newTableLayout () {
		TableLayout layout = new Table().layout;
		layout.setParent(this);
		return layout;
	}

	public Component newStack () {
		return new JPanel(new LayoutManager() {
			public void layoutContainer (Container parent) {
				int width = parent.getWidth();
				int height = parent.getHeight();
				for (int i = 0, n = parent.getComponentCount(); i < n; i++) {
					parent.getComponent(i).setLocation(0, 0);
					parent.getComponent(i).setSize(width, height);
				}
			}

			public Dimension preferredLayoutSize (Container parent) {
				Dimension size = new Dimension();
				for (int i = 0, n = parent.getComponentCount(); i < n; i++) {
					Dimension pref = parent.getComponent(i).getPreferredSize();
					size.width = Math.max(size.width, pref.width);
					size.height = Math.max(size.height, pref.height);
				}
				return size;
			}

			public Dimension minimumLayoutSize (Container parent) {
				Dimension size = new Dimension();
				for (int i = 0, n = parent.getComponentCount(); i < n; i++) {
					Dimension min = parent.getComponent(i).getMinimumSize();
					size.width = Math.max(size.width, min.width);
					size.height = Math.max(size.height, min.height);
				}
				return size;
			}

			public void addLayoutComponent (String name, Component comp) {
			}

			public void removeLayoutComponent (Component comp) {
			}
		});
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

	public void invalidate () {
		table.invalidate();
	}

	TimerTask newDebugTask () {
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

	void drawDebug () {
		Graphics2D g = (Graphics2D)table.getGraphics();
		if (g == null) return;
		g.setColor(Color.red);
		for (DebugRect rect : debugRects) {
			if (rect.type.equals(DEBUG_CELL)) g.setColor(Color.red);
			if (rect.type.equals(DEBUG_WIDGET)) g.setColor(Color.green);
			if (rect.type.equals(DEBUG_TABLE)) g.setColor(Color.blue);
			g.draw(rect);
		}
	}

	public void clearDebugRectangles () {
		if (debugRects != null) debugLayouts.remove(this);
		debugRects = null;
	}

	public void addDebugRectangle (String type, int x, int y, int w, int h) {
		if (debugRects == null) {
			debugRects = new ArrayList();
			debugLayouts.add(this);
		}
		debugRects.add(new DebugRect(type, x, y, w, h));
	}

	public Table getTable () {
		return table;
	}

	static private class DebugRect extends Rectangle {
		final String type;

		public DebugRect (String type, int x, int y, int width, int height) {
			super(x, y, width - 1, height - 1);
			this.type = type;
		}
	}
}
