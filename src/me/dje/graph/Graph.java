package me.dje.graph;

import java.util.HashMap;

/*
 * 
 */
public class Graph<E,V> {
	private HashMap<E, Edge<E, V>> nodes;
	
	public Graph() {
		nodes = new HashMap<E, Edge<E, V>>();
	}
	
	public boolean connected() {
		return true;
	}
	
	class Vertex {
		public Vertex() {
			
		}
	}
}
