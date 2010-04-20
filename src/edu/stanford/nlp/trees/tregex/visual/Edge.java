package edu.stanford.nlp.trees.tregex.visual;

public class Edge {

	public Node getNode1() { return n1; }
	public Node getNode2() { return n2; }
	public EdgeDescriptor getDescriptor() { return descriptor; }
	
	Node n1;
	Node n2;

	EdgeDescriptor descriptor;
	
	@Override
	public boolean equals( Object obj ) {
		return this == obj;
	}
	public String render(boolean reverse) {
		return descriptor.render(reverse);
	}
}
