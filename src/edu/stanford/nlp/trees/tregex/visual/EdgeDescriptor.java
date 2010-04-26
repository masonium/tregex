package edu.stanford.nlp.trees.tregex.visual;

public class EdgeDescriptor {
	public enum Type {
		DESCENDANT("Descendant of"),
		DIRECT_DESCENDANT("Direct Descendant of"),
		NTH_CHILD("Nth child of", true),
		NTH_TO_LAST_CHILD("Nth Child of", true),
		PRECEDES("Precedes"),
		IMMEDIATELY_PRECEDES("Immediately Precedes"),
		ONLY_DESCENDANT("Only descendant of"),
		UNARY_DESCENDANT("Descendant of (via unary tree)"),
		SIBLING("Sibling of"),
		//IMMEDIATE_SIBLING,
		EQUALS("Equal to");
		//DESCENDANT_VIA,
		//PRECEDES_VIA
		
		private String name;
		private boolean hasNumberArg;
		private Type(String name, boolean hasNumberArg) {
		  this.name = name;
		  this.hasNumberArg = hasNumberArg;
		}
		private Type(String name) { 
		  this(name, false);  
		}
		/**
		 * Return a list of all of the text descriptions of enums
		 * @return
		 */
		public static String[] descriptions() {
		  String [] descs = new String[ Type.values().length ];
		  for ( int i = 0; i < Type.values().length; ++i )
		    descs[i] = Type.values()[i].name;
		  return descs;
		}
		
    public String getName() {
      return name;
    }
    public boolean hasNumberArg() {
      return hasNumberArg;
    }
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

	
	
	
  public Type getEdgeType() {
    return edgeType;
  }

  public void setEdgeType(Type edgeType) {
    this.edgeType = edgeType;
  }

  public boolean isOptional() {
    return optional;
  }

  public void setOptional(boolean optional) {
    this.optional = optional;
  }

  public int getN() {
    return N;
  }

  public void setN(int n) {
    if (n > 0)
      N = n;
  }

  public Node getViaNode() {
    return viaNode;
  }

  public void setViaNode(Node viaNode) {
    this.viaNode = viaNode;
  }
}
