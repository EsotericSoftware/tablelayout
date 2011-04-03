
package com.esotericsoftware.tablelayout;

import java.awt.Component;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public abstract class BaseTableLayout<T> {
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

	static final ArrayList<String> classPrefixes = new ArrayList();

	static private final int[][] intArrays = new int[10][];
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

	final HashMap<String, T> nameToWidget;

	private final ArrayList<Cell> cells = new ArrayList();
	private Cell cellDefaults = cellDefaults();
	private final ArrayList<Cell> columnDefaults = new ArrayList(4);
	private Cell rowDefaults;
	private int columns, rows;

	public BaseTableLayout () {
		nameToWidget = new HashMap();
	}

	public BaseTableLayout (BaseTableLayout parent) {
		nameToWidget = parent.nameToWidget;
	}

	public BaseTableLayout (String tableText) {
		this();
		if (tableText != null) parse(tableText);
	}

	public T setName (String name, T widget) {
		name = name.toLowerCase();
		if (nameToWidget.containsKey(name)) throw new IllegalArgumentException("Name is already used: " + name);
		nameToWidget.put(name, widget);
		return widget;
	}

	public void parse (String tableText) {
		TableLayoutParser.parse(this, tableText);
	}

	/**
	 * @param widget May be null.
	 */
	public Cell add (T widget) {
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

		addWidget(widget);

		return cell;
	}

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

	public void clear () {
		for (int i = cells.size() - 1; i >= 0; i--) {
			Cell cell = cells.get(i);
			removeWidget((T)cell.widget);
		}
		cells.clear();
		columnDefaults.clear();
		nameToWidget.clear();
		cellDefaults = cellDefaults();
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

	public Cell getCellDefaults () {
		return cellDefaults;
	}

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

	public ArrayList<Cell> getColumnDefaults () {
		return columnDefaults;
	}

	public ArrayList<Cell> getCells () {
		return cells;
	}

	public void layout () {
		if (cells.size() > 0 && !cells.get(cells.size() - 1).endRow) endRow();

		// Determine minimum and preferred cell sizes. Also compute the combined padding/spacing for each cell.
		int[] columnMinWidth = intArray(INT_columnMinWidth, columns, true);
		int[] rowMinHeight = intArray(INT_rowMinHeight, rows, true);
		int[] columnPrefWidth = intArray(INT_columnPrefWidth, columns, true);
		int[] rowPrefHeight = intArray(INT_rowPrefHeight, rows, true);
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

			int prefWidth = getWidth((T)c.widget, c.prefWidth);
			int prefHeight = getHeight((T)c.widget, c.prefHeight);
			int minWidth = getWidth((T)c.widget, c.minWidth);
			int minHeight = getHeight((T)c.widget, c.minHeight);
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
			columnMaxWidth = intArray(INT_columnMaxWidth, columns, false);
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
			rowMaxHeight = intArray(INT_rowMaxHeight, rows, false);
			for (int i = 0; i < rows; i++) {
				int growHeight = rowPrefHeight[i] - rowMinHeight[i];
				float growRatio = growHeight / (float)totalGrowHeight;
				rowMaxHeight[i] = rowMinHeight[i] + (int)(extraHeight * growRatio);
			}
		}

		// Determine widget and cell sizes (before uniform/expand/fill). Also collect columns/rows that expand.
		int[] columnWidth = intArray(INT_columnWidth, columns, true);
		int[] rowHeight = intArray(INT_rowHeight, rows, true);
		float[] expandWidth = floatArray(FLOAT_expandWidth, columns);
		float[] expandHeight = floatArray(FLOAT_expandHeight, rows);
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

			int prefWidth = getWidth((T)c.widget, c.prefWidth);
			int prefHeight = getHeight((T)c.widget, c.prefHeight);
			int minWidth = getWidth((T)c.widget, c.minWidth);
			int minHeight = getHeight((T)c.widget, c.minHeight);
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

			int minWidth = getWidth((T)c.widget, c.minWidth);

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
				int maxWidth = getWidth((T)c.widget, c.maxWidth);
				if (maxWidth > 0) c.widgetWidth = Math.min(c.widgetWidth, maxWidth);
			}
			if (c.fillHeight > 0) {
				c.widgetHeight = (int)(rowHeight[c.row] * c.fillHeight) - c.padTopTemp - c.padBottomTemp;
				int maxHeight = getHeight((T)c.widget, c.maxHeight);
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
		clearDebugRects();
		currentX = x;
		currentY = y;
		if (debug.contains("table,") || debug.contains("all,")) {
			addDebugRect(false, tableLayoutX + padLeft, tableLayoutY + padTop, tableLayoutWidth, tableLayoutHeight);
			addDebugRect(false, x, y, tableWidth, tableHeight);
		}
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;

			// Widget bounds.
			if (debug.contains("widget,") || debug.contains("all,"))
				addDebugRect(false, c.widgetX, c.widgetY, c.widgetWidth, c.widgetHeight);

			// Cell bounds.
			int spannedCellWidth = 0;
			for (int column = c.column, nn = column + c.colspan; column < nn; column++)
				spannedCellWidth += columnWidth[column];
			spannedCellWidth -= c.padLeftTemp + c.padRightTemp;
			currentX += c.padLeftTemp;
			if (debug.contains("cell,") || debug.contains("all,"))
				addDebugRect(true, currentX, currentY + c.padTopTemp, spannedCellWidth, rowHeight[c.row] - c.padTopTemp
					- c.padBottomTemp);
			if (c.endRow) {
				currentX = x;
				currentY += rowHeight[c.row];
			} else
				currentX += spannedCellWidth + c.padRightTemp;
		}
	}

	private int getWidth (T widget, int value) {
		switch (value) {
		case MIN:
			return getMinWidth(widget);
		case PREF:
			return getPrefWidth(widget);
		case MAX:
			return getMaxWidth(widget);
		}
		return value;
	}

	private int getHeight (T widget, int value) {
		switch (value) {
		case MIN:
			return getMinHeight(widget);
		case PREF:
			return getPrefHeight(widget);
		case MAX:
			return getMaxHeight(widget);
		}
		return value;
	}

	abstract protected BaseTableLayout newTableLayout ();

	abstract protected T newLabel (String text);

	abstract protected void setTitle (T parent, String string);

	abstract protected void addChild (T parent, T child, String layoutString);

	abstract protected T wrap (Object object);

	abstract protected void addWidget (T child);

	abstract protected void removeWidget (T child);

	abstract protected int getMinWidth (T widget);

	abstract protected int getMinHeight (T widget);

	abstract protected int getPrefWidth (T widget);

	abstract protected int getPrefHeight (T widget);

	abstract protected int getMaxWidth (T widget);

	abstract protected int getMaxHeight (T widget);

	/**
	 * Marks the TableLayout as needing to layout again.
	 */
	abstract public void invalidate ();

	protected int scale (String value) {
		return Integer.parseInt(value);
	}

	public Object newWidget (String className) throws Exception {
		try {
			return Class.forName(className).newInstance();
		} catch (Exception ex) {
			for (int i = 0, n = classPrefixes.size(); i < n; i++) {
				String prefix = classPrefixes.get(i);
				try {
					return Class.forName(prefix + className).newInstance();
				} catch (Exception ignored) {
				}
			}
			throw ex;
		}
	}

	public void setWidget (Cell cell, T widget) {
		if (cell.widget != null) removeWidget((T)cell.widget);
		cell.widget = widget;
		nameToWidget.put(cell.name, widget);
		addWidget(widget);
	}

	public void setWidget (String name, T widget) {
		setWidget(getCell(name), widget);
	}

	public void setProperty (Object object, String name, ArrayList<String> values) {
		try {
			invokeMethod(object, name, values);
		} catch (NoSuchMethodException ex1) {
			try {
				invokeMethod(object, "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1), values);
			} catch (NoSuchMethodException ex2) {
				try {
					Field field = object.getClass().getField(name);
					Object value = convertType(object, values.get(0), field.getType());
					if (value != null) field.set(object, value);
				} catch (Exception ex3) {
					throw new RuntimeException("No method, bean property, or field found.");
				}
			}
		}
	}

	public void setTableProperty (String name, ArrayList<String> values) {
		name = name.toLowerCase();
		for (int i = 0, n = values.size(); i < n; i++)
			values.set(i, values.get(i).toLowerCase());
		try {
			String value;
			if (name.equals("size")) {
				switch (values.size()) {
				case 1:
					width = height = scale(values.get(0));
					break;
				case 2:
					width = scale(values.get(0));
					height = scale(values.get(1));
					break;
				}

			} else if (name.equals("width") || name.equals("w")) {
				width = scale(values.get(0));

			} else if (name.equals("height") || name.equals("h")) {
				height = scale(values.get(0));

			} else if (name.equals("fill")) {
				switch (values.size()) {
				case 0:
					fillWidth = fillHeight = 1f;
					break;
				case 1:
					value = values.get(0);
					if (value.equals("x"))
						fillWidth = 1f;
					else if (value.equals("y")) //
						fillHeight = 1f;
					else
						fillWidth = fillHeight = Integer.parseInt(value) / 100f;
					break;
				case 2:
					value = values.get(0);
					if (value.length() > 0) fillWidth = Integer.parseInt(value) / 100f;
					value = values.get(1);
					if (value.length() > 0) fillHeight = Integer.parseInt(value) / 100f;
					break;
				}

			} else if (name.equals("padding") || name.equals("pad")) {
				switch (values.size()) {
				case 4:
					value = values.get(3);
					if (value.length() > 0) padRight = scale(value);
				case 3:
					value = values.get(2);
					if (value.length() > 0) padBottom = scale(value);
				case 2:
					value = values.get(0);
					if (value.length() > 0) padTop = scale(value);
					value = values.get(1);
					if (value.length() > 0) padLeft = scale(value);
					break;
				case 1:
					padTop = padLeft = padBottom = padRight = scale(values.get(0));
					break;
				default:
					throw new IllegalArgumentException("Invalid number of values (" + values.size() + "): " + values);
				}

			} else if (name.startsWith("padding") || name.startsWith("pad")) {
				name = name.replace("padding", "").replace("pad", "");
				if (name.equals("top") || name.equals("t"))
					padTop = scale(values.get(0));
				else if (name.equals("left") || name.equals("l"))
					padLeft = scale(values.get(0));
				else if (name.equals("bottom") || name.equals("b"))
					padBottom = scale(values.get(0));
				else if (name.equals("right") || name.equals("r"))
					padRight = scale(values.get(0));
				else
					throw new IllegalArgumentException("Unknown property.");

			} else if (name.equals("align")) {
				align = 0;
				for (int i = 0, n = values.size(); i < n; i++) {
					value = values.get(i);
					if (value.equals("center"))
						align |= CENTER;
					else if (value.equals("left"))
						align |= LEFT;
					else if (value.equals("right"))
						align |= RIGHT;
					else if (value.equals("top"))
						align |= TOP;
					else if (value.equals("bottom"))
						align |= BOTTOM;
					else
						throw new IllegalArgumentException("Invalid value: " + value);
				}

			} else if (name.equals("debug")) {
				debug = "";
				if (values.size() == 0) debug = "all,";
				for (int i = 0, n = values.size(); i < n; i++)
					debug += values.get(i) + ",";
				if (debug.equals("true,")) debug = "all,";

			} else
				throw new IllegalArgumentException("Unknown property: " + name);
		} catch (Exception ex) {
			throw new IllegalArgumentException("Error setting property: " + name, ex);
		}
	}

	public void setCellProperty (Cell c, String name, ArrayList<String> values) {
		name = name.toLowerCase();
		for (int i = 0, n = values.size(); i < n; i++)
			values.set(i, values.get(i).toLowerCase());
		try {
			String value;
			if (name.equals("expand")) {
				switch (values.size()) {
				case 0:
					c.expandWidth = c.expandHeight = 1;
					break;
				case 1:
					value = values.get(0);
					if (value.equals("x"))
						c.expandWidth = 1;
					else if (value.equals("y")) //
						c.expandHeight = 1;
					else
						c.expandWidth = c.expandHeight = Integer.parseInt(value);
					break;
				case 2:
					value = values.get(0);
					if (value.length() > 0) c.expandWidth = Integer.parseInt(value);
					value = values.get(1);
					if (value.length() > 0) c.expandHeight = Integer.parseInt(value);
					break;
				}

			} else if (name.equals("fill")) {
				switch (values.size()) {
				case 0:
					c.fillWidth = c.fillHeight = 1f;
					break;
				case 1:
					value = values.get(0);
					if (value.equals("x"))
						c.fillWidth = 1f;
					else if (value.equals("y")) //
						c.fillHeight = 1f;
					else
						c.fillWidth = c.fillHeight = Integer.parseInt(value) / 100f;
					break;
				case 2:
					value = values.get(0);
					if (value.length() > 0) c.fillWidth = Integer.parseInt(value) / 100f;
					value = values.get(1);
					if (value.length() > 0) c.fillHeight = Integer.parseInt(value) / 100f;
					break;
				}

			} else if (name.equals("size")) {
				switch (values.size()) {
				case 2:
					value = values.get(0);
					if (value.length() > 0) c.minWidth = c.prefWidth = scale(value);
					value = values.get(1);
					if (value.length() > 0) c.minHeight = c.prefHeight = scale(value);
					break;
				case 1:
					value = values.get(0);
					if (value.length() > 0) c.minWidth = c.minHeight = c.prefWidth = c.prefHeight = scale(value);
					break;
				default:
					throw new IllegalArgumentException("Invalid number of values (" + values.size() + "): " + values);
				}

			} else if (name.equals("width") || name.equals("w")) {
				switch (values.size()) {
				case 3:
					value = values.get(0);
					if (value.length() > 0) c.maxWidth = scale(value);
				case 2:
					value = values.get(1);
					if (value.length() > 0) c.prefWidth = scale(value);
				case 1:
					value = values.get(0);
					if (value.length() > 0) c.minWidth = scale(value);
					break;
				default:
					throw new IllegalArgumentException("Invalid number of values (" + values.size() + "): " + values);
				}

			} else if (name.equals("height") || name.equals("h")) {
				switch (values.size()) {
				case 3:
					value = values.get(0);
					if (value.length() > 0) c.maxHeight = scale(value);
				case 2:
					value = values.get(1);
					if (value.length() > 0) c.prefHeight = scale(value);
				case 1:
					value = values.get(0);
					if (value.length() > 0) c.minHeight = scale(value);
					break;
				default:
					throw new IllegalArgumentException("Invalid number of values (" + values.size() + "): " + values);
				}

			} else if (name.equals("spacing") || name.equals("space")) {
				switch (values.size()) {
				case 4:
					value = values.get(3);
					if (value.length() > 0) c.spaceRight = scale(value);
				case 3:
					value = values.get(2);
					if (value.length() > 0) c.spaceBottom = scale(value);
				case 2:
					value = values.get(0);
					if (value.length() > 0) c.spaceTop = scale(value);
					value = values.get(1);
					if (value.length() > 0) c.spaceLeft = scale(value);
					break;
				case 1:
					c.spaceTop = c.spaceLeft = c.spaceBottom = c.spaceRight = scale(values.get(0));
					break;
				default:
					throw new IllegalArgumentException("Invalid number of values (" + values.size() + "): " + values);
				}

			} else if (name.equals("padding") || name.equals("pad")) {
				switch (values.size()) {
				case 4:
					value = values.get(3);
					if (value.length() > 0) c.padRight = scale(value);
				case 3:
					value = values.get(2);
					if (value.length() > 0) c.padBottom = scale(value);
				case 2:
					value = values.get(0);
					if (value.length() > 0) c.padTop = scale(value);
					value = values.get(1);
					if (value.length() > 0) c.padLeft = scale(value);
					break;
				case 1:
					c.padTop = c.padLeft = c.padBottom = c.padRight = scale(values.get(0));
					break;
				default:
					throw new IllegalArgumentException("Invalid number of values (" + values.size() + "): " + values);
				}

			} else if (name.startsWith("padding") || name.startsWith("pad")) {
				name = name.replace("padding", "").replace("pad", "");
				if (name.equals("top") || name.equals("t"))
					c.padTop = scale(values.get(0));
				else if (name.equals("left") || name.equals("l"))
					c.padLeft = scale(values.get(0));
				else if (name.equals("bottom") || name.equals("b"))
					c.padBottom = scale(values.get(0));
				else if (name.equals("right") || name.equals("r")) //
					c.padRight = scale(values.get(0));
				else
					throw new IllegalArgumentException("Unknown property.");

			} else if (name.startsWith("spacing") || name.startsWith("space")) {
				name = name.replace("spacing", "").replace("space", "");
				if (name.equals("top") || name.equals("t"))
					c.spaceTop = scale(values.get(0));
				else if (name.equals("left") || name.equals("l"))
					c.spaceLeft = scale(values.get(0));
				else if (name.equals("bottom") || name.equals("b"))
					c.spaceBottom = scale(values.get(0));
				else if (name.equals("right") || name.equals("r")) //
					c.spaceRight = scale(values.get(0));
				else
					throw new IllegalArgumentException("Unknown property.");

			} else if (name.equals("align")) {
				c.align = 0;
				for (int i = 0, n = values.size(); i < n; i++) {
					value = values.get(i);
					if (value.equals("center"))
						c.align |= CENTER;
					else if (value.equals("left"))
						c.align |= LEFT;
					else if (value.equals("right"))
						c.align |= RIGHT;
					else if (value.equals("top"))
						c.align |= TOP;
					else if (value.equals("bottom"))
						c.align |= BOTTOM;
					else
						throw new IllegalArgumentException("Invalid value: " + value);
				}

			} else if (name.equals("ignore")) {
				c.ignore = values.size() == 0 ? true : Boolean.valueOf(values.get(0));

			} else if (name.equals("colspan")) {
				c.colspan = Integer.parseInt(values.get(0));

			} else if (name.equals("uniform")) {
				if (values.size() == 0) c.uniformWidth = c.uniformHeight = true;
				for (int i = 0, n = values.size(); i < n; i++) {
					value = values.get(i);
					if (value.equals("x"))
						c.uniformWidth = true;
					else if (value.equals("y"))
						c.uniformHeight = true;
					else if (value.equals("false"))
						c.uniformHeight = c.uniformHeight = null;
					else
						throw new IllegalArgumentException("Invalid value: " + value);
				}

			} else
				throw new IllegalArgumentException("Unknown property.");
		} catch (Exception ex) {
			throw new IllegalArgumentException("Error setting property: " + name, ex);
		}
	}

	public void clearDebugRects () {
	}

	public void addDebugRect (boolean isCell, int x, int y, int w, int h) {
	}

	static public void addClassPrefix (String prefix) {
		classPrefixes.add(prefix);
	}

	static private int[] intArray (int index, int size, boolean zero) {
		if (true) return new int[size];
		int[] array = intArrays[index];
		if (array == null || array.length < size) {
			array = new int[size];
			intArrays[index] = array;
		} else if (zero) {
			for (int i = 0; i < size; i++)
				array[i] = 0;
		}
		return array;
	}

	static private float[] floatArray (int index, int size) {
		if (true) return new float[size];
		float[] array = floatArrays[index];
		if (array == null || array.length < size) {
			array = new float[size];
			floatArrays[index] = array;
		} else {
			for (int i = 0; i < size; i++)
				array[i] = 0;
		}
		return array;
	}

	static private void invokeMethod (Object object, String name, ArrayList<String> values) throws NoSuchMethodException {
		Object[] params = values.toArray();
		// Prefer methods with string parameters.
		Class[] stringParamTypes = new Class[params.length];
		Method method = null;
		try {
			for (int i = 0, n = params.length; i < n; i++)
				stringParamTypes[i] = String.class;
			method = object.getClass().getMethod(name, stringParamTypes);
		} catch (NoSuchMethodException ignored) {
			try {
				for (int i = 0, n = params.length; i < n; i++)
					stringParamTypes[i] = CharSequence.class;
				method = object.getClass().getMethod(name, stringParamTypes);
			} catch (NoSuchMethodException ignored2) {
			}
		}
		if (method != null) {
			try {
				method.invoke(object, params);
			} catch (Exception ex) {
				throw new RuntimeException("Error invoking method: " + name, ex);
			}
			return;
		}
		// Try to convert the strings to match a method.
		Method[] methods = object.getClass().getMethods();
		outer:
		for (int i = 0, n = methods.length; i < n; i++) {
			method = methods[i];
			if (!method.getName().equalsIgnoreCase(name)) continue;
			params = values.toArray();
			Class[] paramTypes = method.getParameterTypes();
			for (int ii = 0, nn = paramTypes.length; ii < nn; ii++) {
				Object value = convertType(object, (String)params[ii], paramTypes[ii]);
				if (value == null) continue outer;
				params[ii] = value;
			}
			try {
				method.invoke(object, params);
				return;
			} catch (Exception ex) {
				throw new RuntimeException("Error invoking method: " + name, ex);
			}
		}
		throw new NoSuchMethodException();
	}

	static private Object convertType (Object parentObject, String value, Class paramType) {
		if (paramType == String.class || paramType == CharSequence.class) return value;
		try {
			if (paramType == int.class || paramType == Integer.class) return Integer.valueOf(value);
			if (paramType == float.class || paramType == Float.class) return Float.valueOf(value);
			if (paramType == long.class || paramType == Long.class) return Long.valueOf(value);
			if (paramType == double.class || paramType == Double.class) return Double.valueOf(value);
		} catch (NumberFormatException ignored) {
		}
		if (paramType == boolean.class || paramType == Boolean.class) return Boolean.valueOf(value);
		if (paramType == char.class || paramType == Character.class) return value.charAt(0);
		if (paramType == short.class || paramType == Short.class) return Short.valueOf(value);
		if (paramType == byte.class || paramType == Byte.class) return Byte.valueOf(value);
		// Look for a static field.
		try {
			Field field = getField(paramType, value);
			if (field != null && paramType == field.getType()) return field.get(null);
		} catch (Exception ignored) {
		}
		try {
			Field field = getField(parentObject.getClass(), value);
			if (field != null && paramType == field.getType()) return field.get(null);
		} catch (Exception ignored) {
		}
		return null;
	}

	static private Field getField (Class type, String name) {
		try {
			Field field = type.getField(name);
			if (field != null) return field;
		} catch (Exception ignored) {
		}
		while (type != null && type != Object.class) {
			Field[] fields = type.getDeclaredFields();
			for (int i = 0, n = fields.length; i < n; i++)
				if (fields[i].getName().equalsIgnoreCase(name)) return fields[i];
			type = type.getSuperclass();
		}
		return null;
	}

	private Cell cellDefaults () {
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

	static public class Cell {
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
	}
}
