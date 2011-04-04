
package com.esotericsoftware.tablelayout.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import com.esotericsoftware.tablelayout.Cell;
import com.esotericsoftware.tablelayout.TableLayout;
import com.esotericsoftware.tablelayout.Toolkit;

public class SwingTableLayout extends TableLayout<Component> {
	static Timer timer;
	static ArrayList<SwingTableLayout> debugLayouts = new ArrayList(0);

	Table table;
	ArrayList<DebugRect> debugRects;

	public SwingTableLayout () {
		super();
	}

	public SwingTableLayout (TableLayout parent) {
		super(parent);
	}

	public Toolkit getToolkit () {
		return SwingToolkit.instance;
	}

	public void layout () {
		if (title != null) {
			Border border = table.getBorder();
			if (border == null || !(border instanceof TitledBorder) || !((TitledBorder)border).getTitle().equals(title))
				toolkit.setTitle(table, title);
		}

		Insets insets = table.getInsets();
		tableLayoutX = insets.left;
		tableLayoutY = insets.top;
		tableLayoutWidth = table.getWidth() - insets.left - insets.right;
		tableLayoutHeight = table.getHeight() - insets.top - insets.bottom;

		super.layout();

		List<Cell> cells = getCells();
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
				for (SwingTableLayout layout : debugLayouts)
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
			g.setColor(rect.isCell ? Color.red : Color.green);
			g.draw(rect);
		}
	}

	public void clearDebugRectangles () {
		if (debugRects != null) debugLayouts.remove(this);
		debugRects = null;
	}

	public void addDebugRectangle (boolean isCell, int x, int y, int w, int h) {
		if (debugRects == null) {
			debugRects = new ArrayList();
			debugLayouts.add(this);
		}
		debugRects.add(new DebugRect(isCell, x, y, w, h));
	}

	public Table getTable () {
		return table;
	}

	static private class DebugRect extends Rectangle {
		final boolean isCell;

		public DebugRect (boolean isCell, int x, int y, int width, int height) {
			super(x, y, width - 1, height - 1);
			this.isCell = isCell;
		}
	}
}
