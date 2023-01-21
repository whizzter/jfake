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

import se.jlim.jfake.Table;

/**
 *
 * @author Jonas Lund
 */
public class RowExpression implements Expression,PropertyHolder {
	Table table;
	int row;

	public RowExpression(Table table) {
		this.table = table;
	}
	
	public void setRow(int row) {
		this.row = row;
	}

	@Override
	public Generator compile() {
		throw new UnsupportedOperationException("Cannot compile a @row expression directly but must instead use properties");
	}
	
	@Override
	public Expression getProperty(String key) {
		Expression prop = table.getProperty(key);
		if (prop==null)
			return null;
		return new CompoundExpression(null,prop) {
			@Override
			public Generator compileExpr() {
				Generator gen=prop.compile();
				if (gen==null)
					return null;
				return new Generator() {
					@Override
					public Long size() {
						return null;
					}

					@Override
					public Object get(long idx, long seed) {
						return gen.get(row, seed);
					}
				};
			}
		};
	}

}
