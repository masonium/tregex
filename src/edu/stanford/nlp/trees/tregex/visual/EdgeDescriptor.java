package edu.stanford.nlp.trees.tregex.visual;

public class EdgeDescriptor {	
	Type edgeType;
	boolean optional;
	boolean negative;
	int N;
	String via;
	
	public EdgeDescriptor() {
	  this.edgeType = Type.DESCENDANT;
	  this.optional = false;
	  this.negative = false;
	  this.N = 1;
	  this.via = "";
	}
	
	public String render(boolean reverse) {
	  String result = simpleRender( reverse );
	  result = (negative ? "!" : "") + (optional ? "?" : "") + result;
		return result;
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

  public String getViaPattern() {
    return via;
  }

  public void setViaPattern(String viaPattern) {
    this.via = viaPattern;
  }
  
  public enum Type {
    DESCENDANT("Descendant of"),
    DIRECT_DESCENDANT("Direct Descendant of"),
    NTH_CHILD("Nth child of"),
    NTH_TO_LAST_CHILD("Nth-to-last child of"),
    PRECEDES("Precedes"),
    IMMEDIATELY_PRECEDES("Immediately Precedes"),
    ONLY_DESCENDANT("Only descendant of"),
    UNARY_DESCENDANT("Descendant of (via unary tree)"),
    SIBLING("Sibling of"),
    //IMMEDIATE_SIBLING,
    EQUALS("Equal to"),
    DESCENDANT_VIA("Descendant via"),
    PRECEDES_VIA("Precedes via");
    
    private String name;
    private Type(String name) {
      this.name = name;
    }
    
    /**
     * Return a list of all of the text descriptions of enums
     * @return
     */
    /*public static String[] descriptions() {
      String [] descs = new String[ Type.values().length ];
      for ( int i = 0; i < Type.values().length; ++i )
        descs[i] = Type.values()[i].name;
      return descs;
    }*/
    
    public String toString() { 
      return name;
    }
    
    public String getName() {
      return name;
    }
    public boolean hasNumberArg() {
      return (this == NTH_CHILD || this == NTH_TO_LAST_CHILD );
    }
    public boolean hasViaArg() {
      return (this == DESCENDANT_VIA || this == PRECEDES_VIA );
    }
  }

  public boolean isNegative() {
    return negative;
  }

  public void setNegative(boolean negative) {
    this.negative = negative;
  }
}
