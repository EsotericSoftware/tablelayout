/*******************************************************************************
 * Copyright (c) 2011, Nathan Sweet <nathan.sweet@gmail.com>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package com.esotericsoftware.tablelayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

// BOZO - Support inserting cells/rows.

/** Base layout functionality.
 * @author Nathan Sweet */
abstract public class BaseTableLayout<C, T extends C, L extends BaseTableLayout, K extends Toolkit<C, T, L>> {
	static public final int CENTER = 1 << 0;
	static public final int TOP = 1 << 1;
	static public final int BOTTOM = 1 << 2;
	static public final int LEFT = 1 << 3;
	static public final int RIGHT = 1 << 4;

	/** Scales the source to fit the target while keeping the same aspect ratio. This may cause the source to be smaller than the
	 * target in one dimension. */
	static public final int SCALE_FIT = 1 << 1;
	/** Scales the source to completely fill the target while keeping the same aspect ratio. This may cause the source to be larger
	 * than the target in one dimension. */
	static public final int SCALE_FILL = 1 << 2;
	/** Scales the source to completely fill the target. This may cause the source to not keep the same aspect ratio. */
	static public final int SCALE_STRETCH = 1 << 3;

	static public final String MIN = "min";
	static public final String PREF = "pref";
	static public final String MAX = "max";

	static public final int DEBUG_NONE = 0;
	static public final int DEBUG_ALL = 1 << 0;
	static public final int DEBUG_TABLE = 1 << 1;
	static public final int DEBUG_CELL = 1 << 2;
	static public final int DEBUG_WIDGET = 1 << 3;

	K toolkit;
	T table;
	HashMap<String, C> nameToWidget = new HashMap(8);
	HashMap<C, Cell> widgetToCell = new HashMap(8);
	private int columns, rows;

	private final ArrayList<Cell> cells = new ArrayList(4);
	private final Cell cellDefaults = Cell.defaults(this);
	private final ArrayList<Cell> columnDefaults = new ArrayList(2);
	private Cell rowDefaults;

	private int layoutX, layoutY;
	private int layoutWidth, layoutHeight;

	private boolean sizeInvalid = true;
	private int[] columnMinWidth, rowMinHeight;
	private int[] columnPrefWidth, rowPrefHeight;
	private int tableMinWidth, tableMinHeight;
	private int tablePrefWidth, tablePrefHeight;
	private int[] columnWidth, rowHeight;
	private float[] expandWidth, expandHeight;
	private int[] columnWeightedWidth, rowWeightedHeight;

	String width, height;
	String padTop, padLeft, padBottom, padRight;
	int align = CENTER;
	int debug = DEBUG_NONE;

	public BaseTableLayout (K toolkit) {
		this.toolkit = toolkit;
	}

	public void invalidate () {
		sizeInvalid = true;
	}

	abstract public void invalidateHierarchy ();

	/** The position within it's parent and size of the widget that will be laid out. Must be set before layout. */
	public void setLayoutSize (int tableLayoutX, int tableLayoutY, int tableLayoutWidth, int tableLayoutHeight) {
		this.layoutX = tableLayoutX;
		this.layoutY = tableLayoutY;
		this.layoutWidth = tableLayoutWidth;
		this.layoutHeight = tableLayoutHeight;
	}

	/** Sets the name of a widget so it may be referenced in {@link #parse(String)}. */
	public C register (String name, C widget) {
		if (name == null) throw new IllegalArgumentException("name cannot be null.");
		name = name.toLowerCase().trim();
		if (nameToWidget.containsKey(name)) throw new IllegalArgumentException("Name is already used: " + name);
		nameToWidget.put(name, widget);
		return widget;
	}

	/** Parses a table description and adds the widgets and cells to the table. */
	public void parse (String tableDescription) {
		TableLayoutParser.parse(this, tableDescription);
	}

	/** Adds a new cell to the table with the specified widget.
	 * @param widget May be null to add a cell without a widget. */
	public Cell<C> add (C widget) { // BOZO - Add column description parsing.
		widget = toolkit.wrap((L)this, widget);

		Cell cell = new Cell(this);
		cell.widget = widget;

		widgetToCell.put(widget, cell);

		for (Entry<String, C> entry : nameToWidget.entrySet()) {
			if (widget == entry.getValue()) {
				cell.name = entry.getKey();
				break;
			}
		}

		if (cells.size() > 0) {
			// Set cell x and y.
			Cell lastCell = cells.get(cells.size() - 1);
			if (!lastCell.endRow) {
				cell.column = lastCell.column + lastCell.colspan;
				cell.row = lastCell.row;
			} else
				cell.row = lastCell.row + 1;
			// Set the index of the cell above.
			if (cell.row > 0) {
				outer:
				for (int i = cells.size() - 1; i >= 0; i--) {
					Cell other = cells.get(i);
					for (int column = other.column, nn = column + other.colspan; column < nn; column++) {
						if (other.column == cell.column) {
							cell.cellAboveIndex = i;
							break outer;
						}
					}
				}
			}
		}
		cells.add(cell);

		if (cell.column < columnDefaults.size()) {
			Cell columnDefaults = this.columnDefaults.get(cell.column);
			cell.set(columnDefaults != null ? columnDefaults : cellDefaults);
		} else
			cell.set(cellDefaults);
		cell.merge(rowDefaults);

		toolkit.addChild(table, widget, null);

		return cell;
	}

