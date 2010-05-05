package edu.stanford.nlp.trees.tregex.visual.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.RenderingHints.Key;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;

import edu.stanford.nlp.trees.tregex.visual.EdgeDescriptor;
import edu.stanford.nlp.trees.tregex.visual.QueryEdge;

public class Edge implements Selectable {

  public enum Type {
    GRAPH_EDGE,
    SHARED_LABEL
  }
  
  // whether or not the edge is selected
  boolean selected;
  Type type;
  
  // internal edge, if it exists
  QueryEdge edge;
  
  // shared label
  String sharedLabel;
  
  // GUI-level nodes
  Node n1, n2;
  
  // underlying main line
  Line2D.Double line;
  
  // shared label edge
  public Edge(Node n1, Node n2, String sharedLabel) {
    this.n1 = n1;
    this.n2 = n2;
    this.type = Type.SHARED_LABEL;
    this.line = new Line2D.Double( n1.getLocation(), n2.getLocation() );
    throw new RuntimeException( "Shared label edges unimplemented" );
  }
  
  // graph edge
  public Edge(Node n1, Node n2, QueryEdge edge) {
    this.n1 = n1;
    this.n2 = n2;
    this.edge = edge;
    this.type = Type.GRAPH_EDGE;
    this.line = new Line2D.Double(n1.getLocation(), n2.getLocation());
    this.edge.setOwner( this );
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
        
    g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
    g2.setStroke( new BasicStroke(2.0f) );
    g2.setColor( isSelected() ? Color.red : Color.BLACK );
    
    // The drawing style depends on the edge type.
    switch (this.type) {
    case GRAPH_EDGE:
      renderGraphEdge( g2 );
      break;
    case SHARED_LABEL:
      renderSharedLabelEdge( g2 );
    default:
        break;
    }
    
  }
  
  private void renderSharedLabelEdge( Graphics2D g2 ) {

    Color oldColor = g2.getColor();
    Stroke oldStroke = g2.getStroke();
    
    float dash[] = {7.0f};
    
    g2.setColor( Color.GREEN );
    g2.setStroke( new BasicStroke( 1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 
       10.0f, dash, 0.0f ) );
    g2.draw( line );
    g2.setColor( oldColor );
    g2.setStroke( oldStroke );    
  }
  
