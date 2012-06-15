
package com.esotericsoftware.tablelayout.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;

import com.esotericsoftware.tablelayout.BaseTableLayout;
import com.esotericsoftware.tablelayout.Cell;

public class Table extends JComponent {
	private final TableLayout layout;

	public Table () {
		this(new TableLayout());
	}

	public Table (final TableLayout layout) {
		this.layout = layout;
		layout.setTable(this);

		setLayout(new LayoutManager() {
			private Dimension minSize = new Dimension(), prefSize = new Dimension();

			public Dimension preferredLayoutSize (Container parent) {
				layout.layout(); // BOZO - Cache layout?
				prefSize.width = layout.getMinWidth();
				prefSize.height = layout.getMinHeight();
				return prefSize;
			}

			public Dimension minimumLayoutSize (Container parent) {
				layout.layout(); // BOZO - Cache layout?
				minSize.width = layout.getMinWidth();
				minSize.height = layout.getMinHeight();
				return minSize;
			}

			public void layoutContainer (Container ignored) {
				layout.layout();
			}

			public void addLayoutComponent (String name, Component comp) {
			}

			public void removeLayoutComponent (Component comp) {
			}
		});
	}

	/** Removes all Components and cells from the table. */
	public void clear () {
		layout.clear();
		invalidate();
	}

	public Component register (String name, Component widget) {
		return layout.register(name, widget);
	}

	public Cell addCell (String text) {
		return addCell(new JLabel(text));
	}

	/** Adds a cell with a placeholder Component. */
	public Cell addCell () {
		return addCell((Component)null);
	}

	/** Adds a new cell to the table with the specified Component.
	 * @see TableLayout#add(Component)
	 * @param Component May be null to add a cell without an Component. */
	public Cell addCell (Component Component) {
		return layout.add(Component);
	}

	/** Adds a new cell to the table with the specified Components in a {@link Stack}.
	 * @see TableLayout#stack(Component...)
	 * @param Component May be null to add a cell without an Component. */
	public Cell stack (Component... Component) {
		return layout.stack(Component);
	}

	/** Creates a new table with the same Skin and AssetManager as this table. */
	public Table newTable () {
		return layout.getToolkit().newTable(this);
	}

	/** Indicates that subsequent cells should be added to a new row and returns the cell values that will be used as the defaults
	 * for all cells in the new row.
	 * @see TableLayout#row() */
	public Cell row () {
		return layout.row();
	}

	/** Parses a table description and adds the Components and cells to the table.
	 * @see TableLayout#parse(String) */
	public void parse (String tableDescription) {
		layout.parse(tableDescription);
	}

	/** Gets the cell values that will be used as the defaults for all cells in the specified column.
	 * @see TableLayout#columnDefaults(int) */
	public Cell columnDefaults (int column) {
		return layout.columnDefaults(column);
	}

	/** The cell values that will be used as the defaults for all cells.
	 * @see TableLayout#defaults() */
	public Cell defaults () {
		return layout.defaults();
	}

	/** Positions and sizes children of the Component being laid out using the cell associated with each child.
	 * @see TableLayout#layout() */
	public void layout () {
		layout.layout();
	}

	/** Removes all Components and cells from the table (same as {@link #clear()}) and additionally resets all table properties and
	 * cell, column, and row defaults.
	 * @see TableLayout#reset() */
	public void reset () {
		layout.reset();
	}

	/** Returns the widget with the specified name, anywhere in the table hierarchy. */
	public Component getWidget (String name) {
		return layout.getWidget(name);
	}

	/** Returns all named widgets, anywhere in the table hierarchy. */
	public List<Component> getWidgets () {
		return layout.getWidgets();
	}

	/** Returns all widgets with the specified name prefix, anywhere in the table hierarchy. */
	public List<Component> getWidgets (String namePrefix) {
		return layout.getWidgets(namePrefix);
	}

	/** Returns the cell for the specified Component, anywhere in the table hierarchy.
	 * @see TableLayout#getCell(Component) */
	public Cell getCell (Component Component) {
		return layout.getCell(Component);
	}

	/** Returns the cell with the specified name, anywhere in the table hierarchy.
	 * @see TableLayout#getCell(String) */
	public Cell getCell (String name) {
		return layout.getCell(name);
	}

	/** Returns all cells, anywhere in the table hierarchy.
	 * @see TableLayout#getAllCells() */
	public List<Cell> getAllCells () {
		return layout.getAllCells();
	}

	/** Returns all cells with the specified name prefix, anywhere in the table hierarchy.
	 * @see TableLayout#getAllCells(String) */
	public List<Cell> getAllCells (String namePrefix) {
		return layout.getAllCells(namePrefix);
	}

	/** Returns the cells for this table.
	 * @see TableLayout#getCells() */
	public List<Cell> getCells () {
		return layout.getCells();
	}

	/** Sets the Component in the cell with the specified name.
	 * @see TableLayout#setWidget(String, Component) */
	public void setWidget (String name, Component Component) {
		layout.setWidget(name, Component);
	}

	/** The fixed size of the table.
	 * @see TableLayout#size(String, String) */
	public Table size (String width, String height) {
		layout.size(width, height);
		return this;
	}

	/** The fixed width of the table, or null.
	 * @see TableLayout#width(String) */
	public Table width (String width) {
		layout.width(width);
		return this;
	}

	/** The fixed height of the table, or null.
	 * @see TableLayout#height(String) */
	public Table height (String height) {
		layout.height(height);
		return this;
	}

	/** The fixed size of the table.
	 * @see TableLayout#size(int, int) */
	public Table size (int width, int height) {
		layout.size(width, height);
		return this;
	}

