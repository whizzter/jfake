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

import java.util.Arrays;
import java.util.List;
import se.jlim.jfake.JFake;
import se.jlim.jfake.Token;

/**
 *
 * @author Jonas Lund
 */
public class ListExpression extends CompoundExpression implements Generator {
    
	final List<Integer> initPercentages;
	double[] percentages = null;

	public ListExpression(Token t, List<Expression> subs, List<Integer> percentages) {
		super(t, subs.toArray(new Expression[subs.size()]));
		this.initPercentages = percentages;
		//
		//			int sum=0;
		//			int empty=0;
		//			for (Integer i:percentages) {
		//				if (i==null)
		//					// TODO: we need to handle subsets..
		//				else
		//					sum+=i;
		//			}
		//			double fairleft=(sum<100 && empty>0)?(100.0-sum)/empty:0;
		//			// NEED SUBSETS this.percentages=new double[percentages.size()];
		//			double dsum=0;
		//			for (int i=0;i<percentages.size();i++) {
		//				this.percentages[i]=dsum;
		//				dsum+=percentages.get(i)==null?fairleft:percentages.get(i)/100.0;
		//			}
	} //
	//			int sum=0;
	//			int empty=0;
	//			for (Integer i:percentages) {
	//				if (i==null)
	//					// TODO: we need to handle subsets..
	//				else
	//					sum+=i;
	//			}
	//			double fairleft=(sum<100 && empty>0)?(100.0-sum)/empty:0;
	//			// NEED SUBSETS this.percentages=new double[percentages.size()];
	//			double dsum=0;
	//			for (int i=0;i<percentages.size();i++) {
	//				this.percentages[i]=dsum;
	//				dsum+=percentages.get(i)==null?fairleft:percentages.get(i)/100.0;
	//			}

	@Override
	public Generator compileExpr() {
		return this;
	}

	@Override
	public String toString() {
		return "[" + Arrays.stream(subs).map((t) -> t.toString()).reduce((a, b) -> a + "," + b).get() + "]";
	}

	@Override
	public Integer size() {
		int sum = 0;
		for (Generator g : compiled) {
			Integer sz = g.size();
			if (sz != null) {
				sum += sz;
			} else {
				sum++; // in list contexts treat infinite generators as one
			}
		}
		return sum;
	}

	@Override
	public Object get(int idx, long seed) {
		int sum = 0;
		for (Generator g : compiled) {
			Integer sz = g.size();
			int loc = sz == null ? 1 : sz;
			if (sum <= idx && idx < sum + loc) {
				return g.get(idx - sum, seed);
			}
			sum += loc;
		}
		throw new IllegalStateException("Out of bounds!!");
	}
    
}
