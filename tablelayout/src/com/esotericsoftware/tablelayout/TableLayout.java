
package com.esotericsoftware.tablelayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

abstract public class TableLayout<T> {
	static public final int CENTER = 1 << 0;
	static public final int TOP = 1 << 1;
	static public final int BOTTOM = 1 << 2;
	static public final int LEFT = 1 << 3;
	static public final int RIGHT = 1 << 4;

	static public final int X = 1 << 5;
	static public final int Y = 1 << 6;

	static public final int MIN = -1;
	static public final int PREF = -2;
	static public final int MAX = -3;

	static private final int[][] intArrays = new int[8][];
	static private final int INT_columnMinWidth = 0;
	static private final int INT_rowMinHeight = 1;
	static private final int INT_columnPrefWidth = 2;
	static private final int INT_rowPrefHeight = 3;
	static private final int INT_columnMaxWidth = 4;
	static private final int INT_rowMaxHeight = 5;
	static private final int INT_columnWidth = 6;
	static private final int INT_rowHeight = 7;
	static private final float[][] floatArrays = new float[2][];
	static private final int FLOAT_expandWidth = 0;
	static private final int FLOAT_expandHeight = 1;

	public final Toolkit<T> toolkit = getToolkit();

	public int width, height;
	public float fillWidth, fillHeight;
	public int padTop, padLeft, padBottom, padRight;
	public int align = CENTER;
	public String title;
	public String debug;

	public int tableLayoutX, tableLayoutY;
	public int tableLayoutWidth, tableLayoutHeight;
	public int totalMinWidth, totalMinHeight;
	public int totalPrefWidth, totalPrefHeight;

	private final HashMap<String, T> nameToWidget;

	private final ArrayList<Cell> cells = new ArrayList();
	private Cell cellDefaults = Cell.defaults();
	private final ArrayList<Cell> columnDefaults = new ArrayList(4);
	private Cell rowDefaults;
	private int columns, rows;

	public TableLayout () {
		nameToWidget = new HashMap();
	}

	public TableLayout (TableLayout parent) {
		nameToWidget = parent.nameToWidget;
	}

	/**
	 * Sets the name of a widget so it may be referenced in {@link #parse(String)}.
	 */
	public T setName (String name, T widget) {
		name = name.toLowerCase();
		if (nameToWidget.containsKey(name)) throw new IllegalArgumentException("Name is already used: " + name);
		nameToWidget.put(name, widget);
		return widget;
	}

	/**
	 * Parses a TableLayout description and adds the cells and widgets to the table.
	 */
	public void parse (String tableDescription) {
		TableLayoutParser.parse(this, toolkit, tableDescription);
	}

