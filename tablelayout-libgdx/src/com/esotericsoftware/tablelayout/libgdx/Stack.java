
package com.esotericsoftware.tablelayout.libgdx;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Layout;

public class Stack extends Group implements Layout {
	private boolean needsLayout = true;

	public void layout () {
		if (!needsLayout) return;
		needsLayout = false;
		for (int i = 0, n = children.size(); i < n; i++) {
			Actor actor = children.get(i);
			actor.width = width;
			actor.height = height;
			if (actor instanceof Layout) {
				Layout layout = (Layout)actor;
				layout.invalidate();
				layout.layout();
			}
		}
	}

	public void invalidate () {
		needsLayout = true;
	}

	public float getPrefWidth () {
		float width = 0;
		for (int i = 0, n = children.size(); i < n; i++)
			width = Math.max(width, LibgdxToolkit.instance.getPrefWidth(children.get(i)));
		return width * scaleX;
	}

	public float getPrefHeight () {
		float height = 0;
		for (int i = 0, n = children.size(); i < n; i++)
			height = Math.max(height, LibgdxToolkit.instance.getPrefHeight(children.get(i)));
		return height * scaleY;
	}
}
