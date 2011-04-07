
package com.esotericsoftware.tablelayout;

public class ParseException extends RuntimeException {
	public int line, column;

	public ParseException (String message, Throwable cause) {
		super(message, cause);
	}
}
