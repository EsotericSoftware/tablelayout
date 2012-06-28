/*******************************************************************************
 * Copyright (c) 2011, Nathan Sweet <nathan.sweet@gmail.com>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package com.esotericsoftware.tablelayout;

/** @author Nathan Swet */
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
