
package com.esotericsoftware.tablelayout.android;

import java.util.List;

import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;

import com.esotericsoftware.tablelayout.Cell;
import com.esotericsoftware.tablelayout.TableLayout;

public class Table extends ViewGroup {
	static private final OnHierarchyChangeListener hierarchyChangeListener = new OnHierarchyChangeListener() {
		public void onChildViewAdded (View parent, View child) {
			((Table)parent).layout.otherChildren.add(child);
		}

		public void onChildViewRemoved (View parent, View child) {
			((Table)parent).layout.otherChildren.remove(child);
		}
	};

	public final AndroidTableLayout layout;
	public boolean sizeToBackground;

	public Table () {
		this(new AndroidTableLayout());
	}

	public Table (AndroidTableLayout layout) {
		super(AndroidTableLayout.context);
		this.layout = layout;
		layout.table = this;
		setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		setOnHierarchyChangeListener(hierarchyChangeListener);
	}

	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
		boolean widthUnspecified = MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED;
		boolean heightUnspecified = MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED;

		layout.tableLayoutWidth = widthUnspecified ? 0 : MeasureSpec.getSize(widthMeasureSpec);
		layout.tableLayoutHeight = heightUnspecified ? 0 : MeasureSpec.getSize(heightMeasureSpec);

		measureChildren(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
			MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

		layout.layout();

		int measuredWidth = widthUnspecified ? layout.tableMinWidth : layout.tablePrefWidth;
		int measuredHeight = heightUnspecified ? layout.tableMinHeight : layout.tablePrefHeight;

		measuredWidth = Math.max(measuredWidth, getSuggestedMinimumWidth());
		measuredHeight = Math.max(measuredHeight, getSuggestedMinimumHeight());

		setMeasuredDimension(resolveSize(measuredWidth, widthMeasureSpec), resolveSize(measuredHeight, heightMeasureSpec));
	}

	protected void onLayout (boolean changed, int left, int top, int right, int bottom) {
		layout.tableLayoutWidth = right - left;
		layout.tableLayoutHeight = bottom - top;

		layout.layout();

		if (layout.debug != null && layout.debugRects != null) {
			setWillNotDraw(false);
			invalidate();
		}
	}

	protected int getSuggestedMinimumWidth () {
		int width = layout.tableMinWidth;
		if (sizeToBackground && getBackground() != null) width = Math.max(width, getBackground().getMinimumWidth());
		return width;
	}

	protected int getSuggestedMinimumHeight () {
		int height = layout.tableMinHeight;
		if (sizeToBackground && getBackground() != null) height = Math.max(height, getBackground().getMinimumHeight());
		return height;
	}

	protected void dispatchDraw (Canvas canvas) {
		super.dispatchDraw(canvas);
		layout.drawDebug(canvas);
	}
}
