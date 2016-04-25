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
package se.jlim.jfake.target;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Set;

/**
 *
 * @author Jonas Lund
 */
public class StreamTarget implements JFakeTarget {

	private final OutputStream out;

	public StreamTarget(OutputStream out) {
		this.out=out;
	}

	@Override
	public void begin() {}

	@Override
	public void commit() {}

	@Override
	public String findNonEmpty(Set<String> tables) {
		return null;
	}

	@Override
	public void pushTable(String table, String[] columns, Object[][] data) {
		System.out.println("*** Table:"+table+" ***");
		System.out.println( Arrays.stream(columns).map(cn->"'"+cn+"'").reduce((a,b)->a+","+b).orElse("") );
		for (int i=0;i<data[0].length;i++) {
			final int idx=i;
			System.out.println( Arrays.stream(data).map(d->"'"+prettify(d[idx])+"'").reduce((a,b)->a+","+b).orElse("") );
		}
	}
	
	static private Object prettify(Object o) {
		if (o instanceof byte[]) {
			byte[] b=(byte[])o;
			StringBuilder sb=new StringBuilder("<BYTES:"+b.length+":");
			for (int i=0;i<b.length;i++) {
				if (i!=0)
					sb.append(',');
				sb.append(toHex((b[i]>>4)&0xf));
				sb.append(toHex(b[i]&0xf));
			}
			return sb.toString();
		}
		return o;
	}
	private static char toHex(int v) {
		if (0<=v && v<10)
			return (char)('0'+v);
		else if (10<=v && v<16)
			return (char)('a'+v-10);
		else throw new IllegalArgumentException();
	}
}
