
package com.esotericsoftware.tablelayout;

import static com.esotericsoftware.tablelayout.BaseTableLayout.*;

public class Cell {
	public Integer minWidth, minHeight;
	public Integer prefWidth, prefHeight;
	public Integer maxWidth, maxHeight;
	public Integer spaceTop, spaceLeft, spaceBottom, spaceRight;
	public Integer padTop, padLeft, padBottom, padRight;
	public Float fillWidth, fillHeight;
	public Integer align;
	public Integer expandWidth, expandHeight;
	public Boolean ignore;
	public Integer colspan;
	public Boolean uniformWidth, uniformHeight;
	public String name;

	public Object widget;
	public int widgetX, widgetY;
	public int widgetWidth, widgetHeight;

	boolean endRow;
	int column, row;
	int cellAboveIndex = -1;
	int padTopTemp, padLeftTemp, padBottomTemp, padRightTemp;

	Cell () {
	}

	void set (Cell defaults) {
		minWidth = defaults.minWidth;
		minHeight = defaults.minHeight;
		prefWidth = defaults.prefWidth;
		prefHeight = defaults.prefHeight;
		maxWidth = defaults.maxWidth;
		maxHeight = defaults.maxHeight;
		spaceTop = defaults.spaceTop;
		spaceLeft = defaults.spaceLeft;
		spaceBottom = defaults.spaceBottom;
		spaceRight = defaults.spaceRight;
		padTop = defaults.padTop;
		padLeft = defaults.padLeft;
		padBottom = defaults.padBottom;
		padRight = defaults.padRight;
		fillWidth = defaults.fillWidth;
		fillHeight = defaults.fillHeight;
		align = defaults.align;
		expandWidth = defaults.expandWidth;
		expandHeight = defaults.expandHeight;
		ignore = defaults.ignore;
		colspan = defaults.colspan;
		uniformWidth = defaults.uniformWidth;
		uniformHeight = defaults.uniformHeight;
	}

	void merge (Cell cell) {
		if (cell == null) return;
		if (cell.minWidth != null) minWidth = cell.minWidth;
		if (cell.minHeight != null) minHeight = cell.minHeight;
		if (cell.prefWidth != null) prefWidth = cell.prefWidth;
		if (cell.prefHeight != null) prefHeight = cell.prefHeight;
		if (cell.maxWidth != null) maxWidth = cell.maxWidth;
		if (cell.maxHeight != null) maxHeight = cell.maxHeight;
		if (cell.spaceTop != null) spaceTop = cell.spaceTop;
		if (cell.spaceLeft != null) spaceLeft = cell.spaceLeft;
		if (cell.spaceBottom != null) spaceBottom = cell.spaceBottom;
		if (cell.spaceRight != null) spaceRight = cell.spaceRight;
		if (cell.padTop != null) padTop = cell.padTop;
		if (cell.padLeft != null) padLeft = cell.padLeft;
		if (cell.padBottom != null) padBottom = cell.padBottom;
		if (cell.padRight != null) padRight = cell.padRight;
		if (cell.fillWidth != null) fillWidth = cell.fillWidth;
		if (cell.fillHeight != null) fillHeight = cell.fillHeight;
		if (cell.align != null) align = cell.align;
		if (cell.expandWidth != null) expandWidth = cell.expandWidth;
		if (cell.expandHeight != null) expandHeight = cell.expandHeight;
		if (cell.ignore != null) ignore = cell.ignore;
		if (cell.colspan != null) colspan = cell.colspan;
		if (cell.uniformWidth != null) uniformWidth = cell.uniformWidth;
		if (cell.uniformHeight != null) uniformHeight = cell.uniformHeight;
	}

	static Cell defaults () {
		Cell defaults = new Cell();
		defaults.minWidth = MIN;
		defaults.minHeight = MIN;
		defaults.prefWidth = PREF;
		defaults.prefHeight = PREF;
		defaults.maxWidth = MAX;
		defaults.maxHeight = MAX;
		defaults.spaceTop = 0;
		defaults.spaceLeft = 0;
		defaults.spaceBottom = 0;
		defaults.spaceRight = 0;
		defaults.padTop = 0;
		defaults.padLeft = 0;
		defaults.padBottom = 0;
		defaults.padRight = 0;
		defaults.fillWidth = 0f;
		defaults.fillHeight = 0f;
		defaults.align = CENTER;
		defaults.expandWidth = 0;
		defaults.expandHeight = 0;
		defaults.ignore = false;
		defaults.colspan = 1;
		return defaults;
	}
}
