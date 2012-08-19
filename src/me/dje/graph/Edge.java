package me.dje.graph;

import java.util.HashMap;

import android.graphics.Point;

public class Edge<E, V> {
	private E edge;
	private HashMap<V, Edge<E, V>> vertices;
	private HashMap<String, String> meta;
	public Edge(E edge) {
		this.edge = edge;
		this.vertices = new HashMap<V, Edge<E, V>>();
		this.meta = new HashMap<String, String>();
	}
	
	public void addVertex(V vertex, Edge<E, V> dest) {
		this.vertices.put(vertex, dest);
	}
	
	public void test() {
		Edge<Point, int[]> e = new Edge<Point, int[]>(new Point(10, 10));
	}
}
