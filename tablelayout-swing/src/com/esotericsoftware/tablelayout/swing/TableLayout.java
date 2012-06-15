
package com.esotericsoftware.tablelayout.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import com.esotericsoftware.tablelayout.BaseTableLayout;
import com.esotericsoftware.tablelayout.Cell;
import com.esotericsoftware.tablelayout.swing.SwingToolkit.DebugRect;

public class TableLayout extends BaseTableLayout<Component, Table, TableLayout, SwingToolkit> {
	ArrayList<DebugRect> debugRects;

	public TableLayout () {
		super(SwingToolkit.instance);
	}

	public TableLayout (SwingToolkit toolkit) {
		super(toolkit);
	}

	public void layout () {
		Table table = getTable();
		Insets insets = table.getInsets();
		setLayoutSize(insets.left, insets.top, //
			table.getWidth() - insets.left - insets.right, //
			table.getHeight() - insets.top - insets.bottom);

		super.layout();

		List<Cell> cells = getCells();
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.getIgnore()) continue;
			Component component = (Component)c.getWidget();
			component.setLocation(c.getWidgetX(), c.getWidgetY());
			component.setSize(c.getWidgetWidth(), c.getWidgetHeight());
		}

		if (getDebug() != DEBUG_NONE) SwingToolkit.startDebugTimer();
	}

	public void invalidate () {
		super.invalidate();
		if (getTable().isValid()) getTable().invalidate();
	}

	public void invalidateHierarchy () {
		if (getTable().isValid()) getTable().invalidate();
	}

	void drawDebug () {
		Graphics2D g = (Graphics2D)getTable().getGraphics();
		if (g == null) return;
		g.setColor(Color.red);
		for (DebugRect rect : debugRects) {
			if ((rect.type & DEBUG_CELL) != 0) g.setColor(Color.red);
			if ((rect.type & DEBUG_WIDGET) != 0) g.setColor(Color.green);
			if ((rect.type & DEBUG_TABLE) != 0) g.setColor(Color.blue);
			g.draw(rect);
		}
	}
}
