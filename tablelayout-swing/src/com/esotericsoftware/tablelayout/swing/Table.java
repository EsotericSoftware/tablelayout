
package com.esotericsoftware.tablelayout.swing;

import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;

import com.esotericsoftware.tablelayout.BaseTableLayout;
import com.esotericsoftware.tablelayout.BaseTableLayout.Debug;
import com.esotericsoftware.tablelayout.Cell;
import com.esotericsoftware.tablelayout.Toolkit;
import com.esotericsoftware.tablelayout.Value;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

public class Table extends JComponent {
	static {
		Toolkit.instance = new SwingToolkit();
	}

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
				prefSize.width = (int)layout.getMinWidth();
				prefSize.height = (int)layout.getMinHeight();
				return prefSize;
			}

			public Dimension minimumLayoutSize (Container parent) {
				layout.layout(); // BOZO - Cache layout?
				minSize.width = (int)layout.getMinWidth();
				minSize.height = (int)layout.getMinHeight();
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

	/** Adds a new cell to the table with the specified Components in a {@link Stack}.
	 * @param components May be null to add a cell without an Component. */
	public Cell stack (Component... components) {
		Stack stack = new Stack();
		for (int i = 0, n = components.length; i < n; i++)
			stack.add(components[i]);
		return addCell(stack);
	}

	/** Positions and sizes children of the Component being laid out using the cell associated with each child.
	 * @see TableLayout#layout() */
	public void layout () {
		layout.layout();
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

	/** Invalidates the layout. The cached min and pref sizes are recalculated the next time layout is done or the min or pref
	 * sizes are accessed. */
	public void invalidate () {
		super.invalidate();
		layout.invalidate();
	}

	/** Invalidates the layout of this table and every parent widget. */
	public void invalidateHierarchy () {
		layout.invalidateHierarchy();
	}

	/** Indicates that subsequent cells should be added to a new row and returns the cell values that will be used as the defaults
	 * for all cells in the new row. */
	public Cell row () {
		return layout.row();
	}

	/** Gets the cell values that will be used as the defaults for all cells in the specified column. Columns are indexed starting
	 * at 0. */
	public Cell columnDefaults (int column) {
		return layout.columnDefaults(column);
	}

	/** Removes all widgets and cells from the table (same as {@link #clear()}) and additionally resets all table properties and
	 * cell, column, and row defaults. */
	public void reset () {
		layout.reset();
	}

	/** Removes all widgets and cells from the table. */
	public void clear () {
		layout.clear();
		invalidate();
	}

	/** Returns the cell for the specified widget in this table, or null. */
	public Cell getCell (Component widget) {
		return layout.getCell(widget);
	}

	/** Returns the cells for this table. */
	public List<Cell> getCells () {
		return layout.getCells();
	}

	/** The minimum width of the table. */
	public float getMinWidth () {
		return layout.getMinWidth();
	}

	/** The minimum size of the table. */
	public float getMinHeight () {
		return layout.getMinHeight();
	}

	/** The preferred width of the table. */
	public float getPrefWidth () {
		return layout.getPrefWidth();
	}

	/** The preferred height of the table. */
	public float getPrefHeight () {
		return layout.getPrefHeight();
	}

	/** The cell values that will be used as the defaults for all cells. */
	public Cell defaults () {
		return layout.defaults();
	}

	/** Sets the padTop, padLeft, padBottom, and padRight around the table to the specified value. */
	public Table pad (Value pad) {
		layout.pad(pad);
		return this;
	}

	public Table pad (Value top, Value left, Value bottom, Value right) {
		layout.pad(top, left, bottom, right);
		return this;
	}

	/** Padding at the top edge of the table. */
	public Table padTop (Value padTop) {
		layout.padTop(padTop);
		return this;
	}

	/** Padding at the left edge of the table. */
	public Table padLeft (Value padLeft) {
		layout.padLeft(padLeft);
		return this;
	}

	/** Padding at the bottom edge of the table. */
	public Table padBottom (Value padBottom) {
		layout.padBottom(padBottom);
		return this;
	}

	/** Padding at the right edge of the table. */
	public Table padRight (Value padRight) {
		layout.padRight(padRight);
		return this;
	}

	/** Sets the padTop, padLeft, padBottom, and padRight around the table to the specified value. */
	public Table pad (float pad) {
		layout.pad(pad);
		return this;
	}

	public Table pad (float top, float left, float bottom, float right) {
		layout.pad(top, left, bottom, right);
		return this;
	}

	/** Padding at the top edge of the table. */
	public Table padTop (float padTop) {
		layout.padTop(padTop);
		return this;
	}

	/** Padding at the left edge of the table. */
	public Table padLeft (float padLeft) {
		layout.padLeft(padLeft);
		return this;
	}

	/** Padding at the bottom edge of the table. */
	public Table padBottom (float padBottom) {
		layout.padBottom(padBottom);
		return this;
	}

	/** Padding at the right edge of the table. */
	public Table padRight (float padRight) {
		layout.padRight(padRight);
		return this;
	}

	/** Alignment of the logical table within the table widget. Set to {@link BaseTableLayout#CENTER}, {@link BaseTableLayout#TOP},
	 * {@link BaseTableLayout#BOTTOM} , {@link BaseTableLayout#LEFT}, {@link BaseTableLayout#RIGHT}, or any combination of
	 * those. */
	public Table align (int align) {
		layout.align(align);
		return this;
	}

	/** Sets the alignment of the logical table within the table widget to {@link BaseTableLayout#CENTER}. This clears any other
	 * alignment. */
	public Table center () {
		layout.center();
		return this;
	}

	/** Adds {@link BaseTableLayout#TOP} and clears {@link BaseTableLayout#BOTTOM} for the alignment of the logical table within
	 * the table widget. */
	public Table top () {
		layout.top();
		return this;
	}

	/** Adds {@link BaseTableLayout#LEFT} and clears {@link BaseTableLayout#RIGHT} for the alignment of the logical table within
	 * the table widget. */
	public Table left () {
		layout.left();
		return this;
	}

	/** Adds {@link BaseTableLayout#BOTTOM} and clears {@link BaseTableLayout#TOP} for the alignment of the logical table within
	 * the table widget. */
	public Table bottom () {
		layout.bottom();
		return this;
	}

	/** Adds {@link BaseTableLayout#RIGHT} and clears {@link BaseTableLayout#LEFT} for the alignment of the logical table within
	 * the table widget. */
	public Table right () {
		layout.right();
		return this;
	}

	/** Turns on all debug lines. */
	public Table debug () {
		layout.debug();
		return this;
	}

	/** Turns on table debug lines. */
	public Table debugTable () {
		layout.debugTable();
		return this;
	}

	/** Turns on cell debug lines. */
	public Table debugCell () {
		layout.debugCell();
		return this;
	}

	/** Turns on widget debug lines. */
	public Table debugWidget () {
		layout.debugWidget();
		return this;
	}

	/** Turns on debug lines. */
	public Table debug (Debug debug) {
		layout.debug(debug);
		return this;
	}

	public Debug getDebug () {
		return layout.getDebug();
	}

	public Value getPadTopValue () {
		return layout.getPadTopValue();
	}

	public float getPadTop () {
		return layout.getPadTop();
	}

	public Value getPadLeftValue () {
		return layout.getPadLeftValue();
	}

	public float getPadLeft () {
		return layout.getPadLeft();
	}

	public Value getPadBottomValue () {
		return layout.getPadBottomValue();
	}

	public float getPadBottom () {
		return layout.getPadBottom();
	}

	public Value getPadRightValue () {
		return layout.getPadRightValue();
	}

	public float getPadRight () {
		return layout.getPadRight();
	}

	public int getAlign () {
		return layout.getAlign();
	}

	/** Returns the row index for the y coordinate, or -1 if there are no cells. */
	public int getRow (float y) {
		return layout.getRow(y);
	}

	public BaseTableLayout getTableLayout () {
		return layout;
	}
}
