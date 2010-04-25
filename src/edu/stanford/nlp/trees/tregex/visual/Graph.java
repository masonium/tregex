package edu.stanford.nlp.trees.tregex.visual;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sun.misc.Queue;

public class Graph {
	
	List<Node> nodes;
	List<Edge> edges;

	Node headNode;
	
	public Graph() {
		
	}
	
	/**
	 * convert to a tregex query
	 * breadth-first search, starting with the head node
	 */
	public String toTregexQuery() {
		Map<Node, String> labels = assignLabelsToNodes( );
				
		return visitNode( headNode, null, new HashSet<Node>(), new HashSet<Edge>(),
				labels);
	}
	
	public String visitNode( Node currentNode, Node previousNode,
			Set<Node> visitedNodes, Set<Edge> visitedEdges,
			Map<Node, String> labels ) {
		
		String label = labels.get( currentNode );
		if ( visitedNodes.contains( currentNode ) )
			return "=" + label;
		
		visitedNodes.add( currentNode );
		String query = currentNode.pattern + "=" + label;
				
		for ( Edge e: currentNode.outgoingEdges ) {
			// skip any relations already rendered
			if ( visitedEdges.contains(e) )
				continue;
			
			visitedEdges.add(e);
			
			// render the edge
			query += " " + e.render( false ) + "(" + visitNode( e.n2, currentNode, 
					visitedNodes, visitedEdges, labels ) + ")";
			
		}
		for ( Edge e: currentNode.incomingEdges ) {
			// skip any relations already rendered
			if ( visitedEdges.contains(e) )
				continue;
			
			visitedEdges.add(e);
			
			// render the edge
			query += " " + e.render( false ) + "(" + visitNode( e.n1, currentNode, 
					visitedNodes, visitedEdges, labels ) + ")";
			
		}
		
		return query;
	}
	
	/**
	 * Compute the query string contribution from this edge
	 * @param edge
	 * @param currentNode
	 * @param reverse
	 * @param visitedNodes
	 * @param visitedEdges
	 * @param labels
	 * @return
	 */
	/*
	public String visitEdge( Edge edge, Node currentNode, boolean reverse,
			Set<Node> visitedNodes, Set<Edge> visitedEdges,
			Map<Node, String> labels ) {
		
	}*/
	
	private Map<Node, String> assignLabelsToNodes() {
		Map<Node, String> labelMap = new HashMap<Node, String>();
		for (Node node: nodes) {
			String label = new String(node.label);
			if (label.isEmpty())
				label = generateLabel();
			labelMap.put(node, label);
				
		}
		return labelMap;
	}

	private int labelCounter = 0;
	private String generateLabel() {
		return "___g" + (labelCounter++);
	}

	public Node createNode(  ) {
		Node newNode = new Node();
		nodes.add( newNode );
		return newNode;
	}
	
	public void addEdge(Edge e, Node n1, Node n2) {
		e.n1 = n1;
		e.n2 = n2;
		n1.outgoingEdges.add( e );
		n2.incomingEdges.add( e );
		edges.add( e );
	}
	
	public void removeEdge( Edge e ) {
		e.n1.outgoingEdges.remove( e );
		e.n2.incomingEdges.remove( e );
		edges.remove( e );
	}
}
