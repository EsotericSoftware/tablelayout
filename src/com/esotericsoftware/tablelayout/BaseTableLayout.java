
package com.esotericsoftware.tablelayout;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JPanel;

// BOZO - Align percent?
// BOZO - Shrink priority?
// BOZO - When merging alignment, clear only the flags necessary?
// BOZO - Line numbers for errors.
// BOZO - Error if widget already in layout?
// BOZO - Name label? 'moo' vs [moo: 'moo']
// BOZO - Avoid allocation.

public class BaseTableLayout<T> {
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

	public int width, height;
	public int padTop, padLeft, padBottom, padRight;
	public int align = CENTER;
	public String debug;
	public float fillX, fillY; // BOZO - Use?

	protected int tableLayoutWidth, tableLayoutHeight;
	protected int totalMinWidth, totalMinHeight;
	protected int totalPrefWidth, totalPrefHeight;

	final HashMap<String, T> nameToWidget;

	private final ArrayList<Cell> cells = new ArrayList();
	private Cell cellDefaults = Cell.defaults();
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
		parse(tableText);
	}

	public T setName (String name, T widget) {
		name = name.toLowerCase();
		if (nameToWidget.containsKey(name)) throw new IllegalArgumentException("Name is already used: " + name);
		nameToWidget.put(name, widget);
		return widget;
	}

	public void parse (String tableText) {
		TableLayoutParser.parse(this, null, tableText);
	}

	public Cell add (T widget) {
		Cell cell = new Cell();
		cell.widget = widget;

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

		return cell;
	}

	public Cell add (T widget, String cellText) {
		Cell cell = add(widget);
		TableLayoutParser.parse(null, cell, cellText);
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
		int[] columnMinWidth = new int[columns], rowMinHeight = new int[rows];
		int[] columnPrefWidth = new int[columns], rowPrefHeight = new int[rows];
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

		// Determine maximum cell sizes using preferred size ratios to distribute space beyond min size.
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
		int[] columnWidth = new int[columns], rowHeight = new int[rows];
		float[] expandWidth = new float[columns], expandHeight = new float[rows];
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

		// Position table with the TableLayout.
		int tableWidth = 0, tableHeight = 0;
		for (int i = 0; i < columns; i++)
			tableWidth += columnWidth[i];
		tableWidth = Math.max(tableWidth, width);
		for (int i = 0; i < rows; i++)
			tableHeight += rowHeight[i];
		tableHeight = Math.max(tableHeight, height);

		int x = padLeft, y = padTop;
		if ((align & RIGHT) != 0)
			x += tableLayoutWidth - tableWidth;
		else if ((align & LEFT) == 0) // Center
			x += (tableLayoutWidth - tableWidth) / 2;

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
				c.widgetWidth = Math.max((int)(spannedCellWidth * c.fillWidth), getWidth((T)c.widget, c.minWidth));
				int maxWidth = getWidth((T)c.widget, c.maxWidth);
				if (maxWidth > 0) c.widgetWidth = Math.min(c.widgetWidth, maxWidth);
			}
			if (c.fillHeight > 0) {
				c.widgetHeight = Math.max((int)(rowHeight[c.row] * c.fillHeight) - c.padTopTemp - c.padBottomTemp,
					getHeight((T)c.widget, c.minHeight));
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
		currentX = x;
		currentY = y;
		if (debug.contains("table,") || debug.contains("all,")) {
			drawDebugRect(true, padLeft, padTop, tableLayoutWidth - 1, tableLayoutHeight - 1);
			drawDebugRect(true, x, y, tableWidth - 1, tableHeight - 1);
		}
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;

			// Widget bounds.
			if (debug.contains("widget,") || debug.contains("all,"))
				drawDebugRect(true, c.widgetX, c.widgetY, c.widgetWidth - 1, c.widgetHeight - 1);

			// Cell bounds.
			int spannedCellWidth = 0;
			for (int column = c.column, nn = column + c.colspan; column < nn; column++)
				spannedCellWidth += columnWidth[column];
			spannedCellWidth -= c.padLeftTemp + c.padRightTemp;
			currentX += c.padLeftTemp;
			if (debug.contains("cell,") || debug.contains("all,"))
				drawDebugRect(false, currentX, currentY + c.padTopTemp, spannedCellWidth - 1, rowHeight[c.row] - c.padTopTemp
					- c.padBottomTemp - 1);
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

	protected BaseTableLayout newTableLayout () {
		return new BaseTableLayout(this);
	}

	protected T newLabel (String text) {
		return (T)text;
	}

	protected void setTitle (T parent, String string) {
	}

	protected void addChild (T parent, T child, String layoutString) {
	}

	protected T wrap (Object object) {
		return (T)object;
	}

	protected int getMinWidth (T widget) {
		return 0;
	}

	protected int getMinHeight (T widget) {
		return 0;
	}

	protected int getPrefWidth (T widget) {
		return 0;
	}

	protected int getPrefHeight (T widget) {
		return 0;
	}

	protected int getMaxWidth (T widget) {
		return 0;
	}

	protected int getMaxHeight (T widget) {
		return 0;
	}

	protected void drawDebugRect (boolean dash, int x, int y, int w, int h) {
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

		boolean endRow;
		int column, row;
		int cellAboveIndex = -1;
		int padTopTemp, padLeftTemp, padBottomTemp, padRightTemp;

		public Object widget;
		public int widgetX, widgetY;
		public int widgetWidth, widgetHeight;

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

	static public void addClassPrefix (String prefix) {
		classPrefixes.add(prefix);
	}

	static public void main (String[] args) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(800, 600);
		frame.setLocationRelativeTo(null);

		final BaseTableLayout table = new BaseTableLayout();
		table.setName("1", 1);
		table.setName("2", 2);
		table.setName("3", 3);
		table.setName("4", 4);
		table.setName("5", 5);
		table.setName("6", 6);
		table.setName("7", 7);
		table.setName("8", 8);
		table.setName("9", 9);
		table.setName("10", 10);

		// table.parse("align:center padding:10" //
		// + "* height:140 expand" //
		// + "[1] width:180 fill:50,50" //
		// + "'2' size:200,20 colspan:2" //
		// + "---" //
		// + "[1] size:40,120" //
		// + "[4] size:180,20" //
		// + "[5] size:20,20" //
		// + "---" //
		// + "[6] size:40,20" //
		// + "[7] size:180,20" //
		// + "---" //
		// + "[8] size:40,20" //
		// + "{padding:10" //
		// + "'Name:' size:50" //
		// + "'111' size:25 " //
		// + "--- " //
		// + "'Stuff:' width:100" //
		// + "'moo' size:25 } fill " //
		// + "[2] size:100,200 " //
		// + "[10] size:20,20" //
		// );

		// table.parse("padding:10 * align:left" //
		// + "|  | align:right " //
		// + "--- align:bottom,right expand:1,1" //
		// + "'Name:' width:100" //
		// + "[1] size:100,200 " //
		// + "--- " //
		// + "'Stuff:' width:100 expand" //
		// + "[2] size:100,200 " //
		// );

		// table.parse("padding:10 " //
		// + "[1] size:150,250 fill" //
		// + "[2] size:100,200 expand:50" //
		// + "{ * expand" //
		// + "	[3] size:50,200" //
		// + "	---" //
		// + "	[4] size:40,100" //
		// + "} size:70,300" //
		// );

		// table.parse("padding:10" //
		// + "---" //
		// + "[1] size:40 align:right,top" //
		// + "[2] size:80 align:right" //
		// + "---" //
		// + "[3] size:20 align:left" //
		// + "[4] fill" //
		// );

		// BOZO - Shit broke?
		table.parse("width:640 height:480" //
			+ "---" //
			+ "'logo'" //
			+ "{ width:200 height:200" //
			// + "* fill" //
			+ "| align:right | align:left" //
			+ "'Name:' size:40" //
			+ "'nameEdit' size:200 colspan:2" //
			+ "---" //
			+ "'File' size:40" //
			+ "'fileEdit' size:40 expand:x" //
			+ "'browseButton' size:40" //
			+ "} width:200 height:200 fill" //
		);

		frame.setContentPane(new JPanel() {
			public void paintComponent (Graphics g) {
				table.layout();
			}
		});
		frame.setVisible(true);
	}
}
