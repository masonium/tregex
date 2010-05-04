package edu.stanford.nlp.trees.tregex.visual;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import edu.stanford.nlp.trees.tregex.visual.gui.Node;

public class QueryNode extends Ownable<Node> {
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
	
	/**
	 * Returns an unmodifiable list of all edges
	 * 
	 * @return
	 */
	public List<QueryEdge> getEdges() {
	  List<QueryEdge> edges = new ArrayList<QueryEdge>();
	  edges.addAll( incomingEdges );
	  edges.addAll( outgoingEdges );
	  return Collections.unmodifiableList( edges );
	}
	
	public boolean hasEdgeTo( QueryNode toNode ) {
	  for (QueryEdge edge: outgoingEdges)
	    if (edge.n2 == toNode)
	      return true;
	  return false;
	}
	public boolean hasEdgeFrom( QueryNode fromNode ) {
    for (QueryEdge edge: incomingEdges)
      if (edge.n1 == fromNode)
        return true;
    return false;
  }
	
	public int getDegree() {
	  return incomingEdges.size() + outgoingEdges.size();
	}
}
