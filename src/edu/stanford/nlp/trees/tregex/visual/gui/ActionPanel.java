package edu.stanford.nlp.trees.tregex.visual.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

public class ActionPanel extends JPanel {
  private static final long serialVersionUID = -2638380841944752844L;
  private List<ActionListener> actionListeners;

  public ActionPanel() {
    actionListeners = new ArrayList<ActionListener>();
  }
  
  public void addActionListener(ActionListener l) {
    actionListeners.add( l );
  }
  public void fireEvent(Object source, String command) {
    ActionEvent event = new ActionEvent( source, ActionEvent.ACTION_PERFORMED, command );
    for (ActionListener l: actionListeners)
      l.actionPerformed( event );
  }  
    
}
