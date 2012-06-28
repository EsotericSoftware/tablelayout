
package com.esotericsoftware.tablelayout;

abstract public class Value {
	abstract public float get (Object table);

	abstract public float get (Cell cell);

	public float width (Object table) {
		return Toolkit.instance.width(get(table));
	}

	public float height (Object table) {
		return Toolkit.instance.height(get(table));
	}

	public float width (Cell cell) {
		return Toolkit.instance.width(get(cell));
	}

	public float height (Cell cell) {
		return Toolkit.instance.height(get(cell));
	}

	static abstract public class CellValue extends Value {
		public float get (Object table) {
			throw new UnsupportedOperationException("This value can only be used for a cell property.");
		}
	}

	static abstract public class TableValue extends Value {
		public float get (Cell cell) {
			return get(cell.getLayout().getTable());
		}
	}

	static public class FixedValue extends Value {
		private float value;

		public FixedValue (float value) {
			this.value = value;
		}

		public void set (float value) {
			this.value = value;
		}

		public float get (Object table) {
			return value;
		}

		public float get (Cell cell) {
			return value;
		}
	}

	static public Value minWidth () {
		return new CellValue() {
			public float get (Cell cell) {
				if (cell == null) throw new RuntimeException("minWidth can only be set on a cell property.");
				Object widget = cell.getWidget();
				if (widget == null) return 0;
				return Toolkit.instance.getMinWidth(widget);
			}
		};
	}

	static public Value minHeight () {
		return new CellValue() {
			public float get (Cell cell) {
				if (cell == null) throw new RuntimeException("minHeight can only be set on a cell property.");
				Object widget = cell.getWidget();
				if (widget == null) return 0;
				return Toolkit.instance.getMinHeight(widget);
			}
		};
	}

	static public Value prefWidth () {
		return new CellValue() {
			public float get (Cell cell) {
				if (cell == null) throw new RuntimeException("prefWidth can only be set on a cell property.");
				Object widget = cell.getWidget();
				if (widget == null) return 0;
				return Toolkit.instance.getPrefWidth(widget);
			}
		};
	}

	static public Value prefHeight () {
		return new CellValue() {
			public float get (Cell cell) {
				if (cell == null) throw new RuntimeException("prefHeight can only be set on a cell property.");
				Object widget = cell.getWidget();
				if (widget == null) return 0;
				return Toolkit.instance.getPrefHeight(widget);
			}
		};
	}

	static public Value maxWidth () {
		return new CellValue() {
			public float get (Cell cell) {
				if (cell == null) throw new RuntimeException("maxWidth can only be set on a cell property.");
				Object widget = cell.getWidget();
				if (widget == null) return 0;
				return Toolkit.instance.getMaxWidth(widget);
			}
		};
	}

	static public Value maxHeight () {
		return new CellValue() {
			public float get (Cell cell) {
				if (cell == null) throw new RuntimeException("maxHeight can only be set on a cell property.");
				Object widget = cell.getWidget();
				if (widget == null) return 0;
				return Toolkit.instance.getMaxHeight(widget);
			}
		};
	}

	static public Value percentWidth (final float percent) {
		return new TableValue() {
			public float get (Object table) {
				return Toolkit.instance.getWidth(table) * percent;
			}
		};
	}

	static public Value percentHeight (final float percent) {
		return new TableValue() {
			public float get (Object table) {
				return Toolkit.instance.getHeight(table) * percent;
			}
		};
	}

	static public Value percentWidth (final float percent, final Object widget) {
		return new Value() {
			public float get (Cell cell) {
				return Toolkit.instance.getWidth(widget) * percent;
			}

			public float get (Object table) {
				return Toolkit.instance.getWidth(widget) * percent;
			}
		};
	}

	static public Value percentHeight (final float percent, final Object widget) {
		return new TableValue() {
			public float get (Object table) {
				return Toolkit.instance.getHeight(widget) * percent;
			}
		};
	}
}
