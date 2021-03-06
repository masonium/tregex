package edu.stanford.nlp.trees.tregex.visual.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.border.Border;

import edu.stanford.nlp.trees.tregex.visual.QueryNode;

import net.java.swingfx.jdraggable.Draggable;

public class Node extends JLabel implements Draggable, Selectable {

  private static final long serialVersionUID = 2764812387581897795L;

  private boolean selected;
  private boolean headNode;

  // underlying query node that this node represents
  private QueryNode queryNode;
  
  public Node( QueryEditorPanel panel, QueryNode node ) {
    this( panel, node, false );
  }
  
  public Node( QueryEditorPanel panel, QueryNode node, boolean selected ) {
    super( node.label );
    this.queryNode = node;
    this.selected = selected;
    this.headNode = false;
    this.queryNode.setOwner( this );
    updateText();
    setVisible( true );
    setSelected( selected );
    //deleteNodeItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_DELETE, 0 ) );
  }

  @Override
  public void setText(String text) {
    super.setText( text );

    updateBounds();
  }

  /**
   * Update the bounds of the label and redraw. Because we are using absolute
   * positioning, this needs to be called whenever we apply any changes that
   * could affect appearance.
   */
  private void updateBounds() {
    // reset the bounds
    this.setBounds( this.getBounds().x, this.getBounds().y, this
        .getPreferredSize().width, this.getPreferredSize().height );

    // repaint
    this.repaint();
  }

  public void setSelected(boolean selected) {
    if (this.selected == selected)
      return;

    this.selected = selected;
    
    updateAppearance();
  }

  private void updateAppearance() {    
    Color c = isHeadNode() ? new Color(0.5f, 0.5f, 0.8f) : Color.BLACK;
    Border border = BorderFactory.createCompoundBorder( 
        BorderFactory.createLineBorder( c ),
        BorderFactory.createEmptyBorder(3, 3, 3, 3) );
    if (selected)
      border = BorderFactory.createCompoundBorder( BorderFactory.createLineBorder( Color.RED ),
          BorderFactory.createCompoundBorder( BorderFactory.createEmptyBorder( 1, 1, 1, 1 ),
              border));
    this.setOpaque( true );
    this.setBorder( border );
    this.setBackground( Color.WHITE );
    this.setForeground( c );
    
    updateBounds();
  }
  
  private void updateText() {
    setText( "<html>" + queryNode.getPattern() + //Util.escapeHTML( queryNode.pattern ) + 
        (queryNode.label.isEmpty() ? "" : 
          "<P>[" + Util.escapeHTML( queryNode.label ) + "]</html>"));  
  }
  
  @Override
  public Component getComponent() {
    return this;
  }
  
  public boolean isHeadNode() {
    return headNode;
  }

  public void setHeadNode(boolean headNode) {
    this.headNode = headNode;
    updateAppearance();
  }

  public String getPattern() { return queryNode.getPattern(); }
  public void setPattern( String pattern ) { queryNode.setPattern(pattern); updateText(); }
  
  public String getLabel() { return queryNode.label; }
  public void setLabel( String label ) { queryNode.label = label; updateText(); }
  
  public String getGroupLabel(int i) {
    String g = queryNode.groupLabels.get( i );
    return (g == null) ? "" : g;
  }
  
  public void setGroupLabel(int i, String label) {
    queryNode.groupLabels.put( i, label.trim() );
  }

  public QueryNode getQueryNode() {
    return queryNode;
  }
  
  public Point getCenter() {
    Point loc = getLocation();
    Dimension size = getSize();
    return new Point(loc.x + size.width/2, loc.y + size.height/2);
  }

  @Override
  public boolean getSelected() {
    return selected;
  }
}
