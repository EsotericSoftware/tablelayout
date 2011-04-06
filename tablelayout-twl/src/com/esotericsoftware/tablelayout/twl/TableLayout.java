
package com.esotericsoftware.tablelayout.twl;

import com.esotericsoftware.tablelayout.BaseTableLayout;
import com.esotericsoftware.tablelayout.Cell;

import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.Widget;

public class TableLayout extends BaseTableLayout<Widget> {
	static {
		addClassPrefix("de.matthiasmann.twl.");
	}

	Table table;

	public void layout () {
		tableLayoutX = table.getBorderLeft();
		tableLayoutY = table.getBorderTop();
		tableLayoutWidth = table.getInnerWidth();
		tableLayoutHeight = table.getInnerHeight();

		super.layout();

		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;
			Widget cellWidget = (Widget)c.widget;
			cellWidget.setPosition(c.widgetX, c.widgetY);
			cellWidget.setSize(c.widgetWidth, c.widgetHeight);
		}
	}

	public void addChild (Widget parent, Widget child, String layoutString) {
		parent.add(child);
	}

	public void removeChild (Widget parent, Widget child) {
		parent.removeChild(child);
	}

	public Widget wrap (Object object) {
		if (object instanceof String) return new Label((String)object);
		if (object == null) return new Widget();
		return super.wrap(object);
	}

	public BaseTableLayout newTableLayout () {
		TableLayout layout = new Table().layout;
		layout.setParent(this);
		return layout;
	}

	public Widget newStack () {
		Widget stack = new Widget() {
			protected void layout () {
				layoutChildrenFullInnerArea();
			}

			public int getMinWidth () {
				int width = 0;
				for (int i = 0, n = getNumChildren(); i < n; i++)
					width = Math.max(width, getChild(i).getMinWidth());
				return width;
			}

			public int getMinHeight () {
				int height = 0;
				for (int i = 0, n = getNumChildren(); i < n; i++)
					height = Math.max(height, getChild(i).getMinHeight());
				return height;
			}
		};
		stack.setTheme("");
		return stack;
	}

	public int getMinWidth (Widget widget) {
		return widget.getMinWidth();
	}

	public int getMinHeight (Widget widget) {
		return widget.getMinHeight();
	}

	public int getPrefWidth (Widget widget) {
		return widget.getPreferredWidth();
	}

	public int getPrefHeight (Widget widget) {
		return widget.getPreferredHeight();
	}

	public int getMaxWidth (Widget widget) {
		return widget.getMaxWidth();
	}

	public int getMaxHeight (Widget widget) {
		return widget.getMaxHeight();
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
