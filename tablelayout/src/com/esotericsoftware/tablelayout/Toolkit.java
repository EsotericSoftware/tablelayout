
package com.esotericsoftware.tablelayout;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import static com.esotericsoftware.tablelayout.TableLayout.*;

abstract public class Toolkit<T> {
	static final ArrayList<String> classPrefixes = new ArrayList();

	private final Class<T> widgetType;

	public Toolkit (Class<T> widgetType) {
		this.widgetType = widgetType;
	}

	/**
	 * Sets the title of widget. Eg, this may apply a titled border to the widget.
	 */
	abstract public void setTitle (T widget, String string);

	/**
	 * Adds a child to the specified parent.
	 * @param layoutString May be null.
	 */
	abstract public void addChild (T parent, T child, String layoutString);

	abstract public void removeChild (T parent, T child);

	/**
	 * Returns a placeholder widget to use for empty cells.
	 */
	abstract public T newEmptyWidget ();

	abstract public T newLabel (String text);

	/**
	 * Returns a new TableLayout.
	 */
	abstract public TableLayout newTableLayout ();

	abstract public int getMinWidth (T widget);

	abstract public int getMinHeight (T widget);

	abstract public int getPrefWidth (T widget);

	abstract public int getPrefHeight (T widget);

	abstract public int getMaxWidth (T widget);

	abstract public int getMaxHeight (T widget);

	/**
	 * Returns the value unless it is one of the special integers representing min, pref, or max height.
	 */
	int getWidth (T widget, int value) {
		switch (value) {
		case MIN:
			return getMinWidth(widget);
		case PREF:
			return getPrefWidth(widget);
		case MAX:
			return getMaxWidth(widget);
		}
		return value;
	}

	/**
	 * Returns the value unless it is one of the special integers representing min, pref, or max height.
	 */
	int getHeight (T widget, int value) {
		switch (value) {
		case MIN:
			return getMinHeight(widget);
		case PREF:
			return getPrefHeight(widget);
		case MAX:
			return getMaxHeight(widget);
		}
		return value;
	}

	/**
	 * Creates a new widget from the specified class name. This method can be overriden to create widgets using shortcut names,
	 * such as "button".
	 */
	public Object newWidget (String className) throws Exception {
		try {
			return (T)Class.forName(className).newInstance();
		} catch (Exception ex) {
			for (int i = 0, n = classPrefixes.size(); i < n; i++) {
				String prefix = classPrefixes.get(i);
				try {
					return (T)Class.forName(prefix + className).newInstance();
				} catch (Exception ignored) {
				}
			}
			throw ex;
		}
	}

	public T wrap (Object object) {
		if (widgetType.isAssignableFrom(object.getClass())) return (T)object;
		if (object instanceof TableLayout) return (T)((TableLayout)object).getTable();
		throw new RuntimeException("Unknown object type: " + object.getClass());
	}

