
package com.esotericsoftware.tablelayout.android;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.R;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.esotericsoftware.tablelayout.BaseTableLayout;
import com.esotericsoftware.tablelayout.Cell;

public class TableLayout extends BaseTableLayout<View> {
	static {
		addClassPrefix("android.view.");
	}

	static public Context context;
	static public float density = 1;

	static final HashMap<String, Integer> drawableToID = new HashMap();
	static Paint paint;

	Table table;
	ArrayList<View> otherChildren = new ArrayList(1);
	ArrayList<DebugRect> debugRects;

	public View getWidget (String name) {
		View view = super.getWidget(name);
		if (view == null) view = table.findViewWithTag(name);
		return view;
	}

	public void layout () {
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;
			((View)c.widget).layout(c.widgetX, c.widgetY, c.widgetX + c.widgetWidth, c.widgetY + c.widgetHeight);
		}

		super.layout();

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

	public void addDebugRectangle (String type, int x, int y, int w, int h) {
		if (debugRects == null) debugRects = new ArrayList();
		debugRects.add(new DebugRect(type, x, y, w, h));
	}

	public int size (String value) {
		return (int)(super.size(value) * density);
	}

	public int size (float value) {
		return (int)(value * density);
	}

	public void addChild (View parent, View child, String layoutString) {
		((ViewGroup)parent).addView(child);
	}

	public void removeChild (View parent, View child) {
		((ViewGroup)parent).removeView(child);
	}

	public BaseTableLayout newTableLayout () {
		TableLayout layout = new Table().layout;
		layout.setParent(this);
		return layout;
	}

	public View newWidget (String className) {
		if (className.equals("button")) return new Button(context);
		try {
			return super.newWidget(className);
		} catch (RuntimeException ex) {
			ImageView image = getImageView(className);
			if (image != null) return image;
			throw ex;
		}
	}

	public View wrap (Object object) {
		if (object instanceof String) {
			TextView textView = new TextView(context);
			textView.setText((String)object);
			return textView;
		}
		if (object == null) return new FrameLayout(context);
		return super.wrap(object);
	}

	public View newStack () {
		return new Stack(context);
	}

	public int getMinWidth (View view) {
		return view.getMeasuredWidth();
	}

	public int getMinHeight (View view) {
		return view.getMeasuredHeight();
	}

	public int getPrefWidth (View view) {
		return view.getMeasuredWidth();
	}

	public int getPrefHeight (View view) {
		return view.getMeasuredHeight();
	}

	public int getMaxWidth (View view) {
		return 0;
	}

	public int getMaxHeight (View view) {
		return 0;
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
			int r = rect.type.equals(DEBUG_CELL) ? 255 : 0;
			int g = rect.type.equals(DEBUG_WIDGET) ? 255 : 0;
			int b = rect.type.equals(DEBUG_TABLE) ? 255 : 0;
			paint.setColor(Color.argb(255, r, g, b));
			canvas.drawRect(rect.rect, paint);
		}
	}

	public Table getTable () {
		return table;
	}

	public void setProperty (View view, String name, List<String> values) {
		if (values.size() == 1) {
			if (setBackground(view, name, values.get(0))) return;
			if (view instanceof TextView) {
				if (setCompoundDrawable((TextView)view, name, values.get(0))) return;
			}
		}

		super.setProperty(view, name, values);
	}

	static public Drawable getDrawable (String name) {
		Integer id = drawableToID.get(name);
		if (id == null) throw new IllegalArgumentException("Unknown drawable name: " + name);
		return context.getResources().getDrawable(id);
	}

	static public ImageView getImageView (String name) {
		Integer id = drawableToID.get(name);
		if (id != null) {
			ImageView view = new ImageView(context);
			view.setScaleType(ScaleType.FIT_XY);
			view.setImageResource(id);
			return view;
		}
		return null;
	}

	static public boolean setCompoundDrawable (TextView view, String name, String value) {
		if (name.equals("left")) {
			Drawable[] drawables = view.getCompoundDrawables();
			view.setCompoundDrawablesWithIntrinsicBounds(getDrawable(value), drawables[1], drawables[2], drawables[3]);
			return true;
		}

		if (name.equals("top")) {
			Drawable[] drawables = view.getCompoundDrawables();
			view.setCompoundDrawablesWithIntrinsicBounds(drawables[0], getDrawable(value), drawables[2], drawables[3]);
			return true;
		}

		if (name.equals("right")) {
			Drawable[] drawables = view.getCompoundDrawables();
			view.setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], getDrawable(value), drawables[3]);
			return true;
		}

		if (name.equals("bottom")) {
			Drawable[] drawables = view.getCompoundDrawables();
			view.setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], drawables[2], getDrawable(value));
			return true;
		}

		return false;
	}

	static public boolean setBackground (View view, String name, String value) {
		if (name.equals("bg")) {
			view.setBackgroundDrawable(getDrawable(value));
			return true;
		}

		if (name.equals("normal")) {
			setBackgroundState(view, 0, value);
			return true;
		}

		if (name.equals("pressed")) {
			setBackgroundState(view, R.attr.state_pressed, value);
			return true;
		}

		if (name.equals("focused")) {
			setBackgroundState(view, R.attr.state_focused, value);
			return true;
		}

		return false;
	}

	static public void setBackgroundState (View view, int state, String value) {
		Drawable background = view.getBackground();
		StateListDrawable states;
		if (background instanceof CustomizedStateListDrawable)
			states = (StateListDrawable)background;
		else {
			states = new CustomizedStateListDrawable();
			view.setBackgroundDrawable(states);
		}
		states.addState(new int[] {state}, getDrawable(value));
	}

	static public void setup (Activity activity, Class drawableClass) {
		context = activity;
		if (!drawableClass.getName().endsWith(".R$drawable"))
			throw new RuntimeException("The drawable class must be R.drawable: " + drawableClass);

		DisplayMetrics metrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		density = metrics.density;

		drawableToID.clear();
		Field[] fields = drawableClass.getFields();
		for (int i = 0, n = fields.length; i < n; i++) {
			Field field = fields[i];
			try {
				drawableToID.put(field.getName(), field.getInt(null));
			} catch (Exception ex) {
				throw new RuntimeException("Error getting drawable field value: " + field, ex);
			}
		}
	}

	/**
	 * Marker class to know when the background is no longer the default.
	 */
	static class CustomizedStateListDrawable extends StateListDrawable {
	}

	static private class Stack extends FrameLayout {
		public Stack (Context context) {
			super(context);
		}

		protected void onLayout (boolean changed, int left, int top, int right, int bottom) {
			for (int i = 0, n = getChildCount(); i < n; i++)
				getChildAt(i).layout(left, top, right, bottom);
		}
	}

	static private class DebugRect {
		final String type;
		final Rect rect;

		public DebugRect (String type, int x, int y, int width, int height) {
			rect = new Rect(x, y, x + width - 1, y + height - 1);
			this.type = type;
		}
	}
}
