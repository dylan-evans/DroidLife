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

/**
 * Parses and represents a map file which describes a set of structures used to 
 * initialise a grid. 
 * @author dylan
 *
 */
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
	
	/**
	 * Open and parse a map file.
	 * @param context A Context instance for opening the data file
	 * @param fileName The name of the file to open
	 * @throws Exception
	 */
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
	
	/**
	 * Get a Map instance representing the points in a given map.
	 * @param name The name of a map.
	 * @return a Map instance.
	 */
	public Map getPoints(String name) {
		if(!patterns.containsKey(name))
			return null;
		return patterns.get(name);
	}
	
	/**
	 * A single coordinate.
	 */
	class Point {
		public final int x, y;
		Point(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
	
	/**
	 * The Map class represents a set of points.
	 * @author dylan
	 *
	 */
	class Map implements Iterable<Point> {
		private Vector<Point> points;
		
		/**
		 * Create a Map instance.
		 */
		Map() {
			points = new Vector<Point>();
		}
		
		/**
		 * Get an iterator over the points of the map.
		 */
		public Iterator<Point> iterator() {
			return points.iterator();
		}
		
		/**
		 * Add a point to a map.
		 * @param x Horizontal coordinate
		 * @param y Vertical coordinate
		 */
		public void add(int x, int y) {
			points.add(new Point(x, y));
		}
		
		/**
		 * Add the points of a map at a specified offset.
		 * @param mName The name of the map to be included
		 * @param x The horizontal offset
		 * @param y The vertical offset
		 */
		public void add(String mName, int x, int y) {
			//System.out.println("map: " + mName);
			Map m = patterns.get(mName);
			add(m, x, y);
		}
		
		/**
		 * Add the points of a given map at a specified offset.
		 * @param m The map containing the set of points
		 * @param x The horizontal offset
		 * @param y The vertical offset
		 */
		public void add(Map m, int x, int y) {
			for(Point p : m) {
				add(p.x + x, p.y + y);
			}
		}
	}
	
	/**
	 * This exception represents a syntax error.
	 * @author dylan
	 *
	 */
	class UnexpectedTokenException extends Exception {
		
	}
	
	/**
	 * The map file parser.
	 *
	 */
	class Parser {
		/**
		 * @param mapText
		 * @throws UnexpectedTokenException
		 */
		Parser(String mapText) throws UnexpectedTokenException {
			Tokenizer t = new Tokenizer(mapText);
			
			// This is the body of the parser, a map begins with an ID
			while(t.expect(Token.ID) != null) {
				String name = t.text();
				t.require(Token.LBRAC); // Followed by a left bracket
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
	
	/**
	 * A token scanner which uses a stack to verify grammar rules.
	 * 
	 *
	 */
	class Tokenizer implements Iterator<Token> {
		private Matcher match;
		private Token currentToken;
		private String currentText;
		private int tokenIndex = 0;
		private LinkedList<Token> tokenStack;
		private LinkedList<String> stringStack;
		private Vector<Token> ignoreTokens;
		
		/**
		 * Initialise the scanner with a string.
		 * @param text The string to scan
		 */
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
		 * one is ready.
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
		
		/**
		 * Check whether more tokens are available. 
		 * @return True if there are more tokens, otherwise false
		 */
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

		/**
		 * Get the next available token. This method removes the token from the
		 * stack if available.
		 */
		@Override
		public Token next() {
			if(!hasNext())
				return Token.EOF;
			Token cur = tokenStack.removeFirst();
			currentText = stringStack.removeFirst();
			return cur;
		}
		
		/**
		 * Get the text of the token read by the last call to next.
		 * @return The text of the token
		 */
		public String text() {
			return currentText;
		}
		
		/**
		 * Non functioning method required to implement Iterator.
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			
		}
		
		/**
		 * Require any one of the Tokens in the argument list. An exception is
		 * thrown if the no matching token is found.
		 * @param tokens A list of Tokens
		 * @return False only at the end of the file.
		 * @throws UnexpectedTokenException If the specified token isn't found
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
		 * Expect the specified sequence of tokens in the given order. If the 
		 * tokens do not match then the stack is reset and an exception is 
		 * thrown.
		 * @param seq An ordered list of tokens
		 * @return An array of strings containing the text of each token
		 * @throws UnexpectedTokenException
		 */
		public String[] sequence(Token ... seq) 
				throws UnexpectedTokenException {
			String items[] = new String[seq.length];
			int i = 0;
			for(Token t : seq) {
				Token nt = readStack();
				//System.out.println("R: " + t + " T: " + nt);
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
