package edu.stanford.nlp.trees.tregex.visual.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Line2D;

import edu.stanford.nlp.trees.tregex.visual.EdgeDescriptor;
import edu.stanford.nlp.trees.tregex.visual.QueryEdge;

public class Edge implements Selectable {

  public enum Type {
    GRAPH_EDGE,
    SHARED_LABEL
  }
  
  // whether or not the edge is selected
  boolean selected;
  
  // internal edge, if it exists
  QueryEdge edge;
  Type type;
  
  // GUI-level nodes
  Node n1, n2;
  
  // underlying main line
  Line2D.Double line;
  
  // shared label edge
  public Edge(Node n1, Node n2) {
    this.n1 = n1;
    this.n2 = n2;
    this.type = Type.SHARED_LABEL;
    this.line = new Line2D.Double( n1.getLocation(), n2.getLocation() );
  }
  
  // graph edge
  public Edge(Node n1, Node n2, QueryEdge edge) {
    this.n1 = n1;
    this.n2 = n2;
    this.edge = edge;
    this.type = Type.GRAPH_EDGE;
    this.line = new Line2D.Double(n1.getLocation(), n2.getLocation());
  }
  
  public void flip() {
    Node temp = n1;
    n1 = n2;
    n2 = temp;
    
    edge.flip();
  }
  
  public boolean clickedOn( int x, int y ) {
    return line.intersects( x-2, y-2, 5, 5 );
  }
  
  private void update( ) {
    // the component moved, so we need to update the underlying line
    this.line = new Line2D.Double(n1.getCenter(), n2.getCenter());
  }
  
  // rendering
  public void render( Graphics g ) {
    update();
    
    Graphics2D g2 = (Graphics2D) g;
        
    g2.setColor( Color.BLACK );
    
    // The drawing style depends on the edge type.
    switch (this.type) {
    case GRAPH_EDGE:
      // for now, just draw a line
      g2.draw( line );
      break;
    default:
        break;
    }
    
  }

  public boolean isSelected() {
    return selected;
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }
  
  public boolean hasNumberArg() { return edge.getDescriptor().getEdgeType().hasNumberArg(); }
  public boolean hasViaArg() { return edge.getDescriptor().getEdgeType().hasViaArg(); }
  
  public EdgeDescriptor.Type getType() { return edge.getDescriptor().getEdgeType(); }
  public void setType(EdgeDescriptor.Type type) { edge.getDescriptor().setEdgeType( type ); }
  
  public boolean isOptional() { return edge.getDescriptor().isOptional(); }
  public void setOptional( boolean optional ) { edge.getDescriptor().setOptional( optional ); }
  
  public boolean isNegative() { return edge.getDescriptor().isNegative(); }
  public void setNegative( boolean negative ) { edge.getDescriptor().setNegative( negative ); }
  
  public int getN() { return edge.getDescriptor().getN(); }
  public void setN( int N ) { edge.getDescriptor().setN( N ); }
  
  public String getVia() { return edge.getDescriptor().getViaPattern(); }
  public void setVia( String via ) { edge.getDescriptor().setViaPattern( via ); }

  @Override
  public boolean getSelected() {
    return selected;
  }
  
}