  private void renderGraphEdge(Graphics2D g2) {
    g2.draw( line );
    // draw the additional components, depending on the edge type
    
    double dy = line.y1 - line.y2;
    double dx = line.x1 - line.x2;
    double rawOrientation = Math.atan2( dy, dx);
    double length = Math.sqrt( dx * dx + dy * dy );
    double orientation = rawOrientation - Math.PI / 2.0;
    Point2D.Double center = new Point2D.Double( 0.5 * (line.x1 + line.x2), 0.5 * (line.y1 + line.y2) );

    boolean closed = !getType().isPrecedesType();
    boolean filled = getType().isDescendantType();
    boolean drawDouble = getType().requiresDoubleType();
    boolean drawBar = getType().isOnlyType();
    boolean hasVia = getType().hasViaArg();
    boolean hasN = getType().hasNumberArg();
    boolean directional = getType().isDirectionalType();
    
    double width = 16;
    double wd = width * Math.sqrt( 3.0 )/2.0;
    double wb = width / 2.0;
    
    // positive y is toward N1
    AffineTransform orig = g2.getTransform();
    AffineTransform tr = new AffineTransform(orig);
    tr.concatenate( AffineTransform.getTranslateInstance( center.x, center.y ) );
    tr.concatenate( AffineTransform.getRotateInstance( orientation ) );
    
    g2.setTransform( tr );
    
    renderTriangle( g2, width, closed, filled );
    
    if ( drawDouble ) {
      AffineTransform tr2 = new AffineTransform(tr);
      tr2.concatenate( AffineTransform.getTranslateInstance( 0.0, wd ) );
      if (getType() == EdgeDescriptor.Type.SIBLING)
        tr2.concatenate( AffineTransform.getRotateInstance( Math.PI ) );
      g2.setTransform( tr2 );
      renderTriangle( g2, width, closed, filled );
      g2.setTransform( tr );
    }
    
    // draw the bar, if necessary 
    if ( drawBar ) {
      AffineTransform tr2 = new AffineTransform(tr);
      tr2.concatenate( AffineTransform.getTranslateInstance( 0.0, -wb ) );
      g2.setTransform( tr2 );
      renderBar( g2, width );
      g2.setTransform( tr );
    }
    
    // draw the arguments
    if ( hasVia || hasN || directional) {
      AffineTransform tr2 = new AffineTransform(tr);
      tr2.concatenate( AffineTransform.getTranslateInstance( width, 0.0 ) );
      tr2.concatenate( AffineTransform.getRotateInstance(-orientation) );
      g2.setTransform( tr2 );
      
      String arg = "";
      if (hasVia)
        arg = getVia();
      else if (directional) {
        getType();
        if (getType() == EdgeDescriptor.Type.LEFTMOST_DESCENDANT)
          arg = "[L]";
        else
          arg = "[R]";
      } else {
        getType();
        if (getType() == EdgeDescriptor.Type.NTH_TO_LAST_CHILD)
          arg = "-";
        arg += getN();
      }
      
      g2.drawString( arg, 0, 0 );
      g2.setTransform( tr );
    }
    
    if ( isOptional() ) {
      AffineTransform tr2 = new AffineTransform(tr);
      tr2.concatenate( AffineTransform.getTranslateInstance( 0.0, length/4.0 ) );
      tr2.concatenate( AffineTransform.getRotateInstance(-orientation) );
      tr2.concatenate( AffineTransform.getScaleInstance(1.5, 1.5) );
      g2.setTransform( tr2 );
      
      drawQuestion( g2 );
      
      g2.setTransform( tr );
    }
    
    if ( isNegative() ) {
      AffineTransform tr2 = new AffineTransform(tr);
      tr2.concatenate( AffineTransform.getTranslateInstance( 0.0, -length/4.0 ) );
      tr2.concatenate( AffineTransform.getRotateInstance(-orientation) );
      
      g2.setTransform( tr2 );
      
      drawX( g2, width );
      
      g2.setTransform( tr );
    }
    
    g2.setTransform( orig );    
  }
  
  private void drawQuestion(Graphics2D g2) {
    
    Color oldColor = g2.getColor();
    g2.setColor( Color.GREEN );
    g2.drawString( "?", 0, 0 );
    g2.setColor( oldColor );
  }
  
  private void drawX(Graphics2D g2, double width) {
    int hw = (int)(width/2);
    
    Color oldColor = g2.getColor();
    Stroke oldStroke = g2.getStroke();
    g2.setColor(Color.RED);
    g2.setStroke( new BasicStroke(2.5f) );
    
    g2.drawLine( -hw, -hw, hw, hw );
    g2.drawLine( hw, -hw, -hw, hw );
    
    g2.setColor( oldColor );
    g2.setStroke( oldStroke );
  }
  
  /**
   * render a bar for 'only' types
   * @param g2
   * @param width
   */
  private void renderBar(Graphics2D g2, double width) {
    double hw = width / 2.0;
    double f = 0.25;
    g2.fillRect( (int)-hw, (int)(-hw * f), (int)width, (int)(width * f) );    
  }

  /**
   * Draw a triangle pointing toward n1
   * @param g2
   * @param orientation
   * @param center
   * @param width
   */
  private void renderTriangle( Graphics2D g2, double width, boolean closed, boolean filled ) {
    int fromCenter = (int)(width / Math.sqrt(3.0));
    int hw = (int)(width / 2.0);
    int x[] = { hw, 0, -hw };
    int y[] = { -fromCenter/2, fromCenter, -fromCenter/2 };
     
    if (closed && filled)
      g2.fillPolygon( x, y, 3 );
    else if (closed)
      g2.drawPolygon( x, y, 3 );
    else
      g2.drawPolyline( x, y, 3 ); 
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

  QueryEdge getQueryEdge() {
    return edge;
  }
  
}
