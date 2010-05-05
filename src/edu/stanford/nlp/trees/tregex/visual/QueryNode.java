package edu.stanford.nlp.trees.tregex.visual;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import edu.stanford.nlp.trees.tregex.visual.gui.Node;

public class QueryNode extends Ownable<Node> {
	public String label;
	private String pattern;
	
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

  public String getPatternWithGroups() {
    // clear out any empty patterns
    List<Integer> keys = new ArrayList<Integer>();
    keys.addAll( groupLabels.keySet() );
    
    for (Integer key: keys)
      if (groupLabels.get( key ).trim().isEmpty())
        groupLabels.remove( key );
    
    // if there are any patterns, adjoin them
    if (groupLabels.size() == 0)
      return pattern;
    
    StringBuilder sb = new StringBuilder();
    sb.append( "/" );
    sb.append( pattern );
    sb.append( "/" );
    for (Entry<Integer, String> entry: groupLabels.entrySet()) {
      sb.append( "#" );
      sb.append( entry.getKey() );
      sb.append( "%" );
      sb.append( entry.getValue() );
    }
    return sb.toString();
  }

  public String getPattern() {
    return pattern;
  }

  public void setPattern(String pattern) {
    this.pattern = pattern;
  }
}
