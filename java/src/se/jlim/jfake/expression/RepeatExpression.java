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
import se.jlim.jfake.Token;

/**
 *
 * @author Jonas Lund
 */
public class RepeatExpression extends CompoundExpression implements Generator {
    
	public RepeatExpression(Token op, Expression base, Expression repeat) {
		super(op, base, repeat);
	}

	@Override
	public String toString() {
		return subs[0] + "{" + subs[1] + "}";
	}

	@Override
	public Generator compileExpr() {
		return this;
	}
	Long stored = null;

	@Override
	public Long size() {
		Object out = compiled[1].get(0, 0);
		if (!(out instanceof Long) || (stored != null && ((Long) out) != (long) stored)) {
			throw new RuntimeException(out + " is not an integer or has varied between invocations");
		}
		stored = (Long)out;
		return stored;
	}

	@Override
	public Object get(long idx, long seed) {
		return compiled[0].get(idx, JFake.tumble(seed + idx));
	}
    
}