	/**
	 * Sets a property on the widget. This is called for widget properties specified in the TableLayout description.
	 */
	public void setProperty (T object, String name, ArrayList<String> values) {
		try {
			invokeMethod(object, name, values);
		} catch (NoSuchMethodException ex1) {
			try {
				invokeMethod(object, "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1), values);
			} catch (NoSuchMethodException ex2) {
				try {
					Field field = object.getClass().getField(name);
					Object value = convertType(object, values.get(0), field.getType());
					if (value != null) field.set(object, value);
				} catch (Exception ex3) {
					throw new RuntimeException("No method, bean property, or field found.");
				}
			}
		}
	}

	/**
	 * Sets a property on the table. This is called for table properties specified in the TableLayout description.
	 */
	public void setTableProperty (TableLayout table, String name, ArrayList<String> values) {
		name = name.toLowerCase();
		for (int i = 0, n = values.size(); i < n; i++)
			values.set(i, values.get(i).toLowerCase());
		try {
			String value;
			if (name.equals("size")) {
				switch (values.size()) {
				case 1:
					table.width = table.height = getSize(values.get(0));
					break;
				case 2:
					table.width = getSize(values.get(0));
					table.height = getSize(values.get(1));
					break;
				}

			} else if (name.equals("width") || name.equals("w")) {
				table.width = getSize(values.get(0));

			} else if (name.equals("height") || name.equals("h")) {
				table.height = getSize(values.get(0));

			} else if (name.equals("fill")) {
				switch (values.size()) {
				case 0:
					table.fillWidth = table.fillHeight = 1f;
					break;
				case 1:
					value = values.get(0);
					if (value.equals("x"))
						table.fillWidth = 1f;
					else if (value.equals("y")) //
						table.fillHeight = 1f;
					else
						table.fillWidth = table.fillHeight = Integer.parseInt(value) / 100f;
					break;
				case 2:
					value = values.get(0);
					if (value.length() > 0) table.fillWidth = Integer.parseInt(value) / 100f;
					value = values.get(1);
					if (value.length() > 0) table.fillHeight = Integer.parseInt(value) / 100f;
					break;
				}

			} else if (name.equals("padding") || name.equals("pad")) {
				switch (values.size()) {
				case 4:
					value = values.get(3);
					if (value.length() > 0) table.padRight = getSize(value);
				case 3:
					value = values.get(2);
					if (value.length() > 0) table.padBottom = getSize(value);
				case 2:
					value = values.get(0);
					if (value.length() > 0) table.padTop = getSize(value);
					value = values.get(1);
					if (value.length() > 0) table.padLeft = getSize(value);
					break;
				case 1:
					table.padTop = table.padLeft = table.padBottom = table.padRight = getSize(values.get(0));
					break;
				default:
					throw new IllegalArgumentException("Invalid number of values (" + values.size() + "): " + values);
				}

			} else if (name.startsWith("padding") || name.startsWith("pad")) {
				name = name.replace("padding", "").replace("pad", "");
				if (name.equals("top") || name.equals("t"))
					table.padTop = getSize(values.get(0));
				else if (name.equals("left") || name.equals("l"))
					table.padLeft = getSize(values.get(0));
				else if (name.equals("bottom") || name.equals("b"))
					table.padBottom = getSize(values.get(0));
				else if (name.equals("right") || name.equals("r"))
					table.padRight = getSize(values.get(0));
				else
					throw new IllegalArgumentException("Unknown property.");

			} else if (name.equals("align")) {
				table.align = 0;
				for (int i = 0, n = values.size(); i < n; i++) {
					value = values.get(i);
					if (value.equals("center"))
						table.align |= CENTER;
					else if (value.equals("left"))
						table.align |= LEFT;
					else if (value.equals("right"))
						table.align |= RIGHT;
					else if (value.equals("top"))
						table.align |= TOP;
					else if (value.equals("bottom"))
						table.align |= BOTTOM;
					else
						throw new IllegalArgumentException("Invalid value: " + value);
				}

			} else if (name.equals("debug")) {
				table.debug = "";
				if (values.size() == 0) table.debug = "all,";
				for (int i = 0, n = values.size(); i < n; i++)
					table.debug += values.get(i) + ",";
				if (table.debug.equals("true,")) table.debug = "all,";

			} else
				throw new IllegalArgumentException("Unknown property: " + name);
		} catch (Exception ex) {
			throw new IllegalArgumentException("Error setting property: " + name, ex);
		}
	}

	/**
	 * Sets a property on the cell. This is called for cell properties specified in the TableLayout description.
	 */
	public void setCellProperty (Cell c, String name, ArrayList<String> values) {
		name = name.toLowerCase();
		for (int i = 0, n = values.size(); i < n; i++)
			values.set(i, values.get(i).toLowerCase());
		try {
			String value;
			if (name.equals("expand")) {
				switch (values.size()) {
				case 0:
					c.expandWidth = c.expandHeight = 1;
					break;
				case 1:
					value = values.get(0);
					if (value.equals("x"))
						c.expandWidth = 1;
					else if (value.equals("y")) //
						c.expandHeight = 1;
					else
						c.expandWidth = c.expandHeight = Integer.parseInt(value);
					break;
				case 2:
					value = values.get(0);
					if (value.length() > 0) c.expandWidth = Integer.parseInt(value);
					value = values.get(1);
					if (value.length() > 0) c.expandHeight = Integer.parseInt(value);
					break;
				}

			} else if (name.equals("fill")) {
				switch (values.size()) {
				case 0:
					c.fillWidth = c.fillHeight = 1f;
					break;
				case 1:
					value = values.get(0);
					if (value.equals("x"))
						c.fillWidth = 1f;
					else if (value.equals("y")) //
						c.fillHeight = 1f;
					else
						c.fillWidth = c.fillHeight = Integer.parseInt(value) / 100f;
					break;
				case 2:
					value = values.get(0);
					if (value.length() > 0) c.fillWidth = Integer.parseInt(value) / 100f;
					value = values.get(1);
					if (value.length() > 0) c.fillHeight = Integer.parseInt(value) / 100f;
					break;
				}

			} else if (name.equals("size")) {
				switch (values.size()) {
				case 2:
					value = values.get(0);
					if (value.length() > 0) c.minWidth = c.prefWidth = getSize(value);
					value = values.get(1);
					if (value.length() > 0) c.minHeight = c.prefHeight = getSize(value);
					break;
				case 1:
					value = values.get(0);
					if (value.length() > 0) c.minWidth = c.minHeight = c.prefWidth = c.prefHeight = getSize(value);
					break;
				default:
					throw new IllegalArgumentException("Invalid number of values (" + values.size() + "): " + values);
				}

			} else if (name.equals("width") || name.equals("w")) {
				switch (values.size()) {
				case 3:
					value = values.get(0);
					if (value.length() > 0) c.maxWidth = getSize(value);
				case 2:
					value = values.get(1);
					if (value.length() > 0) c.prefWidth = getSize(value);
				case 1:
					value = values.get(0);
					if (value.length() > 0) c.minWidth = getSize(value);
					break;
				default:
					throw new IllegalArgumentException("Invalid number of values (" + values.size() + "): " + values);
				}

			} else if (name.equals("height") || name.equals("h")) {
				switch (values.size()) {
				case 3:
					value = values.get(0);
					if (value.length() > 0) c.maxHeight = getSize(value);
				case 2:
					value = values.get(1);
					if (value.length() > 0) c.prefHeight = getSize(value);
				case 1:
					value = values.get(0);
					if (value.length() > 0) c.minHeight = getSize(value);
					break;
				default:
					throw new IllegalArgumentException("Invalid number of values (" + values.size() + "): " + values);
				}

			} else if (name.equals("spacing") || name.equals("space")) {
				switch (values.size()) {
				case 4:
					value = values.get(3);
					if (value.length() > 0) c.spaceRight = getSize(value);
				case 3:
					value = values.get(2);
					if (value.length() > 0) c.spaceBottom = getSize(value);
				case 2:
					value = values.get(0);
					if (value.length() > 0) c.spaceTop = getSize(value);
					value = values.get(1);
					if (value.length() > 0) c.spaceLeft = getSize(value);
					break;
				case 1:
					c.spaceTop = c.spaceLeft = c.spaceBottom = c.spaceRight = getSize(values.get(0));
					break;
				default:
					throw new IllegalArgumentException("Invalid number of values (" + values.size() + "): " + values);
				}

			} else if (name.equals("padding") || name.equals("pad")) {
				switch (values.size()) {
				case 4:
					value = values.get(3);
					if (value.length() > 0) c.padRight = getSize(value);
				case 3:
					value = values.get(2);
					if (value.length() > 0) c.padBottom = getSize(value);
				case 2:
					value = values.get(0);
					if (value.length() > 0) c.padTop = getSize(value);
					value = values.get(1);
					if (value.length() > 0) c.padLeft = getSize(value);
					break;
				case 1:
					c.padTop = c.padLeft = c.padBottom = c.padRight = getSize(values.get(0));
					break;
				default:
					throw new IllegalArgumentException("Invalid number of values (" + values.size() + "): " + values);
				}

			} else if (name.startsWith("padding") || name.startsWith("pad")) {
				name = name.replace("padding", "").replace("pad", "");
				if (name.equals("top") || name.equals("t"))
					c.padTop = getSize(values.get(0));
				else if (name.equals("left") || name.equals("l"))
					c.padLeft = getSize(values.get(0));
				else if (name.equals("bottom") || name.equals("b"))
					c.padBottom = getSize(values.get(0));
				else if (name.equals("right") || name.equals("r")) //
					c.padRight = getSize(values.get(0));
				else
					throw new IllegalArgumentException("Unknown property.");

			} else if (name.startsWith("spacing") || name.startsWith("space")) {
				name = name.replace("spacing", "").replace("space", "");
				if (name.equals("top") || name.equals("t"))
					c.spaceTop = getSize(values.get(0));
				else if (name.equals("left") || name.equals("l"))
					c.spaceLeft = getSize(values.get(0));
				else if (name.equals("bottom") || name.equals("b"))
					c.spaceBottom = getSize(values.get(0));
				else if (name.equals("right") || name.equals("r")) //
					c.spaceRight = getSize(values.get(0));
				else
					throw new IllegalArgumentException("Unknown property.");

			} else if (name.equals("align")) {
				c.align = 0;
				for (int i = 0, n = values.size(); i < n; i++) {
					value = values.get(i);
					if (value.equals("center"))
						c.align |= CENTER;
					else if (value.equals("left"))
						c.align |= LEFT;
					else if (value.equals("right"))
						c.align |= RIGHT;
					else if (value.equals("top"))
						c.align |= TOP;
					else if (value.equals("bottom"))
						c.align |= BOTTOM;
					else
						throw new IllegalArgumentException("Invalid value: " + value);
				}

			} else if (name.equals("ignore")) {
				c.ignore = values.size() == 0 ? true : Boolean.valueOf(values.get(0));

			} else if (name.equals("colspan")) {
				c.colspan = Integer.parseInt(values.get(0));

			} else if (name.equals("uniform")) {
				if (values.size() == 0) c.uniformWidth = c.uniformHeight = true;
				for (int i = 0, n = values.size(); i < n; i++) {
					value = values.get(i);
					if (value.equals("x"))
						c.uniformWidth = true;
					else if (value.equals("y"))
						c.uniformHeight = true;
					else if (value.equals("false"))
						c.uniformHeight = c.uniformHeight = null;
					else
						throw new IllegalArgumentException("Invalid value: " + value);
				}

			} else
				throw new IllegalArgumentException("Unknown property.");
		} catch (Exception ex) {
			throw new IllegalArgumentException("Error setting property: " + name, ex);
		}
	}

	/**
	 * Parses the specified pixel value to an int. This hook can be used to scale all sizes applied to a cell.
	 */
	protected int getSize (String value) {
		return Integer.parseInt(value);
	}

	static private void invokeMethod (Object object, String name, ArrayList<String> values) throws NoSuchMethodException {
		Object[] params = values.toArray();
		// Prefer methods with string parameters.
		Class[] stringParamTypes = new Class[params.length];
		Method method = null;
		try {
			for (int i = 0, n = params.length; i < n; i++)
				stringParamTypes[i] = String.class;
			method = object.getClass().getMethod(name, stringParamTypes);
		} catch (NoSuchMethodException ignored) {
			try {
				for (int i = 0, n = params.length; i < n; i++)
					stringParamTypes[i] = CharSequence.class;
				method = object.getClass().getMethod(name, stringParamTypes);
			} catch (NoSuchMethodException ignored2) {
			}
		}
		if (method != null) {
			try {
				method.invoke(object, params);
			} catch (Exception ex) {
				throw new RuntimeException("Error invoking method: " + name, ex);
			}
			return;
		}
		// Try to convert the strings to match a method.
		Method[] methods = object.getClass().getMethods();
		outer:
		for (int i = 0, n = methods.length; i < n; i++) {
			method = methods[i];
			if (!method.getName().equalsIgnoreCase(name)) continue;
			params = values.toArray();
			Class[] paramTypes = method.getParameterTypes();
			for (int ii = 0, nn = paramTypes.length; ii < nn; ii++) {
				Object value = convertType(object, (String)params[ii], paramTypes[ii]);
				if (value == null) continue outer;
				params[ii] = value;
			}
			try {
				method.invoke(object, params);
				return;
			} catch (Exception ex) {
				throw new RuntimeException("Error invoking method: " + name, ex);
			}
		}
		throw new NoSuchMethodException();
	}

	static private Object convertType (Object parentObject, String value, Class paramType) {
		if (paramType == String.class || paramType == CharSequence.class) return value;
		try {
			if (paramType == int.class || paramType == Integer.class) return Integer.valueOf(value);
			if (paramType == float.class || paramType == Float.class) return Float.valueOf(value);
			if (paramType == long.class || paramType == Long.class) return Long.valueOf(value);
			if (paramType == double.class || paramType == Double.class) return Double.valueOf(value);
		} catch (NumberFormatException ignored) {
		}
		if (paramType == boolean.class || paramType == Boolean.class) return Boolean.valueOf(value);
		if (paramType == char.class || paramType == Character.class) return value.charAt(0);
		if (paramType == short.class || paramType == Short.class) return Short.valueOf(value);
		if (paramType == byte.class || paramType == Byte.class) return Byte.valueOf(value);
		// Look for a static field.
		try {
			Field field = getField(paramType, value);
			if (field != null && paramType == field.getType()) return field.get(null);
		} catch (Exception ignored) {
		}
		try {
			Field field = getField(parentObject.getClass(), value);
			if (field != null && paramType == field.getType()) return field.get(null);
		} catch (Exception ignored) {
		}
		return null;
	}

	static private Field getField (Class type, String name) {
		try {
			Field field = type.getField(name);
			if (field != null) return field;
		} catch (Exception ignored) {
		}
		while (type != null && type != Object.class) {
			Field[] fields = type.getDeclaredFields();
			for (int i = 0, n = fields.length; i < n; i++)
				if (fields[i].getName().equalsIgnoreCase(name)) return fields[i];
			type = type.getSuperclass();
		}
		return null;
	}

	static public void addClassPrefix (String prefix) {
		classPrefixes.add(prefix);
	}
}
