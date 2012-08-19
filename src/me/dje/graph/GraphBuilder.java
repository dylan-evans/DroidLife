package me.dje.graph;

import java.util.Iterator;

abstract public class GraphBuilder<V,E> implements Iterator<V> {

	/**
	 * 
	 * @param v The Vertex to be built
	 * @param set The set of previous vertices
	 */
	abstract public void build(Vertex<V,E> v, Iterator<Vertex<V,E>> set);
	

	public boolean hasNext() {
		return false;
	}

	public V next() {
		return null;
	}

	public void remove() {			
	}
}
