
package com.esotericsoftware.tablelayout.android;

import java.util.ArrayList;

import android.R.layout;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.widget.TextView;

import com.esotericsoftware.tablelayout.BaseTableLayout;

public class TableLayout extends BaseTableLayout<View> {
	static {
		addClassPrefix("com.badlogic.gdx.scenes.scene2d.");
		addClassPrefix("com.badlogic.gdx.scenes.scene2d.views.");
	}

	private ViewGroup group;

	public TableLayout (Context context) {
		this(context, null);
	}

	public TableLayout (Context context, String tableText) {
		super(tableText);
		this.group = new ViewGroup(context) {
			protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
				measureChildren(widthMeasureSpec, heightMeasureSpec);

				tableLayoutWidth = MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED ? Integer.MAX_VALUE : MeasureSpec
					.getMode(widthMeasureSpec);
				tableLayoutHeight = MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED ? Integer.MAX_VALUE
					: MeasureSpec.getMode(heightMeasureSpec);
				TableLayout.this.layout();
				setMeasuredDimension(resolveSize(totalPrefWidth, widthMeasureSpec), resolveSize(totalPrefHeight, heightMeasureSpec));
			}

			protected void onLayout (boolean changed, int left, int top, int right, int bottom) {
				tableLayoutX = left;
				tableLayoutY = top;
				tableLayoutWidth = right - left;
				tableLayoutHeight = bottom - top;
				TableLayout.this.layout();
				ArrayList<Cell> cells = getCells();
				for (int i = 0, n = cells.size(); i < n; i++) {
					Cell c = cells.get(i);
					if (c.ignore) continue;
					View view = (View)c.widget;
					System.out.println(c.widgetX + ", " + c.widgetY + ", " + c.widgetWidth + ", " + c.widgetHeight);
					view.layout(c.widgetX, c.widgetY, c.widgetX + c.widgetWidth, c.widgetY + c.widgetHeight);
				}
			}

			protected int getSuggestedMinimumWidth () {
				return totalMinWidth;
			}

			protected int getSuggestedMinimumHeight () {
				return totalMinHeight;
			}
		};
		group.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	}

	private TableLayout (TableLayout parent) {
		super(parent);
		this.group = parent.group;
	}

	public View getWidget (String name) {
		View view = super.getWidget(name);
		if (view == null) view = group.findViewWithTag(name);
		return view;
	}

	protected TableLayout newTableLayout () {
		return new TableLayout(this);
	}

	protected TextView newLabel (String text) {
		TextView textView = new TextView(group.getContext());
		textView.setText(text);
		group.addView(textView);
		return textView;
	}

	protected void setTitle (View parent, String title) {
	}

	protected void addChild (View parent, View child, String layoutString) {
		((ViewGroup)parent).addView(child);
	}

	protected View wrap (Object object) {
		View view;
		if (object instanceof View)
			view = (View)object;
		else if (object instanceof String)
			view = newLabel((String)object);
		else if (object == null)
			view = new View(group.getContext());
		else
			throw new IllegalArgumentException("Unknown object: " + object);
		if (view.getParent() != group) group.addView(view);
		return view;
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

	protected TableLayout getTableLayout (Object object) {
		if (object instanceof TableLayout) return (TableLayout)object;
		return null;
	}

	protected void drawDebugRect (boolean dash, int x, int y, int w, int h) {
	}

	public ViewGroup getView () {
		return group;
	}
}
