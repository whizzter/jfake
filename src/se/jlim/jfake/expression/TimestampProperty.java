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

import java.sql.Timestamp;

/**
 *
 * @author Jonas Lund
 */
public class TimestampProperty implements Generator {

	private final Generator src;

	public TimestampProperty(Generator src) {
		this.src=src;
	}

	@Override
	public Integer size() {
		return src.size();
	}

	@Override
	public Object get(int idx, long seed) {
		Object base=src.get(idx,seed);
		if (base==null) {
			return null;
		} else if (base instanceof String) {
			return Timestamp.valueOf((String)base);
		} else if (base instanceof Number) {
			return new Timestamp(((Number)base).longValue());
		} else throw new RuntimeException("Do not know how to convert "+base+" to a timestamp");
	}
}