	public Cell<C> stack (C... widgets) { // BOZO - Add column description parsing.
		C stack = toolkit.newStack();
		for (int i = 0, n = widgets.length; i < n; i++)
			toolkit.addChild(stack, widgets[i], null);
		return add(stack);
	}

	/** Indicates that subsequent cells should be added to a new row and returns the cell values that will be used as the defaults
	 * for all cells in the new row. */
	public Cell row () {
		if (cells.size() > 0) endRow();
		rowDefaults = new Cell(this);
		return rowDefaults;
	}

	private void endRow () {
		int rowColumns = 0;
		for (int i = cells.size() - 1; i >= 0; i--) {
			Cell cell = cells.get(i);
			if (cell.endRow) break;
			rowColumns += cell.colspan;
		}
		columns = Math.max(columns, rowColumns);
		rows++;
		cells.get(cells.size() - 1).endRow = true;
		invalidate();
	}

	/** Gets the cell values that will be used as the defaults for all cells in the specified column. */
	public Cell columnDefaults (int column) {
		Cell cell = columnDefaults.size() > column ? columnDefaults.get(column) : null;
		if (cell == null) {
			cell = new Cell(this);
			cell.set(cellDefaults);
			if (column >= columnDefaults.size()) {
				for (int i = columnDefaults.size(); i < column; i++)
					columnDefaults.add(null);
				columnDefaults.add(cell);
			} else
				columnDefaults.set(column, cell);
		}
		return cell;
	}

	/** Removes all widgets and cells from the table (same as {@link #clear()}) and additionally resets all table properties and
	 * cell, column, and row defaults. */
	public void reset () {
		clear();
		padTop = null;
		padLeft = null;
		padBottom = null;
		padRight = null;
		align = CENTER;
		if (debug != DEBUG_NONE) toolkit.clearDebugRectangles((L)this);
		debug = DEBUG_NONE;
		cellDefaults.set(Cell.defaults(this));
		columnDefaults.clear();
		rowDefaults = null;
	}

	/** Removes all widgets and cells from the table. */
	public void clear () {
		for (int i = cells.size() - 1; i >= 0; i--)
			toolkit.removeChild(table, (C)cells.get(i).widget);
		cells.clear();
		nameToWidget.clear();
		widgetToCell.clear();
		rows = 0;
		columns = 0;
		invalidate();
	}

	/** Returns the widget with the specified name, anywhere in the table hierarchy. */
	public C getWidget (String name) {
		return nameToWidget.get(name.toLowerCase());
	}

	/** Returns all named widgets, anywhere in the table hierarchy. */
	public List<C> getWidgets () {
		return new ArrayList(nameToWidget.values());
	}

	/** Returns all widgets with the specified name prefix, anywhere in the table hierarchy. */
	public List<C> getWidgets (String namePrefix) {
		ArrayList<C> widgets = new ArrayList();
		for (Entry<String, C> entry : nameToWidget.entrySet())
			if (entry.getKey().startsWith(namePrefix)) widgets.add(entry.getValue());
		return widgets;
	}

	/** Returns the cell for the specified widget, anywhere in the table hierarchy. */
	public Cell getCell (C widget) {
		return widgetToCell.get(widget);
	}

	/** Returns the cell with the specified name, anywhere in the table hierarchy. */
	public Cell getCell (String name) {
		return getCell(getWidget(name));
	}

	/** Returns all cells, anywhere in the table hierarchy. */
	public List<Cell> getAllCells () {
		return new ArrayList(widgetToCell.values());
	}

	/** Returns all cells with the specified name prefix, anywhere in the table hierarchy. */
	public List<Cell> getAllCells (String namePrefix) {
		ArrayList<Cell> cells = new ArrayList();
		for (Cell cell : widgetToCell.values())
			if (cell.name.startsWith(namePrefix)) cells.add(cell);
		return cells;
	}

	/** Returns the cells for this table. */
	public List<Cell> getCells () {
		return cells;
	}

	/** Sets the widget in the cell with the specified name. */
	public void setWidget (String name, C widget) {
		getCell(name).setWidget(widget);
	}

	/** Sets that this table is nested under the specified parent. This allows the root table to look up widgets and cells in nested
	 * tables, for convenience. */
	public void setParent (BaseTableLayout parent) {
		// Shared per table hierarchy.
		nameToWidget = parent.nameToWidget;
		widgetToCell = parent.widgetToCell;
	}

	public void setToolkit (K toolkit) {
		this.toolkit = toolkit;
	}

