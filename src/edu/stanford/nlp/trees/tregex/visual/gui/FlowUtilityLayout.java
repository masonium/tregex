/**
 * 
 */
package edu.stanford.nlp.trees.tregex.visual.gui;

import javax.swing.JComponent;

class FlowUtilityLayout extends javax.swing.SpringLayout {
  
  private JComponent parent;
  public int hgap;
  public int vgap;
  
  public FlowUtilityLayout(JComponent parent, int hgap, int vgap) {
    this.parent = parent;
    this.hgap = hgap;
    this.vgap = vgap;
  }
  
  public FlowUtilityLayout startOffset( JComponent component ) {
    putConstraint( WEST, component, vgap, WEST, parent );
    putConstraint( NORTH, component, vgap, NORTH, parent );
    return this;
  }
  
  public FlowUtilityLayout rightOf( JComponent c1, JComponent c2, boolean isText ) {
    putConstraint( WEST, c2, hgap, EAST, c1 );
    putConstraint( isText ? BASELINE : VERTICAL_CENTER, c2, 0, 
        isText ? BASELINE : VERTICAL_CENTER, c1 );
    return this;
  }
  public FlowUtilityLayout rightOf( JComponent c1, JComponent c2 ) {
    return rightOf(c1, c2, true);
  }
  
  public FlowUtilityLayout below( JComponent c1, JComponent c2 ) {
    putConstraint( NORTH, c2, vgap, SOUTH, c1  );
    putConstraint( WEST, c2, 0, WEST, c1 );
    return this;
  }
}