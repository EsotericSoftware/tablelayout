
package com.esotericsoftware.tablelayout.android;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import android.R;
import android.app.Activity;
import android.content.Context;
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

import com.esotericsoftware.tablelayout.TableLayout;
import com.esotericsoftware.tablelayout.Toolkit;

public class AndroidToolkit extends Toolkit<View> {
	static {
		addClassPrefix("android.view.");
	}

	static public AndroidToolkit instance = new AndroidToolkit();
	static public Context context;
	static public float density = 1;

	static final HashMap<String, Integer> drawableToID = new HashMap();

	public AndroidToolkit () {
		super(View.class);
	}

	public int getSize (String value) {
		return (int)(super.getSize(value) * density);
	}

	public int getSize (float value) {
		return (int)(value * density);
	}

	public void addChild (View parent, View child, String layoutString) {
		((ViewGroup)parent).addView(child);
	}

	public void removeChild (View parent, View child) {
		((ViewGroup)parent).removeView(child);
	}

	public TableLayout newTableLayout (TableLayout parent) {
		return new Table(parent).layout;
	}

	public Object newWidget (String className) throws Exception {
		if (className.equals("button")) return new Button(context);
		try {
			return super.newWidget(className);
		} catch (Exception ex) {
			ImageView image = getImageView(className);
			if (image != null) return image;
			throw ex;
		}
	}

	public View newLabel (String text) {
		TextView textView = new TextView(AndroidToolkit.context);
		textView.setText(text);
		return textView;
	}

	public View newEmptyWidget () {
		return new FrameLayout(context);
	}

	public void setTitle (View parent, String title) {
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
	 * Marker class so we know the background is no longer the default.
	 */
	static class CustomizedStateListDrawable extends StateListDrawable {
	}
}
