
package com.esotericsoftware.tablelayout.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

import javax.swing.JComponent;

import com.esotericsoftware.tablelayout.Cell;

public class Table extends JComponent {
	private final TableLayout layout;

	public Table () {
		this(new TableLayout());
	}

	public Table (final TableLayout layout) {
		this.layout = layout;
		layout.setTable(this);

		setLayout(new LayoutManager() {
			private Dimension minSize = new Dimension(), prefSize = new Dimension();

			public Dimension preferredLayoutSize (Container parent) {
				layout.layout(); // BOZO - Cache layout?
				prefSize.width = layout.getMinWidth();
				prefSize.height = layout.getMinHeight();
				return prefSize;
			}

			public Dimension minimumLayoutSize (Container parent) {
				layout.layout(); // BOZO - Cache layout?
				minSize.width = layout.getMinWidth();
				minSize.height = layout.getMinHeight();
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

	public Cell addCell (Component actor) {
		return layout.add(actor);
	}

	public Cell row () {
		return layout.row();
	}

	public void parse (String tableDescription) {
		layout.parse(tableDescription);
	}

	public Cell columnDefaults (int column) {
		return layout.columnDefaults(column);
	}

	public Cell defaults () {
		return layout.defaults();
	}

	public TableLayout getTableLayout () {
		return layout;
	}
	
	public void invalidate () {
		layout.invalidate();
		super.invalidate();
	}
}
