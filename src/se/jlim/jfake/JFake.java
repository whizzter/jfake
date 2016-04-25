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

import se.jlim.jfake.expression.RandomProperty;
import se.jlim.jfake.expression.Expression;
import se.jlim.jfake.expression.ListExpression;
import se.jlim.jfake.expression.CompoundExpression;
import se.jlim.jfake.expression.RepeatExpression;
import se.jlim.jfake.expression.ConcatProperty;
import se.jlim.jfake.expression.Generator;
import se.jlim.jfake.expression.RangeExpression;
import se.jlim.jfake.expression.ConstantExpression;
import se.jlim.jfake.target.JFakeTarget;
import se.jlim.jfake.target.StreamTarget;
import se.jlim.jfake.target.JDBCTarget;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import se.jlim.jfake.expression.PropertyExpression;
import se.jlim.jfake.expression.PropertyHolder;
import se.jlim.jfake.expression.RowExpression;

/**
 * The main JFake class, works as a standalone application to be run independantly
 * but also contains most of the functionality to tie together the system.
 * 
 * @author Jonas Lund
 */
public class JFake {

	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Usage: xfake xmlfile");
			return;
		}
		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("-")) {

			} else {
				try (FileInputStream fis = new FileInputStream(args[i])) {
					JFake xf = fakeFromStream(fis);
					String connStr=xf.getProp("@connectionstring",String.class);
					String outputStr=xf.getProp("@outputstream",String.class);
					if (outputStr!=null) {
						if (outputStr.isEmpty()) {
							xf.build(new StreamTarget(System.out));
						} else {
							try (FileOutputStream fos=new FileOutputStream(outputStr)) {
								xf.build(new StreamTarget(fos));
							}
						}
					}
					if (connStr != null) {
						try (JDBCTarget target = new JDBCTarget(connStr, xf.getProp("@username",String.class), xf.getProp("@password",String.class))) {
							xf.build(target);
						} catch (SQLException sqex) {
							sqex.printStackTrace();
							return;
						}
					}
					if (outputStr==null && connStr==null) {
						System.out.println("JFake error, no output stream or connection string specified");
						return;
					}
				} catch (IOException ioex) {
					ioex.printStackTrace();
					return;
				}
			}
		}
	}

	static public JFake fakeFromStream(InputStream is) throws IOException {
		try {
			String sb = streamToString(is);
			LinkedList<Token> tokens = tokenize(sb);
			JFake jf = new JFake();
			jf.parseLevel(tokens, jf.properties, null);
			Integer startId=constProp(jf.properties,"@startid",Integer.class);
			if (startId!=null)
				jf.nextId=startId;
			jf.named.put("@autoid", new CompoundExpression(new Token(null, -1, null)){
				//(se.jlim.jfake.JFake.Token tok, se.jlim.jfake.Expression... subs) {
				//}
				@Override
				public Generator compileExpr() {
					return new Generator() {
						@Override
						public Integer size() {
							return null;
						}

						@Override
						public Object get(int idx, long seed) {
							return jf.nextId++;
						}
					};
				}
				
			});
			// Resolve expressions
			List<Column> notDone = new ArrayList<>();
			for (Table tab : jf.tables) {
				jf.named.put("@row", tab.rowExpression=new RowExpression(tab));
				for (Column col : tab.columns.values()) {
					notDone.add(col);
					System.out.println(tab.name+"."+col.name+"="+col.expression);
					col.expression = jf.resolve(col.expression);
				}
			}
			// Find build order
			while (true) {
				int preCount = notDone.size();
				for (int i = 0; i < notDone.size();) {
					Column col = notDone.get(i);
					
					//System.out.print("Compiling "+col.parent.name+"."+col.name);
					Generator out=col.expression.compile();
					if (out!=null && out.size()!=null) {
						if (col.parent.size==null) {
							col.parent.size=out.size();
						} else if ((int)col.parent.size!=(int)out.size()) {
							throw new RuntimeException("Mismatching size between table "+col.parent.name+" and column "+col.name);
						}
					}
					if (out!=null && col.parent.size!=null) {
						col.compiled=out;
						//System.out.println(".. done");
						col.data=new Object[col.parent.size];
						if (out.size()!=null) {
							if (col.parent.size==null)
								col.parent.size=out.size();
							else if (((int)col.parent.size)!=((int)out.size()))
								throw new IOException("Error, "+col.parent.name+"."+col.name+" has mismatching size compared to another column in the table");
						}
						jf.outOrder.add(notDone.remove(i));
					} else {
						//System.out.println(".. not done");
						i++;
					}
				}
				if (notDone.isEmpty()) {
					break;
				}
				if (preCount == notDone.size()) {
					String colInfo = notDone.stream()
							.map((c) -> "[" + c.parent.name + "." + c.name + "]")
							.reduce((a, b) -> a + "," + b)
							.orElse("<no columns>");
					throw new IOException("XFake : There is a circular dependency between the columns " + colInfo);
				}
			}
			return jf;
		} catch (ParseException ex) {
			throw new IOException("Problem parsing value in jfake.txt " + ex.getMessage(), ex);
		}
	}

	private static <T> T constProp(Map<String, Expression> properties, String id, Class<T> ty) {
		if (!properties.containsKey(id))
			return null;
		Expression pv = properties.get(id);
		return constExpr(pv,ty);
	}
	private static <T> T constExpr(Object pv,Class<T> ty) {
		if (ty.isInstance(pv))
			return (T)pv;
		if (!(pv instanceof ConstantExpression))
			return null;
		ConstantExpression cpv = (ConstantExpression)pv;
		Object value=cpv.get(0, 0);
		if (! ty.isInstance(value) )
			return null;
		return (T)value;
	}

	int nextId=1;
	//Random r=new Random(0);
	
	
	
	// The regexp used by the tokenizer
	final static Pattern tokenizePattern = Pattern.compile("(\\s+)|(#[^\n\r]*)|([{}+:\\[\\]=%,.()])|('(?:''|[^'])*'|\"(?:\"\"|[^\"])*\")|([0-9]+)|([@a-zA-Z_][a-zA-Z0-9_]*)");
	// this type list must match the above regexp
	final static Token.Kind[] tokenizePatternTokenTypes = new Token.Kind[]{null, null, null, Token.Kind.OP, Token.Kind.STR, Token.Kind.NUM, Token.Kind.ID};

	private static LinkedList<Token> tokenize(String sb) throws IOException {
		Matcher m = tokenizePattern.matcher(sb);
		LinkedList<Token> tokens = new LinkedList<>();
		int line = 1;
		while (m.regionStart() != m.regionEnd()) {
			if (!m.lookingAt()) {
				throw new IOException("Do not know how to parse " + sb.substring(m.regionStart(), Math.min(m.regionStart() + 20, sb.length() - 1)));
			}
			String token = m.group();
			for (int i = 1; i < tokenizePatternTokenTypes.length; i++) {
				if (m.start(i) == -1) {
					continue;
				}
				if (tokenizePatternTokenTypes[i] == null) {
					break; // comment or whitespace
				} else {
					tokens.add(new Token(tokenizePatternTokenTypes[i], line, token)); // some real token
				}
			}
			line += lines(token); // update the linecounter (cr/lf checking)
			m.region(m.end(), m.regionEnd()); // update the matching region
		}
		return tokens;
	}

	private static String streamToString(InputStream is) throws IOException, UnsupportedEncodingException {
		int c;
		StringBuilder sb = new StringBuilder();
		Reader r = new InputStreamReader(is, "UTF-8");
		while (-1 != (c = r.read())) {
			sb.append((char) c);
		}
		return sb.toString();
	}

	static int lines(String s) {
		int last = -1;
		int count = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == 13 || c == 10) {
				if (last != c && (last == 13 || last == 10)) {
					c = 0; // crlf or lfcr combo found, reset c so last becomes 0 so we can detect a new combo
				} else {
					count++; // add to linecount for the first newline character
				}
			}
			last = c;
		}
		return count;
	}

	public void build(JFakeTarget target) {
		// Dump via JDBC/JPA..
		Set<String> tableNames = tables.stream().map(t -> t.name).collect(Collectors.toSet());
		String neTable = target.findNonEmpty(tableNames);
		if (neTable != null) {
			System.out.println("JFake found that " + neTable + " was not empty in the database");
			return;
		}
		// Setup data
		long seed=14623412;
		for (Column c:outOrder) {
			//System.out.println("** doing "+c.parent.name+"."+c.name+" with "+c.data.length+" items");
			for (int i=0;i<c.data.length;i++) {
				c.parent.rowExpression.setRow(i);
				c.data[i]=c.compiled.get(i, seed);
				seed=tumble(seed^i +i);
				//System.out.println("["+c.data[i].toString().length()+"::"+c.data[i]+"]");
			}
		}
		target.begin();
		for (Table tab:tables) {
			String[] cnames=new String[tab.columns.size()];
			Object[][] cdata=new Object[tab.columns.size()][];
			int idx=0;
			for (Column col:tab.columns.values()) {
				cnames[idx]=col.name;
				cdata[idx]=col.data;
				idx++;
			}
			target.pushTable(tab.name, cnames, cdata);
		}
		target.commit();
	}
	
	private Expression resolve(Expression prod) throws ParseException {
		if (!(prod instanceof CompoundExpression)) {
			return prod;
		}
		CompoundExpression expr = (CompoundExpression) prod;
		if (expr.tok.kind == Token.Kind.ID) {
			String id = expr.tok.str;
			if (!named.containsKey(id)) {
				throw new ParseException("Table or symbol " + id + " was not found", expr.tok);
			}
			return named.get(id);
		}
		
		if (expr.subs.length!=0) {
			for (int i = 0; i < expr.subs.length; i++) {
				expr.subs[i] = resolve(expr.subs[i]);
			}
		}

		if (prod instanceof PropertyExpression) {
			PropertyExpression pex=(PropertyExpression)prod;
			if (pex.getSource() instanceof PropertyHolder) {
				Expression replacement=((PropertyHolder)pex.getSource()).getProperty(pex.getPropertyName());
				if (replacement!=null)
					return replacement;
			}
		}

		return prod;
	}

	public <T> T getProp(String key,Class<T> ty) {
		return constProp(properties, key, ty);
	}
	

	private Map<String, Expression> properties = new TreeMap<>();
	private Map<String, Expression> named = new TreeMap<>();
	private List<Table> tables = new ArrayList<>();
	List<Column> outOrder = new ArrayList<>();

	private void parseLevel(LinkedList<Token> toks, Map<String, Expression> subject, Table tab) throws ParseException {
		while (true) {
			if (toks.isEmpty()) {
				if (tab != null) {
					throw new ParseException("End of file when parsing table [" + tab.name + "] that was not terminated");
				} else {
					return;
				}
			}
			Token id = toks.removeFirst();
			if (tab != null && id.str.equals("}")) {
				return;
			}
			if (id.kind != Token.Kind.ID) {
				throw new ParseException("Expected table or value identifier but got ", id);
			}
			if (toks.isEmpty()) {
				throw new ParseException("Unexpected end of file after", id);
			}
			Token op = toks.removeFirst();
			if (toks.isEmpty()) {
				throw new ParseException("Unexpected end of file after", id);
			}
			if (op.str.equals("=")) {
				Expression prod = parseExpression(toks);
				if (id.str.startsWith("@")) {
					subject.put(id.str, prod);
				} else {
					if (tab == null) {
						throw new ParseException("Column expressions only allowed within tables", id);
					}
					if (tab.columns.containsKey(id.str)) {
						throw new ParseException("Duplicate column name", id);
					}
					tab.columns.put(id.str, new Column(tab,id.str, prod));
				}
			} else if (op.str.equals("{")) {
				if (tab == null) {
					Table t = new Table(id.str);
					parseLevel(toks, t.properties, t);
					tables.add(t);
					if (named.containsKey(id.str)) {
						throw new ParseException("Duplicate table name", id);
					}
					named.put(id.str, t);
					named.put("table_" + id.str, t);
				} else {
					throw new ParseException("Tables only allowed within toplevel", id);
				}
			} else {
				throw new ParseException("Unexpected token", op);
			}
		}
	}

	private Expression parseExpression(LinkedList<Token> toks) throws ParseException {
		return parseSuffixExpression(toks, 0);
	}

	final static String[][] ops = new String[][]{{"+"}, {"{", "."}, {":"}};

	private Expression parseSuffixExpression(LinkedList<Token> toks, int opLevel) throws ParseException {
		if (opLevel == ops.length) {
			return parsePrimary(toks);
		}
		Expression expr = parseSuffixExpression(toks, opLevel + 1);
		again: while (true) {
			if (toks.isEmpty()) {
				return expr;
			}
			Token op = toks.peekFirst();
			for (String top : ops[opLevel]) {
				if (op.str.equals(top)) {
					toks.removeFirst();
					if (toks.isEmpty()) {
						throw new ParseException("operator " + op.str + " is missing the second part of the expression", op);
					}
					switch (op.str) {
						case "+": {
							Expression sec = parseSuffixExpression(toks, opLevel + 1);
							expr=new CompoundExpression(op, expr,sec) {
								public String toString() {
									return subs[0]+"+"+subs[1];
								}
								public Generator compileExpr() {
									return new Generator() {
										@Override
										public Integer size() {
											Integer sz=null;
											for (Generator sub:compiled) {
												Integer t=sub.size();
												if (t!=null)
													if (sz==null)
														sz=t;
													else if ((int)sz!=(int)t)
														throw new RuntimeException(subs[0]+" specifies a different size than "+subs[1]+" -> "+sz+"!="+t);
											}
											return sz;
										}
										@Override
										public Object get(int idx, long seed) {
											Object a=compiled[0].get(idx, seed);
											Object b=compiled[1].get(idx, seed);
											if (a instanceof String || b instanceof String) {
												return (a==null?"<NULL>":a.toString())+(b==null?"<NULL>":b.toString());
											} else if (a instanceof Integer && b instanceof Integer) {
												return ((Integer)a)+((Integer)b);
											} else throw new RuntimeException("Do not know how to add "+a+" and "+b);
										}
									};
								}
							};
							continue again;
						}
						case ":": {
							Expression sec = parseSuffixExpression(toks, opLevel + 1);
							expr=new CompoundExpression(op, expr,sec) {
								@Override
								public String toString() {
									return subs[0]+":"+subs[1];
								}
								@Override
								public Generator compileExpr() {
									return new RangeExpression(subs,compiled);
								}
							};
							continue again;
						}
						case ".": {
							Token id = toks.removeFirst();
							if (id.kind != Token.Kind.ID) {
								throw new ParseException("expected identifier after . but got ", id);
							}
							expr=new PropertyExpression(op,id,expr);
							continue again;
						}
						case "{": {
							Expression repeat = parseExpression(toks);
							if (toks.isEmpty()) {
								throw new ParseException("operator " + op.str + " is missing the second part of the expression", op);
							}
							Token end = toks.removeFirst();
							if (!end.str.equals("}")) {
								throw new ParseException("Expected end } but found", end);
							}
							expr=new RepeatExpression(op,expr,repeat);
							continue again;
						}
						default:
							throw new IllegalStateException(op.str);
					}
				}
			}
			return expr;
		}
	}

	private Expression parsePrimary(LinkedList<Token> toks) throws ParseException {
		if (toks.isEmpty()) {
			throw new ParseException("Unexpected end of file");
		}
		Token t = toks.peekFirst();
		// token types
		switch (t.kind) {
			case ID:
				toks.removeFirst();
				return new CompoundExpression(t) {
					//(se.jlim.jfake.JFake.Token tok, se.jlim.jfake.Expression... subs) {
					//}
					@Override
					public Generator compileExpr() {
						throw new IllegalStateException("Should not happen");
					}
				};
			case NUM: {
				toks.removeFirst();
				return new ConstantExpression(t,Integer.parseInt( t.str ));
			}
			case STR:
				toks.removeFirst();
				String old=t.str.charAt(0)=='\"'?"\"\"":"''";
				String nu=t.str.charAt(0)=='\"'?"\"":"'";
				return new ConstantExpression(t, t.str.substring(1, t.str.length()-1).replace(old,nu) );
		}
		// ops
		switch (t.str) {
			case "(": {
				toks.removeFirst();
				Expression sub=parseExpression(toks);
				if (toks.isEmpty()) {
					throw new ParseException("lacking end paranthesis", t);
				}
				Token end = toks.removeFirst();
				if (!end.str.equals(")")) {
					throw new ParseException("expected end parenthesis but got something else ", end);
				}
				return sub;
			}
			case "[": {
				toks.removeFirst();
				List<Expression> subs=new ArrayList<>();
				List<Integer> percentages=new ArrayList<>();
				while (true) {
					subs.add(parseExpression(toks));
					if (toks.isEmpty()) {
						throw new ParseException("List was not terminated properly", t);
					}
					if (toks.peekFirst().str.equals("%")) {
						Token percentSign = toks.removeFirst();
						if (toks.isEmpty())
							throw new ParseException("List was not terminated properly after percentage", percentSign);
						Token percentage = toks.removeFirst();
						if (percentage.kind!=Token.Kind.NUM)
							throw new ParseException("Percentage was not a number but "+percentage.str, percentage);
						percentages.add(Integer.parseInt(percentage.str));
						if (toks.isEmpty()) {
							throw new ParseException("List was not terminated properly after percentage", percentSign);
						}
					} else {
						percentages.add(null);
					}
					Token com = toks.removeFirst();
					if (com.str.equals(",")) {
						continue;
					} else if (com.str.equals("]")) {
						break;
					} else {
						throw new ParseException("Unexpected token inside list", com);
					}
				}
				return new ListExpression(t,subs,percentages);
			}
		}
		throw new ParseException("Unexpected input when parsing primary expression", t);
	}


	

	
	static public long tumble(long iv) {
		return iv^Long.rotateLeft(iv, 4)^Long.rotateLeft(iv, 16)^Long.rotateLeft(iv*135934795324331l, 50);
	}
}
