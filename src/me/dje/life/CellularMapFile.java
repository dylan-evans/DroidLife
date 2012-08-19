package me.dje.life;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;

public class CellularMapFile {
	private static enum State { TOP, MAP, X, Y, };
	private static enum Token { EOF, SPACE, COMMENT, ID, NUM, LBRAC, RBRAC, 
		LPAR, RPAR, COMMA };
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
				"^(\\s+)"					// SPACE
				+ "|([#][^\n]*\n|$)"		// COMMENT
				+ "|([a-zA-Z_]\\w*)"		// ID
				+ "|(\\d+)"					// NUM
				+ "|(\\{)"					// LBRAC
				+ "|(\\})"					// RBRAC
				+ "|(\\()"					// LPAR
				+ "|(\\))"					// RPAR
				+ "|(,)"					// COMMA
				);
	}
	
	private HashMap<String, Map> patterns;
	
	public CellularMapFile(Context context, String fileName) 
			throws Exception {
		InputStream raw = context.getResources().openRawResource(R.raw.life);
		InputStreamReader isr = new InputStreamReader(raw);
		String fileText = new String();
		char[] buffer = new char[1024];
		while(isr.read(buffer, 0, 1024) > 0) {
			fileText = fileText.concat(new String(buffer));
		}
		//System.out.println(fileText);
		patterns = new HashMap<String, Map>();
		new Parser(fileText);
	}
	
	public Map getPoints(String name) {
		if(!patterns.containsKey(name))
			return null;
		return patterns.get(name);
	}
	
	/**
	 * A coordinate
	 */
	class Point {
		public final int x, y;
		Point(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
	
	/**
	 * Hold a set of points
	 * @author dylan
	 *
	 */
	class Map implements Iterable<Point> {
		private Vector<Point> points;
		Map() {
			points = new Vector<Point>();
		}
		
		public Iterator<Point> iterator() {
			return points.iterator();
		}
		
		public void add(int x, int y) {
			points.add(new Point(x, y));
		}
		
		public void add(String mName, int x, int y) {
			System.out.println("map: " + mName);
			Map m = patterns.get(mName);
			add(m, x, y);
		}
		
		public void add(Map m, int x, int y) {
			for(Point p : m) {
				add(p.x + x, p.y + y);
			}
		}
	}
	
	class UnexpectedTokenException extends Exception {
		
	}
	
	class SyntaxException extends Exception {
		
	}
	
	/**
	 * The Parser class 
	 * @author dylan
	 *
	 */
	class Parser {
		/**
		 * 
		 * @param mapText
		 * @throws UnexpectedTokenException
		 */
		Parser(String mapText) throws UnexpectedTokenException {
			Tokenizer t = new Tokenizer(mapText);
			
			while(t.expect(Token.ID) != null) {
				String name = t.text();
				t.require(Token.LBRAC);
				Map map = new Map();
				Token cur;
				while((cur = t.expect(Token.ID, Token.LPAR)) != null) {
					switch(cur) {
					case ID:
						String ref = t.text();
						System.out.println("Got id: " + ref);
						String[] items = t.sequence(Token.LPAR, Token.NUM, 
								Token.COMMA, Token.NUM, Token.RPAR);
						// Add the sub-map at the specified offsets
						map.add(ref, Integer.parseInt(items[1]), 
								Integer.parseInt(items[3]));
						break;
					case LPAR:
						String[] coord = t.sequence(Token.NUM, Token.COMMA, 
								Token.NUM, Token.RPAR);
						// Add the point
						map.add(Integer.parseInt(coord[0]), 
								Integer.parseInt(coord[2]));
						break;
					}
				}
				t.require(Token.RBRAC);
				
				patterns.put(name, map);
				for(String k : patterns.keySet()) {
					System.out.println("----  Key: " + k);
				}
			}
			t.require(Token.EOF);
		}
	}
	
	class Tokenizer implements Iterator<Token> {
		private Matcher match;
		private Token currentToken;
		private String currentText;
		private int tokenIndex = 0;
		private LinkedList<Token> tokenStack;
		private LinkedList<String> stringStack;
		private Vector<Token> ignoreTokens;
		
		Tokenizer(String text) {
			tokenIndex = 0;
			tokenStack = new LinkedList<Token>();
			stringStack = new LinkedList<String>();
			ignoreTokens = new Vector<Token>();
			match = pattern.matcher(text);
			currentToken = Token.EOF;
			ignoreTokens.add(Token.SPACE);
			ignoreTokens.add(Token.COMMENT);
		}
		
		/**
		 * Read the next available token. The Token is read from the stack if
		 * one is one is ready.
		 * @return The next Token.
		 */
		private Token readStack() {
			if(tokenIndex < tokenStack.size()) {
				int i = tokenIndex;
				tokenIndex++;
				return tokenStack.get(i);
			}
			
			while(match.find()) {
				for(Token token : Token.values()) {
					if(token == Token.EOF) continue;
					String str = match.group(token.ordinal());
					if( str != null) {
						if(ignoreTokens.contains(token))
							break;
						System.out.println("Token: " + token.name() + 
								" " + str);
						tokenStack.add(token);
						stringStack.add(str);
						tokenIndex++;
						return token;
					}
				}
			}
			return Token.EOF;
		}
		
		/**
		 * Reset the stack.
		 */
		private void resetStack() {
			tokenIndex = 0;
		}
		
		/**
		 * Clear the stack and reset the stack pointer.
		 */
		private void clearStack() {
			tokenStack.clear();
			stringStack.clear();
			tokenIndex = 0;
		}
		
		@Override
		public boolean hasNext() {
			if(tokenStack.size() == 0) {
				Token t = readStack();
				if(t == null || t == Token.EOF) {
					return false;
				}
			}
			return true;
		}

		@Override
		public Token next() {
			if(!hasNext())
				return Token.EOF;
			Token cur = tokenStack.removeFirst();
			currentText = stringStack.removeFirst();
			return cur;
		}
		
		public String text() {
			return currentText;
		}

		@Override
		public void remove() {
			
		}
		
		/**
		 * Require any one of the Tokens in the argument list. 
		 * @param tokens A list of Tokens
		 * @return False only at the end of the file.
		 * @throws UnexpectedTokenException
		 */
		public Token require(Token ... tokens) 
				throws UnexpectedTokenException {
			Token t = expect(tokens);
			if(t == null) {
				throw new UnexpectedTokenException();
			}
			return t;
		}
		
		/**
		 * Read any of the listed tokens. This method behaves like next but
		 * is selective about the type of Token which can be read, and if the 
		 * next Token does not match any of the parameters then the state of 
		 * the object is reset as if the function had not been called.
		 * 
		 * @param tokens List of valid Tokens
		 * @return The matching Token or null if none match
		 */
		public Token expect(Token ... tokens) {
			Token nt = readStack();
			for(Token t : tokens) {
				if(nt == t) {
					if(Token.EOF != nt)
						currentText = stringStack.get(tokenIndex - 1);
					else
						currentText = "";
					clearStack();
					return t;
				}
			}
			resetStack();
			return null;
		}
		
		/**
		 * 
		 * @param seq
		 * @return
		 * @throws UnexpectedTokenException
		 */
		public String[] sequence(Token ... seq) 
				throws UnexpectedTokenException {
			String items[] = new String[seq.length];
			int i = 0;
			for(Token t : seq) {
				Token nt = readStack();
				System.out.println("R: " + t + " T: " + nt);
				if(t == nt) {
					items[i] = stringStack.get(tokenIndex - 1);
				} else {
					resetStack();
					throw new UnexpectedTokenException();
				}
				i++;
			}
			clearStack();
			return items;
		}
	}
	
	
}
