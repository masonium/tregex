package edu.stanford.nlp.trees.tregex.visual.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;


public class NodePropertiesPanel extends ActionPanel implements ItemListener {
  private static final long serialVersionUID = 1331070331370996950L;
  
  // implemented events
  public static final String HEAD_NODE_SELECTED = "Head Node Selected";
    
  private final int MAX_GROUPS = 4;
  
  // GUI
  JLabel title;
  JLabel patternLabel;
  JTextField pattern;
  JLabel labelLabel;
  JTextField label;
  
  List<JLabel> groupLabels;
  List<JTextField> groupFields;
  
  JCheckBox headNodeCheckBox;
  FlowUtilityLayout layout;
  
  // state
  Node linkedNode;
  
  public NodePropertiesPanel()
  {    
    this.setLayout( layout = new FlowUtilityLayout(this, 10, 15) );
    
    title = new JLabel("<html><h2>Node Properties</h2></html>");
    
    patternLabel = new JLabel("Pattern:");
    labelLabel = new JLabel("Label:");
    
    pattern = new JTextField( 15 );
    pattern.getDocument().addDocumentListener( new Listener(this, Field.PATTERN) );
    label = new JTextField( 10 );
    label.getDocument().addDocumentListener( new Listener(this, Field.LABEL) );
    
    headNodeCheckBox = new JCheckBox("Head Node:");
    headNodeCheckBox.addItemListener( this );
    
    // add components 
    this.add( title );  
    this.add( patternLabel );
    this.add( pattern );
    this.add( labelLabel );
    this.add( label );
    this.add( headNodeCheckBox );
       
    layout.startOffset( title )
      .below( title, patternLabel )
      .rightOf(patternLabel, pattern )
      .below( patternLabel, labelLabel )
      .rightOf( labelLabel, label )
      .below( labelLabel, headNodeCheckBox );
        
    groupLabels = new ArrayList<JLabel>();
    groupFields = new ArrayList<JTextField>();
    
    JComponent top = headNodeCheckBox;
    for (int i = 0; i < MAX_GROUPS; ++i ) {
      groupLabels.add( new JLabel("Group " + (i+1) + ":") );
      groupFields.add( new JTextField(15) );
      
      groupFields.get( i ).getDocument().addDocumentListener( 
          new Listener(this, Field.valueOf( "GROUP" + i ) ) );
      
      this.add( groupLabels.get( i ) );
      this.add( groupFields.get( i ) );
      layout.below( top, groupLabels.get(i) )
        .rightOf( groupLabels.get( i ), groupFields.get(i) );
      top = groupLabels.get( i );
    }
  }
  
  /**
   * Populate the panel fields with those from the node.
   * Update the linked node whenever the fields change to new valid values.
   * @param node
   */
  void linkToNode( Node node )
  {
    this.linkedNode = node;
    
    // populate the fields
    pattern.setText( node.getPattern() );
    label.setText( node.getLabel() );
        
    int numGroups = updateGroupFields();
    for (int i = 0; i < numGroups; ++i) {
      String g = node.getGroupLabel( i );
      groupFields.get( i ).setText( g );
    }
    
    headNodeCheckBox.setSelected( node.isHeadNode() );
    headNodeCheckBox.setEnabled( !node.isHeadNode() );
  }

  void unlinkNode() {
    linkedNode = null;
    
    // unpopulate all of the group fields
    for (JTextField groupField: groupFields)
      groupField.setText( "" );
  }
  
  /*
   * update the group fields, based on the current pattern
   */
  private int updateGroupFields() {    
    // figure out the number of groups in the pattern
    // if the pattern is not a correct regex, just don't update the gropus
    Matcher m = Util.isValidRegex( pattern.getText() );
    if (m == null)
      return 0;
    int numGroups = m.groupCount();
    
    for (int i = 0; i < MAX_GROUPS; ++i)
      groupFields.get( i ).setEditable( i < numGroups );
    return numGroups;
  }
  
  enum Field {
    LABEL(0),
    PATTERN(0),
    GROUP0(0),
    GROUP1(1),
    GROUP2(2),
    GROUP3(3);
    
    private int id;
    public int getId() { return id; }
    public static Field getGroup(int i) {
      switch (i)
      {
      case 0: return GROUP0;
      case 1: return GROUP1;
      case 2: return GROUP2;
      default: return GROUP3;
      }
    }
    private Field(int i) {
      this.id = i;
    }
  }
  
  class Listener implements DocumentListener {
    Field field;
    NodePropertiesPanel panel;
    
    public Listener(NodePropertiesPanel panel, Field field) {
      this.field = field;
      this.panel = panel;
    }
    
    @Override
    public void changedUpdate(DocumentEvent arg0) {
      update( arg0 );
    }

    @Override
    public void insertUpdate(DocumentEvent arg0) {
      update( arg0 );     
    }

    @Override
    public void removeUpdate(DocumentEvent arg0) {
      update(arg0);
    }
    
    private void update(DocumentEvent arg0) {
      System.out.println("change");
      
      if ( panel.linkedNode == null )
        return;
      
      Document d = arg0.getDocument();
      String newText = "";
      try {
      newText = d.getText( 0, d.getLength() );
      } catch (Exception e) {
        
      }
      String trimmed = newText.trim(); 
      switch( field ) {
      case LABEL:
        panel.linkedNode.setLabel( trimmed );
        return;
      case PATTERN:
        if (Util.isValidRegex(newText) != null) {
          panel.linkedNode.setPattern( newText );
          panel.updateGroupFields();
        }
        return;
      // case GROUPn
      default:
        panel.linkedNode.setGroupLabel( field.id, trimmed );
        return;
      }
    }
  }
  

  @Override
  public void itemStateChanged(ItemEvent arg0) {
    if (arg0.getStateChange() == ItemEvent.SELECTED) {
      // make the checkbox uneditable
      headNodeCheckBox.setEnabled( false );
      
      // change the node's state
      linkedNode.setHeadNode( true );
      
      // notify other components
      fireEvent( linkedNode, HEAD_NODE_SELECTED );
    }
  }
  
}
