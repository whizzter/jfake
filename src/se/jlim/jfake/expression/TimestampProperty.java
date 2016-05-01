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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.regex.Pattern;

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
	public Long size() {
		return src.size();
	}

	@Override
	public Object get(long idx, long seed) {
		Object base=src.get(idx,seed);
		if (base==null) {
			return null;
		} else if (base instanceof String) {
			int nc=0;
			int part=0;
			int[] ymdhms=new int[]{0,1,1,0,0,0};
			String s=(String)base;
			int si=0;
			while(true) {
				if (si>=s.length()) {
					if (part==0 && nc==0)
						throw new IllegalArgumentException("Could not interpret "+base+" as a date");
					break;
				}
				char c=s.charAt(si);
				if ('0'<=c && c<='9') {
					ymdhms[part]=(nc==0?0:ymdhms[part]*10) + (c-'0');
					nc++;
					si++;
					continue;
				} else if (nc==0) {
					throw new IllegalArgumentException("Could not interpret "+base+" as a date");
				} else if ((part<2 && c=='-')||(part==2 && c==' ')||(part>2 && c==':')) {
					part++;
					nc=0;
					si++;
					if (part==6)
						throw new IllegalArgumentException("Could not interpret "+base+" as a date");
					continue;
				} else {
					throw new IllegalArgumentException("Could not interpret "+base+" as a date");
				}
			}
			LocalDateTime ldt = LocalDateTime.of(ymdhms[0], ymdhms[1], ymdhms[2], ymdhms[3], ymdhms[4],ymdhms[5]);
			System.out.println(base+" parsed as "+ldt+" ("+(Timestamp.valueOf(ldt).getTime())+")");
			return Timestamp.valueOf(ldt);
		} else if (base instanceof Number) {
			
			System.out.println(base+"->"+new Timestamp(((Number)base).longValue()));
			return new Timestamp(((Number)base).longValue());
		} else throw new RuntimeException("Do not know how to convert "+base+" to a timestamp");
	}
}
