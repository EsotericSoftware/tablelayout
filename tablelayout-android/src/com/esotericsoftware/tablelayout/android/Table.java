
package com.esotericsoftware.tablelayout.android;

import java.util.List;

import com.esotericsoftware.tablelayout.BaseTableLayout.Debug;
import com.esotericsoftware.tablelayout.BaseTableLayout;
import com.esotericsoftware.tablelayout.Cell;
import com.esotericsoftware.tablelayout.Toolkit;
import com.esotericsoftware.tablelayout.Value;

import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;

public class Table extends ViewGroup {
	static {
		Toolkit.instance = new AndroidToolkit();
	}

	static private final OnHierarchyChangeListener hierarchyChangeListener = new OnHierarchyChangeListener() {
		public void onChildViewAdded (View parent, View child) {
			((Table)parent).layout.otherChildren.add(child);
		}

		public void onChildViewRemoved (View parent, View child) {
			((Table)parent).layout.otherChildren.remove(child);
		}
	};

	final TableLayout layout;
	private boolean sizeToBackground;

	public Table () {
		this(new TableLayout());
	}

	public Table (TableLayout layout) {
		super(AndroidToolkit.context);
		this.layout = layout;
		layout.setTable(this);
		setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		setOnHierarchyChangeListener(hierarchyChangeListener);
	}

	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
		boolean widthUnspecified = MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED;
		boolean heightUnspecified = MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED;

		measureChildren(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
			MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

		// Measure GONE children to 0x0.
		List<Cell> cells = layout.getCells();
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.getIgnore()) continue;
			if (((View)c.getWidget()).getVisibility() == GONE) {
				((View)c.getWidget()).measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY),
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY));
			}
		}

		layout.layout(0, 0, //
			widthUnspecified ? 0 : MeasureSpec.getSize(widthMeasureSpec), //
			heightUnspecified ? 0 : MeasureSpec.getSize(heightMeasureSpec));

		int measuredWidth = (int)(widthUnspecified ? layout.getMinWidth() : layout.getPrefWidth());
		int measuredHeight = (int)(heightUnspecified ? layout.getMinHeight() : layout.getPrefHeight());

		invalidate();
		measuredWidth = Math.max(measuredWidth, getSuggestedMinimumWidth());
		measuredHeight = Math.max(measuredHeight, getSuggestedMinimumHeight());

		setMeasuredDimension(resolveSize(measuredWidth, widthMeasureSpec), resolveSize(measuredHeight, heightMeasureSpec));
	}

	protected void onLayout (boolean changed, int left, int top, int right, int bottom) {
		layout.layout(0, 0, right - left, bottom - top);

		if (layout.getDebug() != Debug.none && layout.debugRects != null) {
			setWillNotDraw(false);
			invalidate();
		}
	}

	public void requestLayout () {
		if (layout != null) layout.invalidateSuper();
		super.requestLayout();
	}

	protected int getSuggestedMinimumWidth () {
		int width = (int)layout.getMinWidth();
		if (sizeToBackground && getBackground() != null) width = Math.max(width, getBackground().getMinimumWidth());
		return width;
	}

	protected int getSuggestedMinimumHeight () {
		int height = (int)layout.getMinHeight();
		if (sizeToBackground && getBackground() != null) height = Math.max(height, getBackground().getMinimumHeight());
		return height;
	}

	protected void dispatchDraw (Canvas canvas) {
		super.dispatchDraw(canvas);
		layout.drawDebug(canvas);
	}

	public void setSizeToBackground (boolean sizeToBackground) {
		this.sizeToBackground = sizeToBackground;
	}

	/** Adds a new cell to the table with the specified widget. */
	public Cell add (View widget) {
		return layout.add(widget);
	}

	/** Invalidates the layout. The cached min and pref sizes are recalculated the next time layout is done or the min or pref
	 * sizes are accessed. */
// Should this be uncommented?
//	public void invalidate () {
//		super.invalidate();
//		layout.invalidate();
//	}

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
	}

	/** Returns the cell for the specified widget in this table, or null. */
	public Cell getCell (View widget) {
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
