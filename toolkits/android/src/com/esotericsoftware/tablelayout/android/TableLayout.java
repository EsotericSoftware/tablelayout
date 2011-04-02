
package com.esotericsoftware.tablelayout.android;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

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
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.esotericsoftware.tablelayout.BaseTableLayout;

public class TableLayout extends BaseTableLayout<View> {
	static {
		addClassPrefix("com.badlogic.gdx.scenes.scene2d.");
		addClassPrefix("com.badlogic.gdx.scenes.scene2d.views.");
	}

	static final HashMap<String, Integer> drawableToID = new HashMap();
	static float scale = 1;
	static Paint paint;

	final Context context;
	ViewGroup group;
	ArrayList<DebugRect> debugRects;

	public TableLayout (Context context) {
		this(context, null);
	}

	public TableLayout (Context context, String tableText) {
		super(tableText);
		this.context = context;
		initialize();
	}

	private TableLayout (TableLayout parent) {
		super(parent);
		this.context = parent.context;
		initialize();
	}

	private void initialize () {
		group = new ViewGroup(context) {
			protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
				boolean widthUnspecified = MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED;
				boolean heightUnspecified = MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED;

				tableLayoutWidth = widthUnspecified ? 0 : MeasureSpec.getSize(widthMeasureSpec);
				tableLayoutHeight = heightUnspecified ? 0 : MeasureSpec.getSize(heightMeasureSpec);

				measureChildren(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

				TableLayout.this.layout();

				int measuredWidth = widthUnspecified ? totalMinWidth : totalPrefWidth;
				int measuredHeight = heightUnspecified ? totalMinHeight : totalPrefHeight;
				setMeasuredDimension(resolveSize(measuredWidth, widthMeasureSpec), resolveSize(measuredHeight, heightMeasureSpec));
			}

			protected void onLayout (boolean changed, int left, int top, int right, int bottom) {
				tableLayoutWidth = right - left;
				tableLayoutHeight = bottom - top;

				if (debug != null && debugRects != null) debugRects.clear();
				TableLayout.this.layout();
				ArrayList<Cell> cells = getCells();
				for (int i = 0, n = cells.size(); i < n; i++) {
					Cell c = cells.get(i);
					if (c.ignore) continue;
					((View)c.widget).layout(c.widgetX, c.widgetY, c.widgetX + c.widgetWidth, c.widgetY + c.widgetHeight);
				}
				if (debug != null && debugRects != null) {
					group.setWillNotDraw(false);
					invalidate();
				}
			}

			protected int getSuggestedMinimumWidth () {
				return totalMinWidth;
			}

			protected int getSuggestedMinimumHeight () {
				return totalMinHeight;
			}

			protected void dispatchDraw (Canvas canvas) {
				super.dispatchDraw(canvas);
				if (debug == null || debugRects == null) return;
				if (paint == null) {
					paint = new Paint();
					paint.setStyle(Paint.Style.STROKE);
					paint.setStrokeWidth(1);
				}
				for (int i = 0, n = debugRects.size(); i < n; i++) {
					DebugRect rect = debugRects.get(i);
					paint.setColor(rect.dash ? Color.GREEN : Color.RED);
					canvas.drawRect(rect.rect, paint);
				}
			}
		};
		group.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	}

	public View getWidget (String name) {
		View view = super.getWidget(name);
		if (view == null) view = getImageView(name);
		if (view == null) view = group.findViewWithTag(name);
		return view;
	}

	protected void addWidget (View child) {
		group.addView(child);
	}

	protected void removeWidget (View child) {
		group.removeView(child);
	}

	public void invalidate () {
		group.requestLayout();
	}

	protected TableLayout newTableLayout () {
		return new TableLayout(this);
	}

	protected TextView newLabel (String text) {
		TextView textView = new TextView(group.getContext());
		textView.setText(text);
		return textView;
	}

	protected void setTitle (View parent, String title) {
	}

	protected void addChild (View parent, View child, String layoutString) {
		((ViewGroup)parent).addView(child);
	}

