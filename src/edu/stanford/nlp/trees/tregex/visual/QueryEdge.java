package edu.stanford.nlp.trees.tregex.visual;

import edu.stanford.nlp.trees.tregex.visual.gui.Edge;

public class QueryEdge extends Ownable<Edge> {

	public QueryNode getNode1() { return n1; }
	public QueryNode getNode2() { return n2; }
	public EdgeDescriptor getDescriptor() { return descriptor; }
	
	// topological info
	QueryNode n1;
	QueryNode n2;

	// semantic info
	EdgeDescriptor descriptor;

	public QueryEdge( ) {
	  this( EdgeDescriptor.Type.DESCENDANT );
	}
	
	public QueryEdge( EdgeDescriptor.Type type ) {
	  descriptor = new EdgeDescriptor( );
	  descriptor.edgeType = type;
	}
	
	@Override
	public boolean equals( Object obj ) {
		return this == obj;
	}
	public String render(boolean reverse) {
		return descriptor.render(reverse);
	}
  public void cloneDescriptor(EdgeDescriptor descriptor2) {
    descriptor = (EdgeDescriptor)descriptor2.clone();  
  }
}
