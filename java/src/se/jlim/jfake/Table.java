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
package se.jlim.jfake;

import se.jlim.jfake.Column;
import java.util.Map;
import java.util.TreeMap;
import se.jlim.jfake.expression.Expression;
import se.jlim.jfake.expression.Generator;
import se.jlim.jfake.expression.PropertyHolder;
import se.jlim.jfake.expression.RowExpression;

/**
 *
 * @author Jonas Lund
 */
public class Table implements Expression,PropertyHolder {
    
	String name;
	Map<String, Column> columns = new TreeMap<>();
	Map<String, Expression> properties = new TreeMap<>();
	Long size = null;
	RowExpression rowExpression; 

	public Table(String name) {
		this.name = name;
	}

	@Override
	public Generator compile() {
		throw new UnsupportedOperationException("Tables(" + name + ") cannot be compiled");
	}

	@Override
	public Expression getProperty(String key) {
		Column c=columns.get(key);
		if (c==null) {
			throw new RuntimeException("Table "+name+" has no column named "+key);
		}
		return c;
	}
    
	
}