	/** Returns the widget that will be laid out. */
	public T getTable () {
		return table;
	}

	/** Sets the widget that will be laid out. */
	public void setTable (T table) {
		this.table = table;
	}

	/** The x position within it's parent of the widget that will be laid out. Set by {@link #setLayoutSize(int, int, int, int)}
	 * before layout. */
	public int getLayoutX () {
		return layoutX;
	}

	/** The y position within it's parent of the widget that will be laid out. Set by {@link #setLayoutSize(int, int, int, int)}
	 * before layout. */
	public int getLayoutY () {
		return layoutY;
	}

	/** The width of the widget that will be laid out. Set by {@link #setLayoutSize(int, int, int, int)} before layout. */
	public int getLayoutWidth () {
		return layoutWidth;
	}

	/** The height of the widget that will be laid out. Set by {@link #setLayoutSize(int, int, int, int)} before layout. */
	public int getLayoutHeight () {
		return layoutHeight;
	}

	/** The minimum width of the table. */
	public int getMinWidth () {
		if (sizeInvalid) computeSize();
		return tableMinWidth;
	}

	/** The minimum size of the table. */
	public int getMinHeight () {
		if (sizeInvalid) computeSize();
		return tableMinHeight;
	}

	/** The preferred width of the table. */
	public int getPrefWidth () {
		if (sizeInvalid) computeSize();
		return tablePrefWidth;
	}

	/** The preferred height of the table. */
	public int getPrefHeight () {
		if (sizeInvalid) computeSize();
		return tablePrefHeight;
	}

	/** The cell values that will be used as the defaults for all cells. */
	public Cell defaults () {
		return cellDefaults;
	}

	public K getToolkit () {
		return toolkit;
	}

	/** The fixed size of the table. */
	public L size (String width, String height) {
		this.width = width;
		this.height = height;
		sizeInvalid = true;
		return (L)this;
	}

	/** The fixed width of the table, or null. */
	public L width (String width) {
		this.width = width;
		sizeInvalid = true;
		return (L)this;
	}

	/** The fixed height of the table, or null. */
	public L height (String height) {
		this.height = height;
		sizeInvalid = true;
		return (L)this;
	}

	/** The fixed size of the table. */
	public L size (int width, int height) {
		this.width = String.valueOf(width);
		this.height = String.valueOf(height);
		sizeInvalid = true;
		return (L)this;
	}

	/** The fixed width of the table. */
	public L width (int width) {
		this.width = String.valueOf(width);
		sizeInvalid = true;
		return (L)this;
	}

	/** The fixed height of the table. */
	public L height (int height) {
		this.height = String.valueOf(height);
		sizeInvalid = true;
		return (L)this;
	}

	/** Padding around the table. */
	public L pad (String pad) {
		padTop = pad;
		padLeft = pad;
		padBottom = pad;
		padRight = pad;
		sizeInvalid = true;
		return (L)this;
	}

	/** Padding around the table. */
	public L pad (String top, String left, String bottom, String right) {
		padTop = top;
		padLeft = left;
		padBottom = bottom;
		padRight = right;
		sizeInvalid = true;
		return (L)this;
	}

	/** Padding at the top of the table. */
	public L padTop (String padTop) {
		this.padTop = padTop;
		sizeInvalid = true;
		return (L)this;
	}

	/** Padding at the left of the table. */
	public L padLeft (String padLeft) {
		this.padLeft = padLeft;
		sizeInvalid = true;
		return (L)this;
	}

	/** Padding at the bottom of the table. */
	public L padBottom (String padBottom) {
		this.padBottom = padBottom;
		sizeInvalid = true;
		return (L)this;
	}

	/** Padding at the right of the table. */
	public L padRight (String padRight) {
		this.padRight = padRight;
		sizeInvalid = true;
		return (L)this;
	}

	/** Padding around the table. */
	public L pad (int pad) {
		padTop = String.valueOf(pad);
		padLeft = String.valueOf(pad);
		padBottom = String.valueOf(pad);
		padRight = String.valueOf(pad);
		sizeInvalid = true;
		return (L)this;
	}

	/** Padding around the table. */
	public L pad (int top, int left, int bottom, int right) {
		padTop = String.valueOf(top);
		padLeft = String.valueOf(left);
		padBottom = String.valueOf(bottom);
		padRight = String.valueOf(right);
		sizeInvalid = true;
		return (L)this;
	}

	/** Padding at the top of the table. */
	public L padTop (int padTop) {
		this.padTop = String.valueOf(padTop);
		sizeInvalid = true;
		return (L)this;
	}

	/** Padding at the left of the table. */
	public L padLeft (int padLeft) {
		this.padLeft = String.valueOf(padLeft);
		sizeInvalid = true;
		return (L)this;
	}

	/** Padding at the bottom of the table. */
	public L padBottom (int padBottom) {
		this.padBottom = String.valueOf(padBottom);
		sizeInvalid = true;
		return (L)this;
	}

