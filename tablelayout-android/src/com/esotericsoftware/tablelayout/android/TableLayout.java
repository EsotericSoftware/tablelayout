
package com.esotericsoftware.tablelayout.android;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import com.esotericsoftware.tablelayout.BaseTableLayout;
import com.esotericsoftware.tablelayout.Cell;
import com.esotericsoftware.tablelayout.android.AndroidToolkit.DebugRect;

public class TableLayout extends BaseTableLayout<View, Table, TableLayout, AndroidToolkit> {
	ArrayList<View> otherChildren = new ArrayList(1);
	ArrayList<DebugRect> debugRects;

	public TableLayout () {
		super(AndroidToolkit.instance);
	}

	public TableLayout (AndroidToolkit toolkit) {
		super(toolkit);
	}

	public Cell add (View widget) {
		Cell cell = super.add(widget);
		otherChildren.remove(widget);
		return cell;
	}

	public View getWidget (String name) {
		View view = super.getWidget(name);
		if (view == null) view = getTable().findViewWithTag(name);
		return view;
	}

	public void layout () {
		List<Cell> cells = getCells();
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.getIgnore()) continue;
			((View)c.getWidget()).layout(c.getWidgetX(), c.getWidgetY(), //
				c.getWidgetX() + c.getWidgetWidth(), //
				c.getWidgetY() + c.getWidgetHeight());
		}

		super.layout();

		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.getIgnore()) continue;
			((View)c.getWidget()).layout(c.getWidgetX(), c.getWidgetY(), c.getWidgetX() + c.getWidgetWidth(),
				c.getWidgetY() + c.getWidgetHeight());
		}

		for (int i = 0, n = otherChildren.size(); i < n; i++) {
			View child = otherChildren.get(i);
			child.layout(0, 0, getLayoutWidth(), getLayoutHeight());
		}
	}

	public void invalidate () {
		getTable().requestLayout();
	}

	public void invalidateHierarchy () {
		getTable().requestLayout();
	}

	public void drawDebug (Canvas canvas) {
		if (getDebug() == DEBUG_NONE || debugRects == null) return;
		Paint paint = AndroidToolkit.getDebugPaint();
		for (int i = 0, n = debugRects.size(); i < n; i++) {
			DebugRect rect = debugRects.get(i);
			int r = (rect.type & DEBUG_CELL) != 0 ? 255 : 0;
			int g = (rect.type & DEBUG_WIDGET) != 0 ? 255 : 0;
			int b = (rect.type & DEBUG_TABLE) != 0 ? 255 : 0;
			paint.setColor(Color.argb(255, r, g, b));
			canvas.drawRect(rect.rect, paint);
		}
	}
}
