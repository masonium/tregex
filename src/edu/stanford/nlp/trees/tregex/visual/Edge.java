package edu.stanford.nlp.trees.tregex.visual;

public class Edge {

	public QueryNode getNode1() { return n1; }
	public QueryNode getNode2() { return n2; }
	public EdgeDescriptor getDescriptor() { return descriptor; }
	
	QueryNode n1;
	QueryNode n2;

	EdgeDescriptor descriptor;

	public Edge( ) {
	  this( EdgeDescriptor.Type.DESCENDANT );
	}
	
	public Edge( EdgeDescriptor.Type type ) {
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
}