	/** Padding at the right of the table. */
	public L padRight (int padRight) {
		this.padRight = String.valueOf(padRight);
		sizeInvalid = true;
		return (L)this;
	}

	/** Alignment of the table within the widget being laid out. Set to {@link #CENTER}, {@link #TOP}, {@link #BOTTOM},
	 * {@link #LEFT}, {@link #RIGHT}, or any combination of those. */
	public L align (int align) {
		this.align = align;
		return (L)this;
	}

	/** Alignment of the table within the widget being laid out. Set to "center", "top", "bottom", "left", "right", or a string
	 * containing any combination of those. */
	public L align (String value) {
		align = 0;
		if (value.contains("center")) align |= CENTER;
		if (value.contains("left")) align |= LEFT;
		if (value.contains("right")) align |= RIGHT;
		if (value.contains("top")) align |= TOP;
		if (value.contains("bottom")) align |= BOTTOM;
		return (L)this;
	}

	/** Sets the alignment of the table within the widget being laid out to {@link #CENTER}. */
	public L center () {
		align |= CENTER;
		return (L)this;
	}

	/** Sets the alignment of the table within the widget being laid out to {@link #TOP}. */
	public L top () {
		align |= TOP;
		align &= ~BOTTOM;
		return (L)this;
	}

	/** Sets the alignment of the table within the widget being laid out to {@link #LEFT}. */
	public L left () {
		align |= LEFT;
		align &= ~RIGHT;
		return (L)this;
	}

	/** Sets the alignment of the table within the widget being laid out to {@link #BOTTOM}. */
	public L bottom () {
		align |= BOTTOM;
		align &= ~TOP;
		return (L)this;
	}

	/** Sets the alignment of the table within the widget being laid out to {@link #RIGHT}. */
	public L right () {
		align |= RIGHT;
		align &= ~LEFT;
		return (L)this;
	}

	/** Turns on all debug lines. */
	public L debug () {
		this.debug = DEBUG_ALL;
		invalidate();
		return (L)this;
	}

	/** Turns on debug lines. Set to {@value #DEBUG_ALL}, {@value #DEBUG_TABLE}, {@value #DEBUG_CELL}, {@value #DEBUG_WIDGET}, or
	 * any combination of those. Set to {@value #DEBUG_NONE} to disable. */
	public L debug (int debug) {
		this.debug = debug;
		if (debug == DEBUG_NONE)
			toolkit.clearDebugRectangles((L)this);
		else
			invalidate();
		return (L)this;
	}

	/** Turns on debug lines. Set to "all", "table", "cell", "widget", or a string containing any combination of those. Set to null
	 * to disable. */
	public L debug (String value) {
		debug = 0;
		if (value == null) return (L)this;
		if (value.equalsIgnoreCase("true")) debug |= DEBUG_ALL;
		if (value.contains("all")) debug |= DEBUG_ALL;
		if (value.contains("cell")) debug |= DEBUG_CELL;
		if (value.contains("table")) debug |= DEBUG_TABLE;
		if (value.contains("widget")) debug |= DEBUG_WIDGET;
		if (debug == DEBUG_NONE)
			toolkit.clearDebugRectangles((L)this);
		else
			invalidate();
		return (L)this;
	}

	public int getDebug () {
		return debug;
	}

	public String getWidth () {
		return width;
	}

	public String getHeight () {
		return height;
	}

	public String getPadTop () {
		return padTop;
	}

	public String getPadLeft () {
		return padLeft;
	}

	public String getPadBottom () {
		return padBottom;
	}

	public String getPadRight () {
		return padRight;
	}

	public int getAlign () {
		return align;
	}

	/** Returns the row index for the y coordinate, or -1 if there are no cells. */
	public int getRow (float y) {
		int row = 0;
		y += toolkit.height((L)this, padTop);
		int i = 0, n = cells.size();
		if (n == 0) return -1;
		// Skip first row.
		while (i < n && !cells.get(i).isEndRow())
			i++;
		while (i < n) {
			Cell c = cells.get(i++);
			if (c.getIgnore()) continue;
			if (c.widgetY + c.computedPadTop > y) break;
			if (c.endRow) row++;
		}
		return rows - row;
	}

	private int[] ensureSize (int[] array, int size) {
		if (array == null || array.length < size) return new int[size];
		for (int i = 0, n = array.length; i < n; i++)
			array[i] = 0;
		return array;
	}

	private float[] ensureSize (float[] array, int size) {
		if (array == null || array.length < size) return new float[size];
		for (int i = 0, n = array.length; i < n; i++)
			array[i] = 0;
		return array;
	}

