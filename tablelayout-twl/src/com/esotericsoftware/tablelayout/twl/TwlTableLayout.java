
package com.esotericsoftware.tablelayout.twl;

import java.awt.Canvas;
import java.util.ArrayList;

import com.esotericsoftware.tablelayout.Cell;
import com.esotericsoftware.tablelayout.TableLayout;
import com.esotericsoftware.tablelayout.Toolkit;

import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.Widget;

public class TwlTableLayout extends TableLayout<Widget> {
	Table table;

	public Toolkit getToolkit () {
		return TwlToolkit.instance;
	}

	public void layout () {
		tableLayoutX = table.getBorderLeft();
		tableLayoutY = table.getBorderTop();
		tableLayoutWidth = table.getInnerWidth();
		tableLayoutHeight = table.getInnerHeight();

		super.layout();

		ArrayList<Cell> cells = getCells();
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;
			Widget cellWidget = (Widget)c.widget;
			cellWidget.setPosition(c.widgetX, c.widgetY);
			cellWidget.setSize(c.widgetWidth, c.widgetHeight);
		}
	}

	public void invalidate () {
		table.invalidateLayout();
	}

	public void clearDebugRectangles () {
	}

	public void addDebugRectangle (boolean isCell, int x, int y, int w, int h) {
	}

	public Table getTable () {
		return table;
	}
}
