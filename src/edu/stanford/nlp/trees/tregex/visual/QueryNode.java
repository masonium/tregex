package edu.stanford.nlp.trees.tregex.visual;

import java.util.ArrayList;
import java.util.List;

public class QueryNode {
	String label;
	public String pattern;
	
	List<Edge> incomingEdges;
	List<Edge> outgoingEdges;
	
	public QueryNode() {
	  this( "/.*/" );
	}
	
	public QueryNode( String pattern ) {
	  incomingEdges = new ArrayList<Edge>();
	  outgoingEdges = new ArrayList<Edge>();
	  this.pattern = pattern;
	  this.label = "";
	}
	
}
