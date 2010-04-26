package edu.stanford.nlp.trees.tregex.visual.gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

import net.java.swingfx.jdraggable.DefaultDraggableManager;
import net.java.swingfx.jdraggable.DragPolicy;

public class QueryEditorPanel extends JPanel {

  /**
   * 
   */
  private static final long serialVersionUID = 3495240357289682684L;

  DefaultDraggableManager dm;
  
  public QueryEditorPanel() {
    dm = new DefaultDraggableManager( this );
    dm.setDragPolicy( DragPolicy.STRICT );
    
    this.add( new Node("Hello") );
  }
  /*
  @Override
  public void paint(Graphics gr) {
    //Graphics2D g = (Graphics2D) gr;
  }
  */
}