	/**
	 * Adds a new cell to the table with the specified widget.
	 * @param widget May be null to add a cell without a widget.
	 */
	public Cell addCell (T widget) {
		Cell cell = new Cell();
		cell.widget = widget;

		for (Entry<String, T> entry : nameToWidget.entrySet()) {
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

		toolkit.addChild(getTable(), widget, null);

		return cell;
	}

	/**
	 * Indicates that subsequent cells should be added to a new row and returns the cell values that will be used as the defaults
	 * for all cells in the new row.
	 */
	public Cell startRow () {
		if (cells.size() > 0) endRow();
		rowDefaults = new Cell();
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
	}

	/**
	 * Gets the cell values that will be used as the defaults for all cells.
	 */
	public Cell getCellDefaults () {
		return cellDefaults;
	}

	/**
	 * Gets the cell values that will be used as the defaults for all cells in the specified column.
	 */
	public Cell getColumnDefaults (int column) {
		Cell cell = columnDefaults.size() > column ? columnDefaults.get(column) : null;
		if (cell == null) {
			cell = new Cell();
			cell.set(cellDefaults);
			if (column <= columnDefaults.size()) {
				for (int i = columnDefaults.size(); i < column; i++)
					columnDefaults.add(null);
				columnDefaults.add(cell);
			} else
				columnDefaults.set(column, cell);
		}
		return cell;
	}

	/**
	 * Removes all widgets and cells from the table.
	 */
	public void clear () {
		for (int i = cells.size() - 1; i >= 0; i--) {
			Cell cell = cells.get(i);
			toolkit.removeChild(getTable(), (T)cell.widget);
		}
		cells.clear();
		columnDefaults.clear();
		nameToWidget.clear();
		cellDefaults = Cell.defaults();
		debug = null;
		rows = 0;
		columns = 0;
		rowDefaults = null;
	}

	public T getWidget (String name) {
		return nameToWidget.get(name.toLowerCase());
	}

	public Cell getCell (T widget) {
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.widget == widget) return c;
		}
		return null;
	}

	public Cell getCell (String name) {
		return getCell(getWidget(name));
	}

	public ArrayList<Cell> getCells () {
		return cells;
	}

	/**
	 * Performs the actual layout.
	 */
	public void layout () {
		if (cells.size() > 0 && !cells.get(cells.size() - 1).endRow) endRow();

		// Determine minimum and preferred cell sizes. Also compute the combined padding/spacing for each cell.
		int[] columnMinWidth = new int[columns];
		int[] rowMinHeight = new int[rows];
		int[] columnPrefWidth = new int[columns];
		int[] rowPrefHeight = new int[rows];
		int spaceRightLast = 0;
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;

			// Spacing between widgets isn't additive, the larger is used. Also, no spacing around edges.
			c.padLeftTemp = c.column == 0 ? c.padLeft : c.padLeft + Math.max(0, c.spaceLeft - spaceRightLast);
			c.padTopTemp = c.cellAboveIndex == -1 ? c.padTop : c.padTop
				+ Math.max(0, c.spaceTop - cells.get(c.cellAboveIndex).spaceBottom);
			c.padRightTemp = c.column + c.colspan == columns ? c.padRight : c.padRight + c.spaceRight;
			c.padBottomTemp = c.row == rows - 1 ? c.padBottom : c.padBottom + c.spaceBottom;
			spaceRightLast = c.spaceRight;

			int prefWidth = toolkit.getWidth((T)c.widget, c.prefWidth);
			int prefHeight = toolkit.getHeight((T)c.widget, c.prefHeight);
			int minWidth = toolkit.getWidth((T)c.widget, c.minWidth);
			int minHeight = toolkit.getHeight((T)c.widget, c.minHeight);
			if (prefWidth < minWidth) prefWidth = minWidth;
			if (prefHeight < minHeight) prefHeight = minHeight;

			if (c.colspan == 1) {
				int padLeftRight = c.padLeftTemp + c.padRightTemp;
				columnPrefWidth[c.column] = Math.max(columnPrefWidth[c.column], prefWidth + padLeftRight);
				columnMinWidth[c.column] = Math.max(columnMinWidth[c.column], minWidth + padLeftRight);
			}
			int padTopBottom = c.padTopTemp + c.padBottomTemp;
			rowPrefHeight[c.row] = Math.max(rowPrefHeight[c.row], prefHeight + padTopBottom);
			rowMinHeight[c.row] = Math.max(rowMinHeight[c.row], minHeight + padTopBottom);
		}

		// Determine maximum cell sizes using (preferred - min) size to weight distribution of extra space.
		totalMinWidth = 0;
		totalMinHeight = 0;
		totalPrefWidth = 0;
		totalPrefHeight = 0;
		for (int i = 0; i < columns; i++) {
			totalMinWidth += columnMinWidth[i];
			totalPrefWidth += columnPrefWidth[i];
		}
		for (int i = 0; i < rows; i++) {
			totalMinHeight += rowMinHeight[i];
			totalPrefHeight += Math.max(rowMinHeight[i], rowPrefHeight[i]);
		}
		int width = fillWidth != 0 ? (int)(tableLayoutWidth * fillWidth) : this.width;
		int height = fillHeight != 0 ? (int)(tableLayoutHeight * fillHeight) : this.height;
		totalMinWidth = Math.max(totalMinWidth, width);
		totalMinHeight = Math.max(totalMinHeight, height);
		totalPrefWidth = Math.max(totalPrefWidth, width);
		totalPrefHeight = Math.max(totalPrefHeight, height);

		int[] columnMaxWidth;
		int tableLayoutWidth = this.tableLayoutWidth - padLeft - padRight;
		int totalGrowWidth = totalPrefWidth - totalMinWidth;
		if (totalGrowWidth == 0)
			columnMaxWidth = columnMinWidth;
		else {
			int extraWidth = Math.max(0, tableLayoutWidth - totalMinWidth);
			columnMaxWidth = new int[columns];
			for (int i = 0; i < columns; i++) {
				int growWidth = columnPrefWidth[i] - columnMinWidth[i];
				float growRatio = growWidth / (float)totalGrowWidth;
				columnMaxWidth[i] = columnMinWidth[i] + (int)(extraWidth * growRatio);
			}
		}

		int[] rowMaxHeight;
		int tableLayoutHeight = this.tableLayoutHeight - padTop - padBottom;
		int totalGrowHeight = totalPrefHeight - totalMinHeight;
		if (totalGrowHeight == 0)
			rowMaxHeight = rowMinHeight;
		else {
			int extraHeight = Math.max(0, tableLayoutHeight - padTop - padBottom - totalMinHeight);
			rowMaxHeight = new int[rows];
			for (int i = 0; i < rows; i++) {
				int growHeight = rowPrefHeight[i] - rowMinHeight[i];
				float growRatio = growHeight / (float)totalGrowHeight;
				rowMaxHeight[i] = rowMinHeight[i] + (int)(extraHeight * growRatio);
			}
		}

		// Determine widget and cell sizes (before uniform/expand/fill). Also collect columns/rows that expand.
		int[] columnWidth = new int[columns];
		int[] rowHeight = new int[rows];
		float[] expandWidth = new float[columns];
		float[] expandHeight = new float[rows];
		float totalExpandWidth = 0, totalExpandHeight = 0;
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;

			int spannedCellMaxWidth = 0;
			for (int column = c.column, nn = column + c.colspan; column < nn; column++) {
				spannedCellMaxWidth += columnMaxWidth[column];

				if (c.colspan == 1 && c.expandWidth != 0 && expandWidth[column] == 0) {
					expandWidth[column] = c.expandWidth / (float)c.colspan;
					totalExpandWidth += c.expandWidth / (float)c.colspan;
				}
			}
			spannedCellMaxWidth -= c.padLeftTemp - c.padRightTemp;
			if (c.expandHeight != 0 && expandHeight[c.row] == 0) {
				expandHeight[c.row] = c.expandHeight;
				totalExpandHeight += c.expandHeight;
			}

			int prefWidth = toolkit.getWidth((T)c.widget, c.prefWidth);
			int prefHeight = toolkit.getHeight((T)c.widget, c.prefHeight);
			int minWidth = toolkit.getWidth((T)c.widget, c.minWidth);
			int minHeight = toolkit.getHeight((T)c.widget, c.minHeight);
			if (prefWidth < minWidth) prefWidth = minWidth;
			if (prefHeight < minHeight) prefHeight = minHeight;

			c.widgetWidth = Math.min(spannedCellMaxWidth, prefWidth);
			c.widgetHeight = Math.min(rowMaxHeight[c.row] - c.padTopTemp - c.padBottomTemp, prefHeight);

			if (c.colspan == 1)
				columnWidth[c.column] = Math.max(columnWidth[c.column], c.widgetWidth + c.padLeftTemp + c.padRightTemp);
			rowHeight[c.row] = Math.max(rowHeight[c.row], c.widgetHeight + c.padTopTemp + c.padBottomTemp);
		}

		// Uniform cells are all the same width/height.
		int uniformMaxWidth = 0, uniformMaxHeight = 0;
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;
			if (c.uniformWidth != null) uniformMaxWidth = Math.max(uniformMaxWidth, columnWidth[c.column]);
			if (c.uniformHeight != null) uniformMaxHeight = Math.max(uniformMaxHeight, rowHeight[c.row]);
		}
		if (uniformMaxWidth > 0 || uniformMaxHeight > 0) {
			outer:
			for (int i = 0, n = cells.size(); i < n; i++) {
				Cell c = cells.get(i);
				if (c.ignore) continue;
				if (uniformMaxWidth > 0 && c.uniformWidth != null)
					columnWidth[c.column] = Math.max(uniformMaxWidth, columnWidth[c.column]);
				if (uniformMaxHeight > 0 && c.uniformHeight != null) //
					rowHeight[c.row] = Math.max(uniformMaxHeight, rowHeight[c.row]);
				continue outer;
			}
		}

		// Distribute remaining space to any expanding columns/rows.
		if (totalExpandWidth > 0) {
			int amount = Math.max(0, tableLayoutWidth - totalPrefWidth);
			for (int i = 0; i < columns; i++)
				if (expandWidth[i] != 0) columnWidth[i] += amount * expandWidth[i] / totalExpandWidth;
		}
		if (totalExpandHeight > 0) {
			int amount = Math.max(0, (int)(tableLayoutHeight - totalPrefHeight));
			for (int i = 0; i < rows; i++)
				if (expandHeight[i] != 0) rowHeight[i] += amount * expandHeight[i] / totalExpandHeight;
		}

		// Distribute any additional width added by colspanned cells to the columns spanned.
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;
			if (c.colspan == 1) continue;

			int minWidth = toolkit.getWidth((T)c.widget, c.minWidth);

			int spannedCellWidth = 0;
			for (int column = c.column, nn = column + c.colspan; column < nn; column++)
				spannedCellWidth += columnWidth[column];

			int extraWidth = Math.max(0, minWidth - spannedCellWidth) / c.colspan;
			for (int column = c.column, nn = column + c.colspan; column < nn; column++)
				columnWidth[column] += extraWidth;

			c.widgetWidth = Math.max(c.widgetWidth, minWidth - (c.padLeftTemp - c.padRightTemp));
		}

		// Determine table size.
		int tableWidth = 0, tableHeight = 0;
		for (int i = 0; i < columns; i++)
			tableWidth += columnWidth[i];
		tableWidth = Math.max(tableWidth, width);
		for (int i = 0; i < rows; i++)
			tableHeight += rowHeight[i];
		tableHeight = Math.max(tableHeight, height);

		// Position table within the TableLayout.
		int x = tableLayoutX + padLeft;
		if ((align & RIGHT) != 0)
			x += tableLayoutWidth - tableWidth;
		else if ((align & LEFT) == 0) // Center
			x += (tableLayoutWidth - tableWidth) / 2;

		int y = tableLayoutY + padTop;
		if ((align & BOTTOM) != 0)
			y += tableLayoutHeight - tableHeight;
		else if ((align & TOP) == 0) // Center
			y += (tableLayoutHeight - tableHeight) / 2;

		// Position widgets within cells.
		int currentX = x;
		int currentY = y;
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;

			int spannedCellWidth = 0;
			for (int column = c.column, nn = column + c.colspan; column < nn; column++)
				spannedCellWidth += columnWidth[column];
			spannedCellWidth -= c.padLeftTemp + c.padRightTemp;

			currentX += c.padLeftTemp;

			if (c.fillWidth > 0) {
				c.widgetWidth = (int)(spannedCellWidth * c.fillWidth);
				int maxWidth = toolkit.getWidth((T)c.widget, c.maxWidth);
				if (maxWidth > 0) c.widgetWidth = Math.min(c.widgetWidth, maxWidth);
			}
			if (c.fillHeight > 0) {
				c.widgetHeight = (int)(rowHeight[c.row] * c.fillHeight) - c.padTopTemp - c.padBottomTemp;
				int maxHeight = toolkit.getHeight((T)c.widget, c.maxHeight);
				if (maxHeight > 0) c.widgetHeight = Math.min(c.widgetHeight, maxHeight);
			}

			if ((c.align & LEFT) != 0)
				c.widgetX = currentX;
			else if ((c.align & RIGHT) != 0)
				c.widgetX = currentX + spannedCellWidth - c.widgetWidth;
			else
				c.widgetX = currentX + (spannedCellWidth - c.widgetWidth) / 2;

			if ((c.align & TOP) != 0)
				c.widgetY = currentY + c.padTopTemp;
			else if ((c.align & BOTTOM) != 0)
				c.widgetY = currentY + rowHeight[c.row] - c.widgetHeight - c.padBottomTemp;
			else
				c.widgetY = currentY + (rowHeight[c.row] - c.widgetHeight + c.padTopTemp - c.padBottomTemp) / 2;

			if (c.endRow) {
				currentX = x;
				currentY += rowHeight[c.row];
			} else
				currentX += spannedCellWidth + c.padRightTemp;
		}

		// Draw widgets and bounds.
		if (debug == null) return;
		clearDebugRectangles();
		currentX = x;
		currentY = y;
		if (debug.contains("table,") || debug.contains("all,")) {
			addDebugRectangle(false, tableLayoutX + padLeft, tableLayoutY + padTop, tableLayoutWidth, tableLayoutHeight);
			addDebugRectangle(false, x, y, tableWidth, tableHeight);
		}
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;

			// Widget bounds.
			if (debug.contains("widget,") || debug.contains("all,"))
				addDebugRectangle(false, c.widgetX, c.widgetY, c.widgetWidth, c.widgetHeight);

			// Cell bounds.
			int spannedCellWidth = 0;
			for (int column = c.column, nn = column + c.colspan; column < nn; column++)
				spannedCellWidth += columnWidth[column];
			spannedCellWidth -= c.padLeftTemp + c.padRightTemp;
			currentX += c.padLeftTemp;
			if (debug.contains("cell,") || debug.contains("all,"))
				addDebugRectangle(true, currentX, currentY + c.padTopTemp, spannedCellWidth, rowHeight[c.row] - c.padTopTemp
					- c.padBottomTemp);
			if (c.endRow) {
				currentX = x;
				currentY += rowHeight[c.row];
			} else
				currentX += spannedCellWidth + c.padRightTemp;
		}
	}

	public void setWidget (Cell cell, T widget) {
		if (cell.widget != null) toolkit.removeChild(getTable(), (T)cell.widget);
		cell.widget = widget;
		nameToWidget.put(cell.name, widget);
		toolkit.addChild(getTable(), widget, null);
	}

	/**
	 * Sets the widget in the cell with the specified name.
	 */
	public void setWidget (String name, T widget) {
		setWidget(getCell(name), widget);
	}

	/**
	 * Marks the TableLayout as needing to layout again.
	 */
	abstract public void invalidate ();

	/**
	 * Clears all debugging rectangles.
	 */
	abstract public void clearDebugRectangles ();

	/**
	 * Adds a rectangle that should be drawn for debugging.
	 */
	abstract public void addDebugRectangle (boolean isCell, int x, int y, int w, int h);

	abstract public T getTable ();

	abstract public Toolkit<T> getToolkit ();
}
