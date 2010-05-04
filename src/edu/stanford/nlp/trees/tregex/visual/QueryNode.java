package edu.stanford.nlp.trees.tregex.visual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QueryNode {
	public String label;
	public String pattern;
	
	// groups
	public HashMap<Integer, String> groupLabels;
	
	List<QueryEdge> incomingEdges;
	List<QueryEdge> outgoingEdges;
	
	public QueryNode() {
	  this( ".*" );
	}
	
	public QueryNode( String pattern ) {
	  incomingEdges = new ArrayList<QueryEdge>();
	  outgoingEdges = new ArrayList<QueryEdge>();
	  this.pattern = pattern;
	  this.label = "";
	  this.groupLabels = new HashMap<Integer, String>();
	}
}
