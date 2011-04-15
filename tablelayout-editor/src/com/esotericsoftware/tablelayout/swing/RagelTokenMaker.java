
package com.esotericsoftware.tablelayout.swing;

import java.util.HashSet;

import javax.swing.text.Segment;

import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMakerBase;

abstract public class RagelTokenMaker extends TokenMakerBase {
	static private final boolean debug = false;

	protected char[] data;
	protected int cs, p, pe, eof, ts, te, act, s = -1, newStartOffset;
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
			if (s != -1)
				addToken(data, s, pe - 1, TableLayoutTokenizer.PLAIN, newStartOffset + s);
			else
				addToken(data, p, pe - 1, TableLayoutTokenizer.PLAIN, newStartOffset + p);
		}

		addNullToken();
		return firstToken;
	}

	protected void buffer () {
		s = p;
	}

	public void addToken (int tokenType) {
		if (debug) System.out.println(tokenType + " \"" + new String(data, s, p - s) + "\"");
		addToken(data, s, p - 1, tokenType, newStartOffset + s);
		s = -1;
	}

	public void addCharToken (int tokenType) {
		if (debug) System.out.println(tokenType + " '" + data[p] + "'");
		addToken(data, p, p, tokenType, newStartOffset + p);
		s = -1;
	}

	public void addToken (HashSet<String> keywords, int keyword, int other) {
		addToken(keyword);
		if (!keywords.contains(currentToken.getLexeme())) currentToken.type = other;
		s = -1;
	}

	abstract protected void parse (int initialTokenType);
}
