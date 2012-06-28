
package com.esotericsoftware.tablelayout;

abstract public class Value {
	BaseTableLayout layout;
	Cell cell;

	abstract public float get ();

	public Cell getCell () {
		return cell;
	}

	public BaseTableLayout getLayout () {
		return layout;
	}
}
