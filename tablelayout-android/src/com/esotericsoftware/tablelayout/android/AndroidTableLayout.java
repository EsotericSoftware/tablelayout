
package com.esotericsoftware.tablelayout.android;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import com.esotericsoftware.tablelayout.Cell;
import com.esotericsoftware.tablelayout.TableLayout;
import com.esotericsoftware.tablelayout.Toolkit;

public class AndroidTableLayout extends TableLayout<View> {
	static Paint paint;

	Table table;
	ArrayList<View> otherChildren = new ArrayList(1);
	ArrayList<DebugRect> debugRects;

	public AndroidTableLayout () {
		super();
	}

	public AndroidTableLayout (TableLayout parent) {
		super(parent);
	}

	public Toolkit getToolkit () {
		return AndroidToolkit.instance;
	}

	public View getWidget (String name) {
		View view = super.getWidget(name);
		if (view == null) view = table.findViewWithTag(name);
		return view;
	}

	public void layout () {
		super.layout();

		List<Cell> cells = getCells();
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;
			((View)c.widget).layout(c.widgetX, c.widgetY, c.widgetX + c.widgetWidth, c.widgetY + c.widgetHeight);
		}

		for (int i = 0, n = otherChildren.size(); i < n; i++) {
			View child = otherChildren.get(i);
			child.layout(0, 0, tableLayoutWidth, tableLayoutHeight);
		}
	}

	public void invalidate () {
		table.requestLayout();
	}

	public void drawDebug (Canvas canvas) {
		if (debug == null || debugRects == null) return;
		if (paint == null) {
			paint = new Paint();
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(1);
		}
		for (int i = 0, n = debugRects.size(); i < n; i++) {
			DebugRect rect = debugRects.get(i);
			paint.setColor(rect.isCell ? Color.RED : Color.GREEN);
			canvas.drawRect(rect.rect, paint);
		}
	}

	public Table getTable () {
		return table;
	}

	public Cell addCell (View widget) {
		Cell cell = super.addCell(widget);
		otherChildren.remove(widget);
		return cell;
	}
	
	public void setWidget (Cell cell, View widget) {
		super.setWidget(cell, widget);
		otherChildren.remove(widget);
	}

	public void clearDebugRectangles () {
		if (debugRects != null) debugRects.clear();
	}

	public void addDebugRectangle (boolean isCell, int x, int y, int w, int h) {
		if (debugRects == null) debugRects = new ArrayList();
		debugRects.add(new DebugRect(isCell, x, y, w, h));
	}

	static private class DebugRect {
		final boolean isCell;
		final Rect rect;

		public DebugRect (boolean isCell, int x, int y, int width, int height) {
			rect = new Rect(x, y, x + width - 1, y + height - 1);
			this.isCell = isCell;

		}
	}
}
