package edu.stanford.nlp.trees.tregex.visual;

public class QueryEdge {

	public QueryNode getNode1() { return n1; }
	public QueryNode getNode2() { return n2; }
	public EdgeDescriptor getDescriptor() { return descriptor; }
	
	QueryNode n1;
	QueryNode n2;

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
  public void flip() {
    QueryNode temp = n1;
    n1 = n2;
    n2 = temp;
  }
}
