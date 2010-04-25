package edu.stanford.nlp.trees.tregex.visual;

public class EdgeDescriptor {
	public enum Type {
		DESCENDANT,
		DIRECT_DESCENDANT,
		NTH_CHILD,
		NTH_TO_LAST_CHILD,
		PRECEDES,
		IMMEDIATELY_PRECEDES,
		ONLY_DESCENDANT,
		UNARY_DESCENDANT,
		SIBLING,
		//IMMEDIATE_SIBLING,
		EQUALS
		//DESCENDANT_VIA,
		//PRECEDES_VIA
	}
	
	Type edgeType;
	boolean optional;
	int N;
	Node viaNode;
	
	public String render(boolean reverse) {
		return simpleRender( reverse );
	}
	
	private String simpleRender(boolean reverse) {
		boolean normal = !reverse;
		
		switch (edgeType) {
		case DESCENDANT:
			return normal ? ">>" : "<<";
		case DIRECT_DESCENDANT:
			return normal ? ">" : "<";
		case NTH_CHILD:
			return (N == 1) ? 
					(normal ? ">," : "<,") :
					(normal ? ">"+N : "<"+N);
		case NTH_TO_LAST_CHILD:
			return (N == 1) ? 
					(normal ? ">-" : "<-") :
					(normal ? ">-" + N : "<-" + N);
		case PRECEDES:
			return normal ? ".." : ",,";
		case IMMEDIATELY_PRECEDES:
			return normal ? "." : ",";
		case ONLY_DESCENDANT:
			return normal ? ">:" : "<:";
		case UNARY_DESCENDANT:
			return normal ? ">>:" : "<<:";
		case SIBLING: 
			return "$";
		//case EQUALS:
		default:
			return "==";				
		}
	}
}
