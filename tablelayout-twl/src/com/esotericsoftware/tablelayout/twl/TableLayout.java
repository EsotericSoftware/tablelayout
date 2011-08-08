
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
		setLayoutSize(table.getBorderLeft(), table.getBorderTop(), table.getInnerWidth(), table.getInnerHeight());

		super.layout();

		List<Cell> cells = getCells();
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.getIgnore()) continue;
			Widget cellWidget = (Widget)c.getWidget();
			cellWidget.setPosition(c.getWidgetX(), c.getWidgetY());
			cellWidget.setSize(c.getWidgetWidth(), c.getWidgetHeight());
		}
	}

	public void invalidate () {
		getTable().invalidateLayout();
	}

	public void invalidateHierarchy () {
		getTable().invalidateLayout();
	}
}