	private void computeSize () {
		sizeInvalid = false;

		Toolkit toolkit = this.toolkit;
		ArrayList<Cell> cells = this.cells;

		if (cells.size() > 0 && !cells.get(cells.size() - 1).endRow) endRow();

		columnMinWidth = ensureSize(columnMinWidth, columns);
		rowMinHeight = ensureSize(rowMinHeight, rows);
		columnPrefWidth = ensureSize(columnPrefWidth, columns);
		rowPrefHeight = ensureSize(rowPrefHeight, rows);
		columnWidth = ensureSize(columnWidth, columns);
		rowHeight = ensureSize(rowHeight, rows);
		expandWidth = ensureSize(expandWidth, columns);
		expandHeight = ensureSize(expandHeight, rows);

		int spaceRightLast = 0;
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;

			// Collect columns/rows that expand.
			if (c.expandY != 0 && expandHeight[c.row] == 0) expandHeight[c.row] = c.expandY;
			if (c.colspan == 1 && c.expandX != 0 && expandWidth[c.column] == 0) expandWidth[c.column] = c.expandX;

			// Compute combined padding/spacing for cells.
			// Spacing between widgets isn't additive, the larger is used. Also, no spacing around edges.
			c.computedPadLeft = c.column == 0 ? toolkit.width(this, c.padLeft) : toolkit.width(this, c.padLeft)
				+ Math.max(0, toolkit.width(this, c.spaceLeft) - spaceRightLast);
			c.computedPadTop = c.cellAboveIndex == -1 ? toolkit.height(this, c.padTop) : toolkit.height(this, c.padTop)
				+ Math.max(0, toolkit.height(this, c.spaceTop) - toolkit.height(this, cells.get(c.cellAboveIndex).spaceBottom));
			int spaceRight = toolkit.width(this, c.spaceRight);
			c.computedPadRight = c.column + c.colspan == columns ? toolkit.width(this, c.padRight) : toolkit.width(this, c.padRight)
				+ spaceRight;
			c.computedPadBottom = c.row == rows - 1 ? toolkit.height(this, c.padBottom) : toolkit.height(this, c.padBottom)
				+ toolkit.height(this, c.spaceBottom);
			spaceRightLast = spaceRight;

			// Determine minimum and preferred cell sizes.
			int prefWidth = toolkit.getWidgetWidth(this, (C)c.widget, c.prefWidth);
			int prefHeight = toolkit.getWidgetHeight(this, (C)c.widget, c.prefHeight);
			int minWidth = toolkit.getWidgetWidth(this, (C)c.widget, c.minWidth);
			int minHeight = toolkit.getWidgetHeight(this, (C)c.widget, c.minHeight);
			int maxWidth = toolkit.getWidgetWidth(this, (C)c.widget, c.maxWidth);
			int maxHeight = toolkit.getWidgetHeight(this, (C)c.widget, c.maxHeight);
			if (prefWidth < minWidth) prefWidth = minWidth;
			if (prefHeight < minHeight) prefHeight = minHeight;
			if (maxWidth > 0 && prefWidth > maxWidth) prefWidth = maxWidth;
			if (maxHeight > 0 && prefHeight > maxHeight) prefHeight = maxHeight;

			if (c.colspan == 1) { // Spanned column min and pref width is added later.
				int hpadding = c.computedPadLeft + c.computedPadRight;
				columnPrefWidth[c.column] = Math.max(columnPrefWidth[c.column], prefWidth + hpadding);
				columnMinWidth[c.column] = Math.max(columnMinWidth[c.column], minWidth + hpadding);
			}
			int vpadding = c.computedPadTop + c.computedPadBottom;
			rowPrefHeight[c.row] = Math.max(rowPrefHeight[c.row], prefHeight + vpadding);
			rowMinHeight[c.row] = Math.max(rowMinHeight[c.row], minHeight + vpadding);
		}

