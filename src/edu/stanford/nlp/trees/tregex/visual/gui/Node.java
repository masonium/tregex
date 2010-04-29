package edu.stanford.nlp.trees.tregex.visual.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import net.java.swingfx.jdraggable.Draggable;

public class Node extends JLabel implements Draggable {

  private static final long serialVersionUID = 2764812387581897795L;

  public Node(String label) {
    super(label);
    this.setBorder( BorderFactory.createLineBorder( Color.BLACK ) );
    setVisible(true);
  }

  @Override
  public Component getComponent() {
    return this;
  }
  
}
