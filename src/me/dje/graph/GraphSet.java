package me.dje.graph;

import java.util.Vector;

/**
 * This class is for constructing a set of connected graphs from arbitrary Edges.
 * @author dylan
 *
 */
public class GraphSet<V,E> {
	private Vector<Vertex<V,E>> allNodes;
	public GraphSet(GraphBuilder<V,E> gb) {
		allNodes = new Vector<Vertex<V,E>>();
		while(gb.hasNext()) {
			V data = gb.next();
			Vertex<V,E> v = new Vertex<V,E>(data);
			gb.build(v, allNodes.iterator());
		}
	}
}
