package edu.stanford.nlp.trees.tregex.visual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QueryNode {
	public String label;
	public String pattern;
	
	// groups
	public HashMap<Integer, String> groupLabels;
	
	List<Edge> incomingEdges;
	List<Edge> outgoingEdges;
	
	public QueryNode() {
	  this( ".*" );
	}
	
	public QueryNode( String pattern ) {
	  incomingEdges = new ArrayList<Edge>();
	  outgoingEdges = new ArrayList<Edge>();
	  this.pattern = pattern;
	  this.label = "";
	  this.groupLabels = new HashMap<Integer, String>();
	}
}
