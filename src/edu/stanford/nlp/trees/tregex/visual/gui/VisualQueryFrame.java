package edu.stanford.nlp.trees.tregex.visual.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

public class VisualQueryFrame extends JFrame implements ActionListener {

  // constants
  private final String EDGE = "edgepanel";
  private final String NODE = "nodepanel";
  private final String EMPTY = "emptypanel";
  
  private JSplitPane contentPropertyPane;
  private JSplitPane fullPane;
  private QueryEditorPanel graphPanel;
  private JPanel propertiesPanel;
  private CardLayout ppLayout;
  private EdgePropertiesPanel edgePanel;
  private NodePropertiesPanel nodePanel;
  private JPanel emptyPanel;
  private JPanel textPanel;
  
  /**
   * 
   */
  private static final long serialVersionUID = 7420412255682215213L;

  public VisualQueryFrame() {
    super("Visual Query Constructor");
    
    this.setDefaultLookAndFeelDecorated( true );
    this.setDefaultCloseOperation( JFrame.HIDE_ON_CLOSE );
   
    setLayout( new BorderLayout() );
    
    graphPanel = new QueryEditorPanel();
    graphPanel.setMinimumSize( new Dimension(400, 400) );
    
    propertiesPanel = new JPanel( ppLayout = new CardLayout() );
    propertiesPanel.setMinimumSize( new Dimension(275, 200) );
    edgePanel = new EdgePropertiesPanel();
    nodePanel = new NodePropertiesPanel();
    emptyPanel = new JPanel();
    propertiesPanel.add( edgePanel, EDGE );
    propertiesPanel.add( nodePanel, NODE );
    propertiesPanel.add( emptyPanel, EMPTY );
    ppLayout.show( propertiesPanel, EMPTY );
    
    // relay listeners
    graphPanel.addActionListener( this );
    nodePanel.addActionListener( this );
    
    textPanel = new JPanel();
    textPanel.setMinimumSize( new Dimension( 400, 50 ) );
    
    // add parts
    contentPropertyPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, 
        propertiesPanel, graphPanel );
    contentPropertyPane.setOneTouchExpandable( false );
    contentPropertyPane.setDividerLocation( 275 );
    contentPropertyPane.setResizeWeight( 0.3 );
    
    fullPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT, contentPropertyPane, textPanel );
    fullPane.setOneTouchExpandable( false );
    fullPane.setDividerLocation( 450 );
    fullPane.setResizeWeight(0.9);
    
    this.setContentPane( fullPane );
    this.setSize( 800, 600 );
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();
    if ( command.equals( QueryEditorPanel.NODE_SELECTED ) ) {
      nodePanel.linkToNode( (Node)e.getSource() );
      ppLayout.show( propertiesPanel, NODE );
    }
    else if ( command.equals( QueryEditorPanel.NODE_DESELECTED ) ) {
      nodePanel.unlinkNode();
      ppLayout.show( propertiesPanel, EMPTY );
    } 
    else if ( command.equals( NodePropertiesPanel.HEAD_NODE_SELECTED ) ) {
      graphPanel.setHeadNode( (Node) e.getSource() );
    }/*
    else if ( command.equals( QueryEditorPanel.NODE_DELETED ) ) {
      nodePanel.unlinkNode();
      ppLayout.show( propertiesPanel, EMPTY );
    }*/
  }
  
  public static void main( String[] args ) {
    VisualQueryFrame frame = new VisualQueryFrame();
    frame.setDefaultCloseOperation( EXIT_ON_CLOSE );
    frame.setVisible( true );
  }
  
}