		// Colspan with expand will expand all spanned columns if none of the spanned columns have expand.
		outer:
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;
			if (c.expandX == 0) continue;
			for (int column = c.column, nn = column + c.colspan; column < nn; column++)
				if (expandWidth[column] != 0) continue outer;
			for (int column = c.column, nn = column + c.colspan; column < nn; column++)
				expandWidth[column] = c.expandX;
		}

		// Distribute any additional min and pref width added by colspanned cells to the columns spanned.
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;
			if (c.colspan == 1) continue;

			int minWidth = toolkit.getWidgetWidth(this, (C)c.widget, c.minWidth);
			int prefWidth = toolkit.getWidgetWidth(this, (C)c.widget, c.prefWidth);
			int maxWidth = toolkit.getWidgetWidth(this, (C)c.widget, c.maxWidth);
			if (prefWidth < minWidth) prefWidth = minWidth;
			if (maxWidth > 0 && prefWidth > maxWidth) prefWidth = maxWidth;

			int spannedMinWidth = 0, spannedPrefWidth = 0;
			for (int column = c.column, nn = column + c.colspan; column < nn; column++) {
				spannedMinWidth += columnMinWidth[column];
				spannedPrefWidth += columnPrefWidth[column];
			}

			// Distribute extra space using expand, if any columns have expand.
			float totalExpandWidth = 0;
			for (int column = c.column, nn = column + c.colspan; column < nn; column++)
				totalExpandWidth += expandWidth[column];

			int extraMinWidth = Math.max(0, minWidth - spannedMinWidth);
			int extraPrefWidth = Math.max(0, prefWidth - spannedPrefWidth);
			for (int column = c.column, nn = column + c.colspan; column < nn; column++) {
				float ratio = totalExpandWidth == 0 ? 1f / c.colspan : expandWidth[column] / totalExpandWidth;
				columnMinWidth[column] += extraMinWidth * ratio;
				columnPrefWidth[column] += extraPrefWidth * ratio;
			}
		}

		// Determine table min and pref size.
		tableMinWidth = 0;
		tableMinHeight = 0;
		tablePrefWidth = 0;
		tablePrefHeight = 0;
		for (int i = 0; i < columns; i++) {
			tableMinWidth += columnMinWidth[i];
			tablePrefWidth += columnPrefWidth[i];
		}
		for (int i = 0; i < rows; i++) {
			tableMinHeight += rowMinHeight[i];
			tablePrefHeight += Math.max(rowMinHeight[i], rowPrefHeight[i]);
		}
		int hpadding = toolkit.width(this, padLeft) + toolkit.width(this, padRight);
		int vpadding = toolkit.height(this, padTop) + toolkit.height(this, padBottom);
		int width = toolkit.width(this, this.width);
		int height = toolkit.height(this, this.height);
		tableMinWidth = Math.max(tableMinWidth + hpadding, width);
		tableMinHeight = Math.max(tableMinHeight + vpadding, height);
		tablePrefWidth = Math.max(tablePrefWidth + hpadding, tableMinWidth);
		tablePrefHeight = Math.max(tablePrefHeight + vpadding, tableMinHeight);
	}

	/** Positions and sizes children of the widget being laid out using the cell associated with each child. */
	public void layout () {
		Toolkit toolkit = this.toolkit;
		ArrayList<Cell> cells = this.cells;

		if (sizeInvalid) computeSize();

		int hpadding = toolkit.width(this, padLeft) + toolkit.width(this, padRight);
		int vpadding = toolkit.height(this, padTop) + toolkit.height(this, padBottom);

		// totalMinWidth/totalMinHeight are needed because tableMinWidth/tableMinHeight could be based on this.width or this.height.
		int totalMinWidth = 0, totalMinHeight = 0;
		float totalExpandWidth = 0, totalExpandHeight = 0;
		for (int i = 0; i < columns; i++) {
			totalMinWidth += columnMinWidth[i];
			totalExpandWidth += expandWidth[i];
		}
		for (int i = 0; i < rows; i++) {
			totalMinHeight += rowMinHeight[i];
			totalExpandHeight += expandHeight[i];
		}

		// Size columns and rows between min and pref size using (preferred - min) size to weight distribution of extra space.
		int[] columnWeightedWidth;
		int totalGrowWidth = tablePrefWidth - totalMinWidth;
		if (totalGrowWidth == 0)
			columnWeightedWidth = columnMinWidth;
		else {
			int extraWidth = Math.min(totalGrowWidth, Math.max(0, layoutWidth - totalMinWidth));
			columnWeightedWidth = this.columnWeightedWidth = ensureSize(this.columnWeightedWidth, columns);
			for (int i = 0; i < columns; i++) {
				int growWidth = columnPrefWidth[i] - columnMinWidth[i];
				float growRatio = growWidth / (float)totalGrowWidth;
				columnWeightedWidth[i] = columnMinWidth[i] + (int)(extraWidth * growRatio);
			}
		}

		int[] rowWeightedHeight;
		int totalGrowHeight = tablePrefHeight - totalMinHeight;
		if (totalGrowHeight == 0)
			rowWeightedHeight = rowMinHeight;
		else {
			rowWeightedHeight = this.rowWeightedHeight = ensureSize(this.rowWeightedHeight, rows);
			int extraHeight = Math.min(totalGrowHeight, Math.max(0, layoutHeight - totalMinHeight));
			for (int i = 0; i < rows; i++) {
				int growHeight = rowPrefHeight[i] - rowMinHeight[i];
				float growRatio = growHeight / (float)totalGrowHeight;
				rowWeightedHeight[i] = rowMinHeight[i] + (int)(extraHeight * growRatio);
			}
		}

		// Determine widget and cell sizes (before uniform/expand/fill).
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;

			int spannedWeightedWidth = 0;
			for (int column = c.column, nn = column + c.colspan; column < nn; column++)
				spannedWeightedWidth += columnWeightedWidth[column];
			int weightedHeight = rowWeightedHeight[c.row];

			int prefWidth = toolkit.getWidgetWidth(this, (C)c.widget, c.prefWidth);
			int prefHeight = toolkit.getWidgetHeight(this, (C)c.widget, c.prefHeight);
			int minWidth = toolkit.getWidgetWidth(this, (C)c.widget, c.minWidth);
			int minHeight = toolkit.getWidgetHeight(this, (C)c.widget, c.minHeight);
			int maxWidth = toolkit.getWidgetWidth(this, (C)c.widget, c.maxWidth);
			int maxHeight = toolkit.getWidgetHeight(this, (C)c.widget, c.maxHeight);
			if (prefWidth < minWidth) prefWidth = minWidth;
			if (prefHeight < minHeight) prefHeight = minHeight;
			if (maxWidth > 0 && prefWidth > maxWidth) prefWidth = maxWidth;
			if (maxHeight > 0 && prefHeight > maxHeight) prefHeight = maxHeight;

			c.widgetWidth = Math.min(spannedWeightedWidth - c.computedPadLeft - c.computedPadRight, prefWidth);
			c.widgetHeight = Math.min(weightedHeight - c.computedPadTop - c.computedPadBottom, prefHeight);

			if (c.colspan == 1) columnWidth[c.column] = Math.max(columnWidth[c.column], spannedWeightedWidth);
			rowHeight[c.row] = Math.max(rowHeight[c.row], weightedHeight);
		}

		// Uniform cells are all the same width/height.
		int uniformMaxWidth = 0, uniformMaxHeight = 0;
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;
			if (c.uniformX != null)
				uniformMaxWidth = Math.max(uniformMaxWidth, columnWidth[c.column] - c.computedPadLeft - c.computedPadRight);
			if (c.uniformY != null)
				uniformMaxHeight = Math.max(uniformMaxHeight, rowHeight[c.row] - c.computedPadTop - c.computedPadBottom);
		}
		if (uniformMaxWidth > 0 || uniformMaxHeight > 0) {
			outer:
			for (int i = 0, n = cells.size(); i < n; i++) {
				Cell c = cells.get(i);
				if (c.ignore) continue;
				if (uniformMaxWidth > 0 && c.uniformX != null) {
					int tempPadding = c.computedPadLeft + c.computedPadRight;
					int diff = uniformMaxWidth - (columnWidth[c.column] - tempPadding);
					if (diff > 0) {
						columnWidth[c.column] = uniformMaxWidth + tempPadding;
						tableMinWidth += diff;
						tablePrefWidth += diff;
					}
				}
				if (uniformMaxHeight > 0 && c.uniformY != null) {
					int tempPadding = c.computedPadTop + c.computedPadBottom;
					int diff = uniformMaxHeight - (rowHeight[c.row] - tempPadding);
					if (diff > 0) {
						rowHeight[c.row] = uniformMaxHeight + tempPadding;
						tableMinHeight += diff;
						tablePrefHeight += diff;
					}
				}
				continue outer;
			}
		}

		// Distribute remaining space to any expanding columns/rows.
		if (totalExpandWidth > 0) {
			int extra = layoutWidth - hpadding;
			for (int i = 0; i < columns; i++)
				extra -= columnWidth[i];
			int used = 0, lastIndex = 0;
			for (int i = 0; i < columns; i++) {
				if (expandWidth[i] == 0) continue;
				int amount = (int)(extra * expandWidth[i] / totalExpandWidth);
				columnWidth[i] += amount;
				used += amount;
				lastIndex = i;
			}
			columnWidth[lastIndex] += extra - used;
		}
		if (totalExpandHeight > 0) {
			int extra = layoutHeight - vpadding;
			for (int i = 0; i < rows; i++)
				extra -= rowHeight[i];
			int used = 0, lastIndex = 0;
			for (int i = 0; i < rows; i++) {
				if (expandHeight[i] == 0) continue;
				int amount = (int)(extra * expandHeight[i] / totalExpandHeight);
				rowHeight[i] += amount;
				used += amount;
				lastIndex = i;
			}
			rowHeight[lastIndex] += extra - used;
		}

		// Distribute any additional width added by colspanned cells to the columns spanned.
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;
			if (c.colspan == 1) continue;

			int extraWidth = 0;
			for (int column = c.column, nn = column + c.colspan; column < nn; column++)
				extraWidth += columnWeightedWidth[column] - columnWidth[column];
			extraWidth -= c.computedPadLeft + c.computedPadRight;

			extraWidth /= c.colspan;
			if (extraWidth > 0) {
				for (int column = c.column, nn = column + c.colspan; column < nn; column++)
					columnWidth[column] += extraWidth;
			}
		}

		// Determine table size.
		int tableWidth = 0, tableHeight = 0;
		for (int i = 0; i < columns; i++)
			tableWidth += columnWidth[i];
		int width = toolkit.width(this, this.width);
		tableWidth = Math.max(tableWidth + hpadding, width);

		for (int i = 0; i < rows; i++)
			tableHeight += rowHeight[i];
		int height = toolkit.height(this, this.height);
		tableHeight = Math.max(tableHeight + vpadding, height);

		// Position table within the container.
		int x = layoutX + toolkit.width(this, padLeft);
		if ((align & RIGHT) != 0)
			x += layoutWidth - tableWidth;
		else if ((align & LEFT) == 0) // Center
			x += (layoutWidth - tableWidth) / 2;

		int y = layoutY + toolkit.height(this, padTop);
		if ((align & BOTTOM) != 0)
			y += layoutHeight - tableHeight;
		else if ((align & TOP) == 0) // Center
			y += (layoutHeight - tableHeight) / 2;

		// Position widgets within cells.
		int currentX = x;
		int currentY = y;
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;

			int spannedCellWidth = 0;
			for (int column = c.column, nn = column + c.colspan; column < nn; column++)
				spannedCellWidth += columnWidth[column];
			spannedCellWidth -= c.computedPadLeft + c.computedPadRight;

			currentX += c.computedPadLeft;

			if (c.fillX > 0) {
				c.widgetWidth = (int)(spannedCellWidth * c.fillX);
				int maxWidth = toolkit.getWidgetWidth(this, (C)c.widget, c.maxWidth);
				if (maxWidth > 0) c.widgetWidth = Math.min(c.widgetWidth, maxWidth);
			}
			if (c.fillY > 0) {
				c.widgetHeight = (int)(rowHeight[c.row] * c.fillY) - c.computedPadTop - c.computedPadBottom;
				int maxHeight = toolkit.getWidgetHeight(this, (C)c.widget, c.maxHeight);
				if (maxHeight > 0) c.widgetHeight = Math.min(c.widgetHeight, maxHeight);
			}

			if (c.scaling != SCALE_STRETCH) {
				float sourceWidth = toolkit.getWidgetWidth(this, (C)c.widget, PREF);
				float sourceHeight = toolkit.getWidgetHeight(this, (C)c.widget, PREF);
				switch (c.scaling) {
				case SCALE_FIT: {
					float scale = c.widgetHeight / (float)c.widgetWidth > sourceHeight / sourceWidth ? c.widgetWidth / sourceWidth
						: c.widgetHeight / sourceHeight;
					c.widgetWidth = (int)(sourceWidth * scale);
					c.widgetHeight = (int)(sourceHeight * scale);
					break;
				}
				case SCALE_FILL: {
					float scale = c.widgetHeight / (float)c.widgetWidth < sourceHeight / sourceWidth ? c.widgetWidth / sourceWidth
						: c.widgetHeight / sourceHeight;
					c.widgetWidth = (int)(sourceWidth * scale);
					c.widgetHeight = (int)(sourceHeight * scale);
					break;
				}
				}
			}

			if ((c.align & LEFT) != 0)
				c.widgetX = currentX;
			else if ((c.align & RIGHT) != 0)
				c.widgetX = currentX + spannedCellWidth - c.widgetWidth;
			else
				c.widgetX = currentX + (spannedCellWidth - c.widgetWidth) / 2;

			if ((c.align & TOP) != 0)
				c.widgetY = currentY + c.computedPadTop;
			else if ((c.align & BOTTOM) != 0)
				c.widgetY = currentY + rowHeight[c.row] - c.widgetHeight - c.computedPadBottom;
			else
				c.widgetY = currentY + (rowHeight[c.row] - c.widgetHeight + c.computedPadTop - c.computedPadBottom) / 2;

			if (c.endRow) {
				currentX = x;
				currentY += rowHeight[c.row];
			} else
				currentX += spannedCellWidth + c.computedPadRight;
		}

		// Draw debug widgets and bounds.
		if (debug == DEBUG_NONE) return;
		toolkit.clearDebugRectangles(this);
		currentX = x;
		currentY = y;
		if ((debug & DEBUG_TABLE) != 0 || (debug & DEBUG_ALL) != 0) {
			toolkit.addDebugRectangle(this, DEBUG_TABLE, layoutX, layoutY, layoutWidth, layoutHeight);
			toolkit.addDebugRectangle(this, DEBUG_TABLE, x, y, tableWidth - hpadding, tableHeight - vpadding);
		}
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;

			// Widget bounds.
			if ((debug & DEBUG_WIDGET) != 0 || (debug & DEBUG_ALL) != 0)
				toolkit.addDebugRectangle(this, DEBUG_WIDGET, c.widgetX, c.widgetY, c.widgetWidth, c.widgetHeight);

			// Cell bounds.
			int spannedCellWidth = 0;
			for (int column = c.column, nn = column + c.colspan; column < nn; column++)
				spannedCellWidth += columnWidth[column];
			spannedCellWidth -= c.computedPadLeft + c.computedPadRight;
			currentX += c.computedPadLeft;
			if ((debug & DEBUG_CELL) != 0 || (debug & DEBUG_ALL) != 0) {
				toolkit.addDebugRectangle(this, DEBUG_CELL, currentX, currentY + c.computedPadTop, spannedCellWidth, rowHeight[c.row]
					- c.computedPadTop - c.computedPadBottom);
			}

			if (c.endRow) {
				currentX = x;
				currentY += rowHeight[c.row];
			} else
				currentX += spannedCellWidth + c.computedPadRight;
		}
	}
}
