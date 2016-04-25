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

/**
 *
 * @author Jonas Lund
 */
public class RangeExpression implements Generator {
    
	Expression[] subs;
	Generator[] compiled;
	Integer pa = null;
	Integer pb = null;

	public RangeExpression(Expression[] subs, Generator[] compiled) {
		this.subs = subs;
		this.compiled = compiled;
	}

	@Override
	public Integer size() {
		Object a = compiled[0].get(0, 0);
		Object b = compiled[1].get(0, 0);
		if (!(a instanceof Integer) || (pa != null && ((Integer) a) != (int) pa)) {
			throw new RuntimeException(subs[0] + " is not an integer or varies between invocations");
		}
		if (!(b instanceof Integer) || (pb != null && ((Integer) b) != (int) pb)) {
			throw new RuntimeException(subs[1] + " is not an integer or varies between invocations");
		}
		pa = (Integer) a;
		pb = (Integer) b;
		return (int) b - (int) a + 1;
	}

	@Override
	public Object get(int idx, long seed) {
		if (pa == null || pb == null) {
			size();
		}
		return idx + (int) pa;
	}
    
}