	protected View wrap (Object object) {
		if (object instanceof View) return (View)object;
		if (object instanceof TableLayout) return ((TableLayout)object).getView();
		if (object instanceof String) return newLabel((String)object);
		if (object == null) return new FrameLayout(group.getContext());
		throw new IllegalArgumentException("Unknown object: " + object);
	}

	protected int getMinWidth (View view) {
		return view.getMeasuredWidth();
	}

	protected int getMinHeight (View view) {
		return view.getMeasuredHeight();
	}

	protected int getPrefWidth (View view) {
		return view.getMeasuredWidth();
	}

	protected int getPrefHeight (View view) {
		return view.getMeasuredHeight();
	}

	protected int getMaxWidth (View view) {
		return 0;
	}

	protected int getMaxHeight (View view) {
		return 0;
	}

	public void drawDebugRect (boolean dash, int x, int y, int w, int h) {
		if (debugRects == null) debugRects = new ArrayList();
		debugRects.add(new DebugRect(dash, x, y, w, h));
	}

	protected int scale (String value) {
		return (int)(Integer.parseInt(value) * scale);
	}

	public ViewGroup getView () {
		return group;
	}

	public Drawable getDrawable (String name) {
		Integer id = drawableToID.get(name);
		if (id == null) throw new IllegalArgumentException("Unknown drawable name: " + name);
		return context.getResources().getDrawable(id);
	}

	public ImageView getImageView (String name) {
		Integer id = drawableToID.get(name);
		if (id != null) {
			ImageView view = new ImageView(context);
			view.setScaleType(ScaleType.FIT_XY);
			view.setImageResource(id);
			return view;
		}
		return null;
	}

	public Object newWidget (String className) throws Exception {
		if (className.equals("button")) return new Button(context);

		return super.newWidget(className);
	}

	public void setProperty (Object object, String name, ArrayList<String> values) {
		if (values.size() == 1) {
			if (object instanceof View) {
				if (setBackground((View)object, name, values.get(0))) return;
			}
			if (object instanceof TextView) {
				if (setCompoundDrawable((TextView)object, name, values.get(0))) return;
			}
		}

		super.setProperty(object, name, values);
	}

	public boolean setCompoundDrawable (TextView view, String name, String value) {
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

	public boolean setBackground (View view, String name, String value) {
		if (name.equals("image")) {
			if (view.getBackground() instanceof StateListDrawable)
				setBackgroundState(view, 0, value);
			else
				view.setBackgroundDrawable(getDrawable(value));
			return true;
		}

		if (name.equals("pressed")) {
			ensureBackgroundStateList(view);
			setBackgroundState(view, R.attr.state_pressed, value);
			return true;
		}

		if (name.equals("focused")) {
			ensureBackgroundStateList(view);
			setBackgroundState(view, R.attr.state_focused, value);
			return true;
		}

		return false;
	}

	private void ensureBackgroundStateList (View view) {
		Drawable background = view.getBackground();
		if (background instanceof StateListDrawable) return;
		StateListDrawable states = new StateListDrawable();
		if (background != null) states.addState(new int[] {0}, background);
		view.setBackgroundDrawable(states);
	}

	public void setBackgroundState (View view, int state, String value) {
		if (!(view.getBackground() instanceof StateListDrawable))
			throw new RuntimeException("View must have a StateListDrawable background: " + view.getBackground());
		StateListDrawable states = (StateListDrawable)view.getBackground();
		Drawable drawable = getDrawable(value);
		states.addState(new int[] {state}, drawable);
	}

	static public void setup (Activity activity, Class drawableClass) {
		if (!drawableClass.getName().endsWith(".R$drawable"))
			throw new RuntimeException("The drawable class must be R.drawable: " + drawableClass);

		DisplayMetrics metrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		scale = metrics.density;
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

	static private class DebugRect {
		final boolean dash;
		final Rect rect;

		public DebugRect (boolean dash, int x, int y, int width, int height) {
			rect = new Rect(x, y, x + width, y + height);
			this.dash = dash;
		}
	}
}
