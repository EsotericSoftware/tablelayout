
package com.esotericsoftware.tablelayout.android;

import android.content.Context;
import android.widget.FrameLayout;

public class Stack extends FrameLayout {
	public Stack (Context context) {
		super(context);
	}

	protected void onLayout (boolean changed, int left, int top, int right, int bottom) {
		int width = right - left;
		int height = bottom - top;
		for (int i = 0, n = getChildCount(); i < n; i++)
			getChildAt(i).layout(0, 0, width, height);
	}
}
