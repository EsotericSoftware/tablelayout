
package com.esotericsoftware.tablelayout.android;

import java.awt.Component;
import java.util.ArrayList;

import com.esotericsoftware.tablelayout.Cell;

import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

public class Table extends ViewGroup {
	public final AndroidTableLayout layout = new AndroidTableLayout();

	public Table () {
		super(AndroidToolkit.context);
		layout.table = this;
		setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	}

	public View setName (String name, View widget) {
		return layout.setName(name, widget);
	}

	public void parse (String tableDescription) {
		layout.parse(tableDescription);
	}

	public void layout () {
		layout.layout();
	}

	public View getWidget (String name) {
		return layout.getWidget(name);
	}

	public void setWidget (String name, View view) {
		layout.setWidget(name, view);
	}

	public Cell getCell (String name) {
		return layout.getCell(name);
	}

	public ArrayList<Cell> getCells () {
		return layout.getCells();
	}

	public Cell getCell (View widget) {
		return layout.getCell(widget);
	}

	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
		boolean widthUnspecified = MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED;
		boolean heightUnspecified = MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED;

		layout.tableLayoutWidth = widthUnspecified ? 0 : MeasureSpec.getSize(widthMeasureSpec);
		layout.tableLayoutHeight = heightUnspecified ? 0 : MeasureSpec.getSize(heightMeasureSpec);

		measureChildren(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
			MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

		layout.layout();

		int measuredWidth = widthUnspecified ? layout.totalMinWidth : layout.totalPrefWidth;
		int measuredHeight = heightUnspecified ? layout.totalMinHeight : layout.totalPrefHeight;
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
		return layout.totalMinWidth;
	}

	protected int getSuggestedMinimumHeight () {
		return layout.totalMinHeight;
	}

	protected void dispatchDraw (Canvas canvas) {
		super.dispatchDraw(canvas);
		layout.drawDebug(canvas);
	}
}
