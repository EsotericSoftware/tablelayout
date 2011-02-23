
package com.esotericsoftware.tablelayout.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import com.esotericsoftware.tablelayout.BaseTableLayout;

// BOZO - * for colspan?
// BOZO - Nede min/pref/max for table layout itself?

public class TableLayout extends BaseTableLayout<Component> implements LayoutManager {
	static {
		addClassPrefix("javax.swing.");
		addClassPrefix("java.awt.");
	}

	static private Timer timer;
	static ArrayList<TableLayout> debugLayouts = new ArrayList(0);
	static BasicStroke debugDash = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] {3}, 0);
	static BasicStroke debugSolid = new BasicStroke(1);

	Container debugParent;
	ArrayList<DebugRect> debugRects;

	private Dimension minSize = new Dimension(), prefSize = new Dimension();

	public TableLayout () {
	}

	public TableLayout (String tableText) {
		super(tableText);
	}

	private TableLayout (TableLayout parent) {
		super(parent);
	}

	public void addLayoutComponent (String text, Component comp) {
		add(comp, text);
	}

	public void removeLayoutComponent (Component comp) {
		clear(); // BOZO - Handle removing components.
	}

	public Dimension preferredLayoutSize (Container parent) {
		layout(); // BOZO - Cache layout.
		prefSize.width = totalPrefWidth;
		prefSize.height = totalPrefHeight;
		return prefSize;
	}

	public Dimension minimumLayoutSize (Container parent) {
		layout(); // BOZO - Cache layout.
		minSize.width = totalMinWidth;
		minSize.height = totalMinHeight;
		return minSize;
	}

	public void layoutContainer (Container parent) {
		if (debug != null) {
			debugParent = parent;
			if (debugRects == null) {
				debugRects = new ArrayList();
				debugLayouts.add(this);
			} else
				debugRects.clear();
		} else if (debugRects != null) {
			debugLayouts.remove(this);
			debugRects = null;
		}

		String title = getTitle();
		if (title != null && parent instanceof JComponent) {
			JComponent component = (JComponent)parent;
			Border border = component.getBorder();
			System.out.println(((TitledBorder)border).getTitle());
			if (border == null || !(border instanceof TitledBorder) || !((TitledBorder)border).getTitle().equals(title))
				setTitle(component, title);
		}

		Insets insets = parent.getInsets();
		tableLayoutX = insets.left;
		tableLayoutY = insets.top;
		tableLayoutWidth = parent.getWidth() - insets.left - insets.right;
		tableLayoutHeight = parent.getHeight() - insets.top - insets.bottom;
		layout();
		ArrayList<Cell> cells = getCells();
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.ignore) continue;
			Component component = (Component)c.widget;
			Container componentParent = component.getParent();
			if (componentParent == null)
				parent.add(component);
			else if (componentParent != parent) //
				throw new IllegalStateException("Component has wrong parent: " + component);
			component.setLocation(c.widgetX, c.widgetY);
			component.setSize(c.widgetWidth, c.widgetHeight);
		}

		if (debug != null) {
			if (timer == null) {
				timer = new Timer("TableLayout Debug", true);
				timer.schedule(new TimerTask() {
					public void run () {
						if (!EventQueue.isDispatchThread()) {
							EventQueue.invokeLater(this);
							return;
						}
						for (TableLayout table : debugLayouts)
							table.drawDebug();
					}
				}, 100, 250);
			}
		}
	}

	protected TableLayout newTableLayout () {
		return new TableLayout(this);
	}

	protected Component newLabel (String text) {
		return new JLabel(text);
	}

	protected void setTitle (Component parent, String title) {
		if (!(parent instanceof JComponent)) return;
		((JComponent)parent).setBorder(BorderFactory.createTitledBorder(title));
	}

	protected void addChild (Component parent, Component child, String layoutString) {
		((Container)parent).add(child, layoutString);
	}

	protected Component wrap (Object object) {
		if (object instanceof Component) return (Component)object;
		if (object instanceof LayoutManager) return new JPanel((LayoutManager)object);
		throw new IllegalArgumentException("Unknown object: " + object);
	}

	protected int getMinWidth (Component widget) {
		return widget.getMinimumSize().width;
	}

	protected int getMinHeight (Component widget) {
		return widget.getMinimumSize().height;
	}

	protected int getPrefWidth (Component widget) {
		return widget.getPreferredSize().width;
	}

	protected int getPrefHeight (Component widget) {
		return widget.getPreferredSize().height;
	}

	protected int getMaxWidth (Component widget) {
		return widget.getMaximumSize().width;
	}

	protected int getMaxHeight (Component widget) {
		return widget.getMaximumSize().height;
	}

	protected TableLayout getTableLayout (Object object) {
		if (object instanceof TableLayout) return (TableLayout)object;
		if (object instanceof JComponent) return (TableLayout)((JComponent)object).getLayout();
		return null;
	}

	void drawDebug () {
		Graphics2D g = (Graphics2D)debugParent.getGraphics();
		if (g == null) return;
		g.setColor(Color.red);
		for (DebugRect rect : debugRects) {
			g.setColor(Color.red);
			g.setStroke(rect.dash ? debugDash : debugSolid);
			g.draw(rect);
		}
	}

	protected void drawDebugRect (boolean dash, int x, int y, int w, int h) {
		if (debugRects != null) debugRects.add(new DebugRect(dash, x, y, w, h));
	}

	static private class DebugRect extends Rectangle {
		final boolean dash;

		public DebugRect (boolean dash, int x, int y, int width, int height) {
			super(x, y, width, height);
			this.dash = dash;
		}
	}

	static public void main (String[] args) {
		TableLayout table = new TableLayout();
		for (int i = 1; i <= 10; i++)
			table.setName(i + "", new JTextField(i + ""));

		// table.parse("align:center padding:10" //
		// + "* height:140 expand" //
		// + "[1] width:180 fill:50,50" //
		// + "'2' size:200,20 colspan:2" //
		// + "---" //
		// + "[1] size:40,120" //
		// + "[4] size:180,20" //
		// + "[5] size:20,20" //
		// + "---" //
		// + "[6] size:40,20" //
		// + "[7] size:180,20" //
		// + "---" //
		// + "[8] size:40,20" //
		// + "{padding:10" //
		// + "'Name:' size:50" //
		// + "'111' size:25 " //
		// + "--- " //
		// + "'Stuff:' width:100" //
		// + "'moo' size:25 } fill " //
		// + "[2] size:100,200 " //
		// + "[10] size:20,20" //
		// );

		// table.parse("padding:10 debug " //
		// + "* align:left padding:10 uniform" //
		// + "|  | align:right " //
		// + "--- align:bottom,right" //
		// + "'Name:' align:top" //
		// + "[1] fill " //
		// + "--- " //
		// + "'Stuff:'" //
		// + "[2] size:100,200 " //
		// );

		// table.parse("debug * space:10" //
		// + "'moo' 'cow'" //
		// );

		table.parse("'asd' {'moo1' 'moo2'}");

		// table.parse("padding:10 " //
		// + "[1] size:150,250 fill" //
		// + "[2] size:100,200 expand:50" //
		// + "{ * expand" //
		// + "	[3] size:50,200" //
		// + "	---" //
		// + "	[4] size:40,100" //
		// + "} size:70,300" //
		// );

		// // BOZO - Shit broke?
		// table.parse("width:640 height:480" //
		// + "---" //
		// + "'logo'" //
		// + "{ width:200 height:200" //
		// // + "* fill" //
		// + "| align:right | align:left" //
		// + "'Name:' size:40" //
		// + "'nameEdit' size:200 colspan:2" //
		// + "---" //
		// + "'File' size:40" //
		// + "'fileEdit' size:40 expand:x" //
		// + "'browseButton' size:40" //
		// + "} width:200 height:200 fill" //
		// );

		// table.parse("padding:10" //
		// + "---" //
		// + "[1] size:40 align:right,top" //
		// + "[2] size:80 align:right" //
		// + "---" //
		// + "[3] size:20 align:left" //
		// + "[4] fill" //
		// );

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(800, 600);
		frame.setLocationRelativeTo(null);
		JPanel panel = new JPanel(table);
		frame.setContentPane(panel);
		frame.setVisible(true);
	}
}
