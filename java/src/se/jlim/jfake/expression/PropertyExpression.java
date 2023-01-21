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
public class PropertyExpression extends CompoundExpression {

	Token id;
	
	public PropertyExpression(Token tok,Token id, Expression... subs) {
		super(tok, subs);
		this.id=id;
	}

	@Override
	public String toString() {
		return subs[0] + "." + id.getString();
	}

	public Expression getSource() {
		return subs[0];
	}
	
	
	
	/*
	@Override
	public Generator compile() {
		if (subs[0] instanceof PropertyHolder) {
			Expression gen = ((PropertyHolder) subs[0]).getProperty(id.str);
			if (gen == null) {
				throw new RuntimeException(subs[0] + " has no property named " + id.str);
			}
			return gen.compile();
		}
		return super.compile();
	}*/

	@Override
	public Generator compileExpr() {
		switch (id.getString()) {
			case "random":
				return new RandomProperty(compiled[0]);
			case "concat":
				return new ConcatProperty(compiled[0]);
			case "binary":
				return new BinaryProperty(compiled[0]);
			case "pbkdf2":
				return new PBKDF2Property(compiled[0]);
			case "timestamp" :
				return new TimestampProperty(compiled[0]);
			default:
				throw new UnsupportedOperationException("Do not know how to compile property " + id.getString());
		}
	}

	public String getPropertyName() {
		return id.getString();
	}

}
