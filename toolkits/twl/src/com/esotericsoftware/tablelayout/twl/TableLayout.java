
package com.esotericsoftware.tablelayout.twl;

import java.util.ArrayList;

import javax.swing.JPanel;

import com.esotericsoftware.tablelayout.BaseTableLayout;

import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.Widget;

public class TableLayout extends BaseTableLayout<Widget> {
	static {
		addClassPrefix("de.matthiasmann.twl.");
	}

	private TableLayoutWidget widget = new TableLayoutWidget();

	public TableLayout () {
	}

	public TableLayout (String tableText) {
		super(tableText);
	}

	private TableLayout (TableLayout parent) {
		super(parent);
	}

	public void layout () {
		tableLayoutX = widget.getBorderLeft();
		tableLayoutY = widget.getBorderTop();
		tableLayoutWidth = widget.getInnerWidth();
		tableLayoutHeight = widget.getInnerHeight();
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

	protected void addWidget (Widget child) {
		widget.add(child);
	}

	protected void removeWidget (Widget child) {
		widget.removeChild(child);
	}

	public void invalidate () {
		widget.invalidateLayout();
	}

	protected TableLayout newTableLayout () {
		return new TableLayout(this);
	}

	protected Label newLabel (String text) {
		return new Label(text);
	}

	protected void setTitle (Widget parent, String title) {
	}

	protected void addChild (Widget parent, Widget child, String layoutString) {
		parent.add(child);
	}

	protected Widget wrap (Object object) {
		if (object instanceof Widget) return (Widget)object;
		if (object == null) return new Widget();
		throw new IllegalArgumentException("Unknown object: " + object);
	}

	protected int getMinWidth (Widget widget) {
		return widget.getMinWidth();
	}

	protected int getMinHeight (Widget widget) {
		return widget.getMinHeight();
	}

	protected int getPrefWidth (Widget widget) {
		return widget.getPreferredWidth();
	}

	protected int getPrefHeight (Widget widget) {
		return widget.getPreferredHeight();
	}

	protected int getMaxWidth (Widget widget) {
		return widget.getMaxWidth();
	}

	protected int getMaxHeight (Widget widget) {
		return widget.getMaxHeight();
	}

	protected TableLayout getTableLayout (Object object) {
		if (object instanceof TableLayout) return (TableLayout)object;
		return null;
	}

	public Widget getWidget () {
		return widget;
	}

	class TableLayoutWidget extends Widget {
		public TableLayoutWidget () {
			setTheme("");
		}

		protected void layout () {
			TableLayout.this.layout();
		}

		public int getMinWidth () {
			return TableLayout.this.totalMinWidth;
		}

		public int getMinHeight () {
			return TableLayout.this.totalMinHeight;
		}

		public int getPreferredWidth () {
			return TableLayout.this.totalPrefWidth;
		}

		public int getPreferredHeight () {
			return TableLayout.this.totalPrefHeight;
		}

		public void invalidateLayout () {
			super.invalidateLayout();
		}

		protected void paintOverlay (GUI gui) {
			super.paintOverlay(gui);
		}
	}
}
