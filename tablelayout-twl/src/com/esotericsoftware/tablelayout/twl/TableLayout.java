
package com.esotericsoftware.tablelayout.twl;

import java.util.List;

import com.esotericsoftware.tablelayout.BaseTableLayout;
import com.esotericsoftware.tablelayout.Cell;

import de.matthiasmann.twl.Widget;

public class TableLayout extends BaseTableLayout<Widget, Table, TableLayout, TwlToolkit> {
	public TableLayout () {
		super(TwlToolkit.instance);
	}

	public TableLayout (TwlToolkit toolkit) {
		super(toolkit);
	}

	public void layout () {
		Table table = getTable();
		super.layout(table.getBorderLeft(), table.getBorderTop(), table.getInnerWidth(), table.getInnerHeight());

		List<Cell> cells = getCells();
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.getIgnore()) continue;
			Widget cellWidget = (Widget)c.getWidget();
			cellWidget.setPosition((int)c.getWidgetX(), (int)c.getWidgetY());
			cellWidget.setSize((int)c.getWidgetWidth(), (int)c.getWidgetHeight());
		}
	}

	public void invalidate () {
		getTable().invalidateLayout();
	}

	public void invalidateHierarchy () {
		getTable().invalidateLayout();
	}
}
