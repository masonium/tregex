package edu.stanford.nlp.trees.tregex.visual.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.stanford.nlp.trees.tregex.visual.QueryEdge;
import edu.stanford.nlp.trees.tregex.visual.EdgeDescriptor;

public class EdgePropertiesPanel extends ActionPanel implements ActionListener, ItemListener {

  private static final long serialVersionUID = -755852857965654456L;
  
  // fired events
  public static final String EDGE_CHANGED = "Edge Changed";
  
  // GUI
  private JComboBox edgeTypeList;
  private JCheckBox optionalCheckbox;
  private JCheckBox negativeCheckbox;

  private JTextField nField;
  private JTextField viaField;
  
  Edge linkedEdge;
  
  public EdgePropertiesPanel() {
    FlowUtilityLayout layout = new FlowUtilityLayout( this, 10, 15 );
    setLayout( layout );

    JLabel title = new JLabel( "<html><h2>Edge Properties</h2></html>" );

    // construct the constituent components
    Dimension optimalSize = new Dimension( 250, 50 );
    edgeTypeList = new JComboBox( EdgeDescriptor.Type.values() );
    edgeTypeList.addActionListener( this );
    edgeTypeList.setPreferredSize( optimalSize );
    edgeTypeList.setMaximumSize( optimalSize );

    optionalCheckbox = new JCheckBox( "Optional Constraint" );
    optionalCheckbox.addItemListener( this );
    negativeCheckbox = new JCheckBox( "Optional Constraint" );
    negativeCheckbox.addItemListener( this );

    JLabel nLabel = new JLabel( "N: " );
    nField = new JTextField( 3 );
    //nField.getDocument().addDocumentListener( this );
    JLabel viaLabel = new JLabel( "Via: " );
    viaField = new JTextField( 15 );
    //viaField.getDocument().addDocumentListener( this );

    // layout components
    layout.startOffset( title ).below( title, edgeTypeList )
      .below(edgeTypeList, optionalCheckbox )
      .below( optionalCheckbox, negativeCheckbox )
      .below( negativeCheckbox, nLabel ).rightOf( nLabel, nField )
      .below( nLabel, viaLabel ).rightOf( viaLabel, viaField );
  }
  
  public void linkToEdge( Edge edge ) {
    linkedEdge = edge;
    
    // copy over the fields
    edgeTypeList.setSelectedItem( edge.getType() );
    optionalCheckbox.setSelected( edge.isOptional() );
    negativeCheckbox.setSelected( edge.isNegative() );

    nField.setText( "" );
    viaField.setText( "" );
    
    // enable/disable fields as necessary
    updateFields();
    
    if (edge.hasNumberArg())
      nField.setText( "" + edge.getN() );
    if (edge.hasViaArg())
      viaField.setText( edge.getVia() );    
  }
  
  public void unlinkEdge( ) {
    linkedEdge = null;
  }

  private void updateFields() {
    nField.setEnabled( linkedEdge.hasNumberArg() );
    viaField.setEnabled( linkedEdge.hasViaArg() );
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    // the only action source is the type selector
    linkedEdge.setType( (EdgeDescriptor.Type)edgeTypeList.getSelectedItem() );
    
    updateFields();
    
    fireEvent( linkedEdge, EDGE_CHANGED );
  }

  @Override
  public void itemStateChanged(ItemEvent arg0) {
    Object source = arg0.getSource();
    int stateChange = arg0.getStateChange();
    if (source == optionalCheckbox)
      linkedEdge.setOptional( stateChange == ItemEvent.SELECTED );
    if (source == negativeCheckbox)
      linkedEdge.setNegative( stateChange == ItemEvent.SELECTED );
    fireEvent( linkedEdge, EDGE_CHANGED );
  }
}
