
package com.esotericsoftware.tablelayout;

public class Values {
	static public class FixedValue extends Value {
		private float value;

		public FixedValue (float value) {
			this.value = value;
		}

		public void set (float value) {
			this.value = value;
		}

		public float get () {
			return value;
		}
	}

	static public Value minWidth () {
		return new Value() {
			public float get () {
				if (cell == null) throw new RuntimeException("minWidth can only be set on a cell property.");
				Object widget = cell.getWidget();
				if (widget == null) return 0;
				return layout.getToolkit().getMinWidth(widget);
			}
		};
	}

	static public Value minHeight () {
		return new Value() {
			public float get () {
				if (cell == null) throw new RuntimeException("minHeight can only be set on a cell property.");
				Object widget = cell.getWidget();
				if (widget == null) return 0;
				return layout.getToolkit().getMinHeight(widget);
			}
		};
	}

	static public Value prefWidth () {
		return new Value() {
			public float get () {
				if (cell == null) throw new RuntimeException("prefWidth can only be set on a cell property.");
				Object widget = cell.getWidget();
				if (widget == null) return 0;
				return layout.getToolkit().getPrefWidth(widget);
			}
		};
	}

	static public Value prefHeight () {
		return new Value() {
			public float get () {
				if (cell == null) throw new RuntimeException("prefHeight can only be set on a cell property.");
				Object widget = cell.getWidget();
				if (widget == null) return 0;
				return layout.getToolkit().getPrefHeight(widget);
			}
		};
	}

	static public Value maxWidth () {
		return new Value() {
			public float get () {
				if (cell == null) throw new RuntimeException("maxWidth can only be set on a cell property.");
				Object widget = cell.getWidget();
				if (widget == null) return 0;
				return layout.getToolkit().getMaxWidth(widget);
			}
		};
	}

	static public Value maxHeight () {
		return new Value() {
			public float get () {
				if (cell == null) throw new RuntimeException("maxHeight can only be set on a cell property.");
				Object widget = cell.getWidget();
				if (widget == null) return 0;
				return layout.getToolkit().getMaxHeight(widget);
			}
		};
	}

	static public Value percentWidth (final float percent) {
		return new Value() {
			public float get () {
				return layout.getLayoutWidth() * percent;
			}
		};
	}

	static public Value percentHeight (final float percent) {
		return new Value() {
			public float get () {
				return layout.getLayoutHeight() * percent;
			}
		};
	}

	static public Value percentWidth (final float percent, final Object widget) {
		return new Value() {
			public float get () {
				return layout.getToolkit().getWidth(widget) * percent;
			}
		};
	}

	static public Value percentHeight (final float percent, final Object widget) {
		return new Value() {
			public float get () {
				return layout.getToolkit().getHeight(widget) * percent;
			}
		};
	}

	private Values () {
	}
}
