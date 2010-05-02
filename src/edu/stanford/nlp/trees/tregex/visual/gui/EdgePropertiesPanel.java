
package edu.stanford.nlp.trees.tregex.visual.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;

import edu.stanford.nlp.trees.tregex.visual.EdgeDescriptor;

public class EdgePropertiesPanel extends JPanel implements ActionListener {
  
  private static final long serialVersionUID = -755852857965654456L;
  
  JComboBox edgeTypeList;
  JCheckBox optionalCheckbox;
  
  public EdgePropertiesPanel() {
    setLayout( new BoxLayout(this, BoxLayout.Y_AXIS) );
    //this.setAlignmentX( LEFT_ALIGNMENT );
    
    Dimension optimalSize = new Dimension( 250, 50 );
    edgeTypeList = new JComboBox(
        EdgeDescriptor.Type.descriptions() );
    edgeTypeList.addActionListener( this );
    edgeTypeList.setPreferredSize( optimalSize );
    edgeTypeList.setMaximumSize( optimalSize );
    
    optionalCheckbox = new JCheckBox();
    optionalCheckbox.setText( "Optional Constraint" );
    optionalCheckbox.addActionListener( this );
    optionalCheckbox.setAlignmentX( CENTER_ALIGNMENT );
    
    this.add( Box.createRigidArea( new Dimension(0, 20) ) );
    this.add( edgeTypeList );
    this.add( Box.createRigidArea( new Dimension(0, 10) ) );    
    this.add( optionalCheckbox );
    //this.add( Box.createVerticalGlue() );
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    // TODO Auto-generated method stub
    
  }
}
