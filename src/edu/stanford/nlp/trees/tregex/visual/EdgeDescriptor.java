package edu.stanford.nlp.trees.tregex.visual;

public class EdgeDescriptor implements Cloneable {	
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
	  this.via = ".*";
	}
	
	public String render(boolean reverse) {
	  String result = simpleRender( reverse );
	  result = (negative ? "!" : "") + (optional ? "?" : "") + result;
		return result;
	}
		
	/**
	 * Render the operation, ignoring negative and optional traits
	 * @param reverse
	 * @return
	 */
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
		case LEFTMOST_DESCENDANT:
		  return normal ? ">>," : "<<,";
		case RIGHTMOST_DESCENDANT:
		  return normal ? ">>-" : "<<-";
		case SIBLING: 
			return "$";
		case LEFT_SIBLING:
		  return normal ? "$.." : "$,,";
    case IMMEDIATE_LEFT_SIBLING:
      return normal ? "$." : "$,";
		case DESCENDANT_VIA:
		  return (normal ? ">" : "<") 
		    + "+(" + via + ")";
		case PRECEDES_VIA:
		  return (normal ? "." : ",") 
      + "+(" + via + ")";
		default:
		//case EQUALS:
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
    LEFTMOST_DESCENDANT("Left-most Descendant of"),
    RIGHTMOST_DESCENDANT("Right-most Descendant of"),
    SIBLING("Sibling of"),
    LEFT_SIBLING("Left Sibling of"),
    IMMEDIATE_LEFT_SIBLING("Immediate Left Sibling of"),
    DESCENDANT_VIA("Descendant via"),
    PRECEDES_VIA("Precedes via");
    
    private String name;
    private Type(String name) {
      this.name = name;
    }

    // requires bar 
    public boolean isOnlyType() {
      return this == UNARY_DESCENDANT || this == ONLY_DESCENDANT;
    }
    
    // requires double
    public boolean requiresDoubleType() {
      switch (this) {
      case DESCENDANT:
      case PRECEDES:
      case UNARY_DESCENDANT:
      case SIBLING:
      case LEFT_SIBLING:
        return true;
      default:
        return false;        
      }
    }
    
    public boolean isPrecedesType() {
      switch (this) {  
      case PRECEDES:
      case IMMEDIATELY_PRECEDES:
      case PRECEDES_VIA:
        return true;
      default:
        return false;
      }
    }
    
    public boolean isSiblingType() {
      switch (this) {
      case SIBLING:
      case LEFT_SIBLING:
      case IMMEDIATE_LEFT_SIBLING:
        return true;
      default:
         return false;
      }
    }
    
    public boolean isDescendantType() {
      switch (this) {
      case DESCENDANT:
      case DIRECT_DESCENDANT:
      case NTH_CHILD:
      case NTH_TO_LAST_CHILD:
      case ONLY_DESCENDANT:
      case UNARY_DESCENDANT:
      case LEFTMOST_DESCENDANT:
      case RIGHTMOST_DESCENDANT:
      case DESCENDANT_VIA:
        return true;
      default:
          return false;
      }
    }
    
    public boolean isDirectionalType() {
      return this == LEFTMOST_DESCENDANT || this == RIGHTMOST_DESCENDANT;
    }
    
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
  
  public Object clone() {
    EdgeDescriptor d = new EdgeDescriptor();
    d.edgeType = edgeType;
    d.N = N;
    d.negative = negative;
    d.optional = optional;
    d.via = new String(via);
    return d;
  }
}
