
package com.esotericsoftware.tablelayout.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.util.ArrayList;

import javax.swing.JComponent;

import com.esotericsoftware.tablelayout.Cell;

public class Table extends JComponent {
	public final SwingTableLayout layout;

	public Table () {
		this(new SwingTableLayout());
	}

	Table (final SwingTableLayout layout) {
		this.layout = layout;

		layout.table = Table.this;

		setLayout(new LayoutManager() {
			private Dimension minSize = new Dimension(), prefSize = new Dimension();

			public Dimension preferredLayoutSize (Container parent) {
				layout.layout(); // BOZO - Cache layout?
				prefSize.width = layout.totalPrefWidth;
				prefSize.height = layout.totalPrefHeight;
				return prefSize;
			}

			public Dimension minimumLayoutSize (Container parent) {
				layout.layout(); // BOZO - Cache layout?
				minSize.width = layout.totalMinWidth;
				minSize.height = layout.totalMinHeight;
				return minSize;
			}

			public void layoutContainer (Container ignored) {
				layout.layout();
			}

			public void addLayoutComponent (String name, Component comp) {
			}

			public void removeLayoutComponent (Component comp) {
			}
		});
	}

	public Component setName (String name, Component widget) {
		return layout.setName(name, widget);
	}

	public void parse (String tableDescription) {
		layout.parse(tableDescription);
	}

	public void layout () {
		layout.layout();
	}

	public Component getWidget (String name) {
		return layout.getWidget(name);
	}

	public void setWidget (String name, Component widget) {
		layout.setWidget(name, widget);
	}

	public Cell getCell (String name) {
		return layout.getCell(name);
	}

	public ArrayList<Cell> getCells () {
		return layout.getCells();
	}

	public Cell getCell (Component widget) {
		return layout.getCell(widget);
	}
}
