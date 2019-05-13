
package com.esotericsoftware.tablelayout.android;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.R;
import android.app.Activity;
import android.content.Context;
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

import com.esotericsoftware.tablelayout.BaseTableLayout.Debug;
import com.esotericsoftware.tablelayout.Cell;
import com.esotericsoftware.tablelayout.Toolkit;

public class AndroidToolkit extends Toolkit<View, Table, TableLayout> {
	static public Context context;
	static public float density = 1;

	static public final HashMap<String, Integer> drawableToID = new HashMap();
	static public Paint paint;

	public Cell obtainCell (TableLayout layout) {
		Cell cell = new Cell();
		cell.setLayout(layout);
		return cell;
	}

	public void freeCell (Cell cell) {
	}

	public void setWidget (TableLayout layout, Cell cell, View widget) {
		super.setWidget(layout, cell, widget);
		layout.otherChildren.remove(widget);
	}

	public void clearDebugRectangles (TableLayout layout) {
		if (layout.debugRects != null) layout.debugRects.clear();
	}

	public void addDebugRectangle (TableLayout layout, Debug type, float x, float y, float w, float h) {
		if (layout.debugRects == null) layout.debugRects = new ArrayList();
		layout.debugRects.add(new DebugRect(type, (int)x, (int)y, (int)w, (int)h));
	}

	public float width (float value) {
		return (int)(value * density);
	}

	public float height (float value) {
		return (int)(value * density);
	}

	public void addChild (View parent, View child) {
		((ViewGroup)parent).addView(child);
	}

	public void removeChild (View parent, View child) {
		((ViewGroup)parent).removeView(child);
	}

	public float getMinWidth (View view) {
		return view.getMeasuredWidth();
	}

	public float getMinHeight (View view) {
		return view.getMeasuredHeight();
	}

	public float getPrefWidth (View view) {
		return view.getMeasuredWidth();
	}

	public float getPrefHeight (View view) {
		return view.getMeasuredHeight();
	}

	public float getMaxWidth (View view) {
		return 0;
	}

	public float getMaxHeight (View view) {
		return 0;
	}

	public float getWidth (View view) {
		return view.getWidth();
	}

	public float getHeight (View view) {
		return view.getHeight();
	}

	static public Paint getDebugPaint () {
		if (paint == null) {
			paint = new Paint();
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(1);
		}
		return paint;
	}

	static public void setup (Activity activity, Class drawableClass) {
		context = activity;
		// if (!drawableClass.getName().endsWith(".R$drawable"))
		// throw new RuntimeException("The drawable class must be R.drawable: " + drawableClass);

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

	static public int getDrawableID (String name) {
		Integer id = drawableToID.get(name);
		if (id == null) return 0;
		return id;
	}

	static public Drawable getDrawable (String name) {
		Integer id = drawableToID.get(name);
		if (id == null) throw new IllegalArgumentException("Unknown drawable name: " + name);
		return context.getResources().getDrawable(id);
	}

	static public ImageView getImageView (String name) {
		Integer id = drawableToID.get(name);
		if (id != null) return getImageView(id);
		return null;
	}

	static public ImageView getImageView (int id) {
		ImageView view = new ImageView(context);
		view.setScaleType(ScaleType.FIT_XY);
		view.setImageResource(id);
		return view;
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

	/** Marker class to know when the background is no longer the default. */
	static public class CustomizedStateListDrawable extends StateListDrawable {
	}

	static public class DebugRect {
		final Debug type;
		final Rect rect;

		public DebugRect (Debug type, int x, int y, int width, int height) {
			rect = new Rect(x, y, x + width - 1, y + height - 1);
			this.type = type;
		}
	}
}
