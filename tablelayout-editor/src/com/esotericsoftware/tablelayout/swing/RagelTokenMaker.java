
package com.esotericsoftware.tablelayout.swing;

import java.util.HashSet;

import javax.swing.text.Segment;

import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMakerBase;

abstract public class RagelTokenMaker extends TokenMakerBase {
	protected char[] data;
	protected int cs, p, pe, eof, ts, te, act, s, newStartOffset;
	protected RuntimeException parseRuntimeEx;

	public Token getTokenList (Segment text, int initialTokenType, int startOffset) {
		resetTokenList();
		data = text.array;
		p = text.offset;
		pe = eof = p + text.count;
		newStartOffset = startOffset - p;

		try {
			parse(initialTokenType);
		} catch (RuntimeException ex) {
			parseRuntimeEx = ex;
		}

		if (p < pe) {
			// BOZO - Don't throw, add the remaining characters as plain text.
			int lineNumber = 1;
			for (int i = 0; i < p; i++)
				if (data[i] == '\n') lineNumber++;
			throw new IllegalArgumentException("Error parsing on line " + lineNumber + " near: " + new String(data, p, pe - p),
				parseRuntimeEx);
		}

		addNullToken();
		return firstToken;
	}

	protected void buffer () {
		s = p;
	}

	public void addToken (int tokenType) {
		addToken(data, s, p - 1, tokenType, newStartOffset + s);
	}

	public void addCharToken (int tokenType) {
		addToken(data, p, p, tokenType, newStartOffset + p);
	}

	public void addToken (HashSet<String> keywords, int keyword, int other) {
		addToken(keyword);
		if (!keywords.contains(currentToken.getLexeme())) currentToken.type = other;
	}

	abstract protected void parse (int initialTokenType);
}
