package edu.stanford.nlp.trees.tregex.visual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import sun.misc.Queue;

public class QueryGraph {
	
	List<QueryNode> nodes;
	List<Edge> edges;

	QueryNode headNode;
	
	public QueryGraph() {
	  this.nodes = new ArrayList<QueryNode>();
	  this.edges = new ArrayList<Edge>();		
	}
	
	/**
	 * convert to a tregex query
	 * breadth-first search, starting with the head node
	 */
	public String toTregexQuery() {
		Map<QueryNode, String> labels = assignLabelsToNodes( );
				
		return visitNode( headNode, null, new HashSet<QueryNode>(), new HashSet<Edge>(),
				labels);
	}
	
	public String visitNode( QueryNode currentNode, QueryNode previousNode,
			Set<QueryNode> visitedNodes, Set<Edge> visitedEdges,
			Map<QueryNode, String> labels ) {
		
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
			query += " " + e.render( false ) + " (" + visitNode( e.n2, currentNode, 
					visitedNodes, visitedEdges, labels ) + ")";
			
		}
		for ( Edge e: currentNode.incomingEdges ) {
			// skip any relations already rendered
			if ( visitedEdges.contains(e) )
				continue;
			
			visitedEdges.add(e);
			
			// render the edge
			query += " " + e.render( true ) + " (" + visitNode( e.n1, currentNode, 
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
	
	private Map<QueryNode, String> assignLabelsToNodes() {
		Map<QueryNode, String> labelMap = new HashMap<QueryNode, String>();
		for (QueryNode node: nodes) {
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

	public QueryNode createNode(  ) {
		QueryNode newNode = new QueryNode();
		nodes.add( newNode );
		return newNode;
	}
	
	public Edge createEdge(QueryNode n1, QueryNode n2) {
	  Edge e = new Edge();
	  addEdge( e, n1, n2 );
	  return e;
	}
	
	public void addEdge(Edge e, QueryNode n1, QueryNode n2) {
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
	
	public static void main( String [] args ) {
	  QueryGraph graph = new QueryGraph();
	  QueryNode n1 = graph.createNode();
	  QueryNode n2 = graph.createNode();
	  QueryNode n3 = graph.createNode();
	  
	  Edge e1 = new Edge( EdgeDescriptor.Type.DESCENDANT);
	  graph.addEdge( e1, n1, n2 );
	  Edge e2 = new Edge( EdgeDescriptor.Type.SIBLING);
	  graph.addEdge( e2, n2, n3 );
	  graph.headNode = n1;
	  
	  System.out.println( graph.toTregexQuery() );
	}
}