	/** The fixed width of the table.
	 * @see TableLayout#width(int) */
	public Table width (int width) {
		layout.width(width);
		return this;
	}

	/** The fixed height of the table.
	 * @see TableLayout#height(int) */
	public Table height (int height) {
		layout.height(height);
		return this;
	}

	/** Padding around the table.
	 * @see TableLayout#pad(String) */
	public Table pad (String pad) {
		layout.pad(pad);
		return this;
	}

	/** Padding around the table.
	 * @see TableLayout#pad(String, String, String, String) */
	public Table pad (String top, String left, String bottom, String right) {
		layout.pad(top, left, bottom, right);
		return this;
	}

	/** Padding at the top of the table.
	 * @see TableLayout#padTop(String) */
	public Table padTop (String padTop) {
		layout.padTop(padTop);
		return this;
	}

	/** Padding at the left of the table.
	 * @see TableLayout#padLeft(String) */
	public Table padLeft (String padLeft) {
		layout.padLeft(padLeft);
		return this;
	}

	/** Padding at the bottom of the table.
	 * @see TableLayout#padBottom(String) */
	public Table padBottom (String padBottom) {
		layout.padBottom(padBottom);
		return this;
	}

	/** Padding at the right of the table.
	 * @see TableLayout#padRight(String) */
	public Table padRight (String padRight) {
		layout.padRight(padRight);
		return this;
	}

	/** Padding around the table.
	 * @see TableLayout#pad(int) */
	public Table pad (int pad) {
		layout.pad(pad);
		return this;
	}

	/** Padding around the table.
	 * @see TableLayout#pad(int, int, int, int) */
	public Table pad (int top, int left, int bottom, int right) {
		layout.pad(top, left, bottom, right);
		return this;
	}

	/** Padding at the top of the table.
	 * @see TableLayout#padTop(int) */
	public Table padTop (int padTop) {
		layout.padTop(padTop);
		return this;
	}

	/** Padding at the left of the table.
	 * @see TableLayout#padLeft(int) */
	public Table padLeft (int padLeft) {
		layout.padLeft(padLeft);
		return this;
	}

	/** Padding at the bottom of the table.
	 * @see TableLayout#padBottom(int) */
	public Table padBottom (int padBottom) {
		layout.padBottom(padBottom);
		return this;
	}

	/** Padding at the right of the table.
	 * @see TableLayout#padRight(int) */
	public Table padRight (int padRight) {
		layout.padRight(padRight);
		return this;
	}

	/** Alignment of the table within the Component being laid out. Set to {@link BaseTableLayout#CENTER},
	 * {@link BaseTableLayout#TOP}, {@link BaseTableLayout#BOTTOM} , {@link BaseTableLayout#LEFT} , {@link BaseTableLayout#RIGHT},
	 * or any combination of those.
	 * @see TableLayout#align(int) */
	public Table align (int align) {
		layout.align(align);
		return this;
	}

	/** Alignment of the table within the Component being laid out. Set to "center", "top", "bottom", "left", "right", or a string
	 * containing any combination of those.
	 * @see TableLayout#align(String) */
	public Table align (String value) {
		layout.align(value);
		return this;
	}

	/** Sets the alignment of the table within the Component being laid out to {@link BaseTableLayout#CENTER}.
	 * @see TableLayout#center() */
	public Table center () {
		layout.center();
		return this;
	}

	/** Sets the alignment of the table within the Component being laid out to {@link BaseTableLayout#TOP}.
	 * @see TableLayout#top() */
	public Table top () {
		layout.top();
		return this;
	}

	/** Sets the alignment of the table within the Component being laid out to {@link BaseTableLayout#LEFT}.
	 * @see TableLayout#left() */
	public Table left () {
		layout.left();
		return this;
	}

	/** Sets the alignment of the table within the Component being laid out to {@link BaseTableLayout#BOTTOM}.
	 * @see TableLayout#bottom() */
	public Table bottom () {
		layout.bottom();
		return this;
	}

	/** Sets the alignment of the table within the Component being laid out to {@link BaseTableLayout#RIGHT}.
	 * @see TableLayout#right() */
	public Table right () {
		layout.right();
		return this;
	}

	/** Turns on all debug lines.
	 * @see TableLayout#debug() */
	public Table debug () {
		layout.debug();
		return this;
	}

	/** Turns on debug lines. Set to {@value TableLayout#DEBUG_ALL}, {@value TableLayout#DEBUG_TABLE},
	 * {@value TableLayout#DEBUG_CELL}, {@value TableLayout#DEBUG_WIDGET}, or any combination of those. Set to
	 * {@value TableLayout#DEBUG_NONE} to disable.
	 * @see TableLayout#debug() */
	public Table debug (int debug) {
		layout.debug(debug);
		return this;
	}

	/** Turns on debug lines. Set to "all", "table", "cell", "widget", or a string containing any combination of those. Set to null
	 * to disable.
	 * @see TableLayout#debug(String) */
	public Table debug (String value) {
		layout.debug(value);
		return this;
	}

	public int getDebug () {
		return layout.getDebug();
	}

	public String getPadTop () {
		return layout.getPadTop();
	}

	public String getPadLeft () {
		return layout.getPadLeft();
	}

	public String getPadBottom () {
		return layout.getPadBottom();
	}

	public String getPadRight () {
		return layout.getPadRight();
	}

	public int getAlign () {
		return layout.getAlign();
	}

	public TableLayout getTableLayout () {
		return layout;
	}

	public void invalidate () {
		super.invalidate();
		layout.invalidate();
	}
}
