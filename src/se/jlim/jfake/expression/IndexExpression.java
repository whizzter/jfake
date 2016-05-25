/*
 * The MIT License
 *
 * Copyright 2016 Jonas Lund.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package se.jlim.jfake.expression;

import se.jlim.jfake.Token;

/**
 *
 * @author Jonas Lund
 */
public class IndexExpression extends CompoundExpression implements Generator {

	public IndexExpression(Token tok, Expression... subs) {
		super(tok, subs);
	}

	@Override
	public Generator compileExpr() {
		return this;
	}

	@Override
	public Long size() {
		return compiled[1].size();
	}

	@Override
	public Object get(long idx, long seed) {
		Object idxValue=compiled[1].get(idx, seed);
		if (idxValue instanceof Long)  {
			return compiled[0].get((Long)idxValue, seed);
		}
		throw new RuntimeException("Index value "+idxValue+" not usable as an index at "+tok.toString());
	}

	@Override
	public String toString() {
		return subs[0]+"["+subs[1]+"]";
	}
}
