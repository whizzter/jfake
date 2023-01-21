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

import se.jlim.jfake.JFake;

/**
 *
 * @author Jonas Lund
 */
public class ConcatProperty implements Generator {
    
	final Generator sub;

	public ConcatProperty(Generator sub) {
		this.sub = sub;
	}

	@Override
	public Long size() {
		return null;
	}

	@Override
	public Object get(long idx, long seed) {
		seed = JFake.tumble(idx + seed);
		Long ss = null;
		if (sub instanceof RepeatExpression) {
			RepeatExpression rp = (RepeatExpression) sub;
			if (rp.compiled[1] instanceof RangeExpression) {
				RangeExpression rag = (RangeExpression) rp.compiled[1];
				Object a = rag.compiled[0].get(0, 0);
				Object b = rag.compiled[1].get(0, 0);
				if (a instanceof Long && b instanceof Long) {
					long min = (Long) a;
					long max = Math.max((Long) b, min);
					ss = min + (int) ((seed & 0x7fffffffffffffffl) % (max - min + 1));
				}
			}
		}
		if (ss == null) {
			ss = sub.size();
		}
		if (ss == null) {
			return sub.get(0, seed);
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ss; i++) {
			sb.append(sub.get(i, seed));
		}
		return sb.toString();
	}
    
}
