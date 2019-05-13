
package com.esotericsoftware.tablelayout.android;

import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;

import com.esotericsoftware.tablelayout.BaseTableLayout.Debug;
import com.esotericsoftware.tablelayout.Cell;
import com.esotericsoftware.tablelayout.Toolkit;

import java.util.List;

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

	public Cell add (View view) {
		return layout.add(view);
	}

	public Cell row () {
		return layout.row();
	}

	public Cell columnDefaults (int column) {
		return layout.columnDefaults(column);
	}

	public Cell defaults () {
		return layout.defaults();
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

	public BaseTableLayout getTableLayout () {
		return layout;
	}
}
