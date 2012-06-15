
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

import com.esotericsoftware.tablelayout.Cell;
import com.esotericsoftware.tablelayout.Toolkit;

public class AndroidToolkit extends Toolkit<View, Table, TableLayout> {
	static {
		addClassPrefix("android.view.");
		addClassPrefix("android.widget.");
	}

	static public final AndroidToolkit instance = new AndroidToolkit();
	static public Context context;
	static public float density = 1;

	static final HashMap<String, Integer> drawableToID = new HashMap();
	static Paint paint;

	public Table newTable (Table parent) {
		return new Table();
	}
	
	public void setWidget (TableLayout layout, Cell cell, View widget) {
		super.setWidget(layout, cell, widget);
		layout.otherChildren.remove(widget);
	}

	public TableLayout getLayout (Table table) {
		return table.layout;
	}

	public void clearDebugRectangles (TableLayout layout) {
		if (layout.debugRects != null) layout.debugRects.clear();
	}

	public void addDebugRectangle (TableLayout layout, int type, int x, int y, int w, int h) {
		if (layout.debugRects == null) layout.debugRects = new ArrayList();
		layout.debugRects.add(new DebugRect(type, x, y, w, h));
	}

	public int width (float value) {
		return (int)(value * density);
	}

	public int height (float value) {
		return (int)(value * density);
	}

	public void addChild (View parent, View child, String layoutString) {
		((ViewGroup)parent).addView(child);
	}

	public void removeChild (View parent, View child) {
		((ViewGroup)parent).removeView(child);
	}

	public View newWidget (TableLayout layout, String className) {
		if (className.equals("button")) return new Button(context);
		try {
			return super.newWidget(layout, className);
		} catch (RuntimeException ex) {
			ImageView image = getImageView(className);
			if (image != null) return image;
			throw ex;
		}
	}

	protected View newInstance (TableLayout layout, String className) throws Exception {
		try {
			return super.newInstance(layout, className);
		} catch (Exception ex) {
			Class type = Class.forName(className);
			Constructor constructor = type.getConstructor(Context.class);
			return (View)constructor.newInstance(context);
		}
	}

	public View wrap (TableLayout layout, Object object) {
		if (object instanceof String) {
			TextView textView = new TextView(context);
			textView.setText((String)object);
			return textView;
		}
		if (object == null) return new FrameLayout(context);
		return super.wrap(layout, object);
	}

	protected Object convertType (TableLayout layout, Object parentObject, Class memberType, String memberName, String value) {
		Object newType = super.convertType(layout, parentObject, memberType, memberName, value);
		if (newType == null && memberType == int.class) {
			try {
				return Color.parseColor(value);
			} catch (IllegalArgumentException ignored) {
			}
		}
		return newType;
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

	public void setProperty (TableLayout layout, View view, String name, List<String> values) {
		if (values.size() == 1) {
			if (setBackground(view, name, values.get(0))) return;

			if (view instanceof TextView) {
				if (setCompoundDrawable((TextView)view, name, values.get(0))) return;
			}
		}

		super.setProperty(layout, view, name, values);
	}

	static Paint getDebugPaint () {
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
	static class CustomizedStateListDrawable extends StateListDrawable {
	}

	static public class DebugRect {
		final int type;
		final Rect rect;

		public DebugRect (int type, int x, int y, int width, int height) {
			rect = new Rect(x, y, x + width - 1, y + height - 1);
			this.type = type;
		}
	}
}
