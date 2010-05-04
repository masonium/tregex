package edu.stanford.nlp.trees.tregex.visual.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

public class VisualQueryFrame extends JFrame implements ActionListener {

  // constants
  private final String EDGE = "edgepanel";
  private final String NODE = "nodepanel";
  private final String EMPTY = "emptypanel";
  
  private JSplitPane contentPropertyPane;
  private JSplitPane fullPane;
  //private JPanel fullPanel;
  private QueryEditorPanel graphPanel;
  private JPanel propertiesPanel;
  private CardLayout ppLayout;
  private EdgePropertiesPanel edgePanel;
  private NodePropertiesPanel nodePanel;
  private JPanel emptyPanel;
  private TextQueryPanel textPanel;
  
  // menu
  JMenuItem clearQueryItem;
  private static final String CLEAR_QUERY_COMMAND = "Clear Query Graph Command";
  
  /**
   * 
   */
  private static final long serialVersionUID = 7420412255682215213L;

  public VisualQueryFrame() {
    super("Visual Query Constructor");
    
    //this.setDefaultLookAndFeelDecorated( true );
    this.setDefaultCloseOperation( JFrame.HIDE_ON_CLOSE );
   
    this.setResizable( false );
    this.setSize( 800, 620 );
    this.setLayout( new BorderLayout( ) );
    
    graphPanel = new QueryEditorPanel();
    graphPanel.setMinimumSize( new Dimension(500, 400) );
    
    propertiesPanel = new JPanel( ppLayout = new CardLayout() );
    propertiesPanel.setMinimumSize( new Dimension(275, 400) );
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
    edgePanel.addActionListener( this );
    
    textPanel = new TextQueryPanel();
    textPanel.setMinimumSize( new Dimension( 400, 100 ) );
    
    // add parts    
    contentPropertyPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, 
        propertiesPanel, graphPanel );
    contentPropertyPane.setOneTouchExpandable( false );
    contentPropertyPane.setEnabled( false );
    contentPropertyPane.setDividerLocation( 275 );
    contentPropertyPane.setResizeWeight( 0.3 );
    
        
    fullPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT, contentPropertyPane, textPanel );
    fullPane.setOneTouchExpandable( false );
    fullPane.setEnabled( false );
    fullPane.setDividerLocation( 500 );
    fullPane.setResizeWeight(0.9);
    
    JMenuBar mbar = new JMenuBar();
    JMenu menu = new JMenu( "Edit" );
    menu.add( clearQueryItem = new JMenuItem("Clear") );
    clearQueryItem.setActionCommand( CLEAR_QUERY_COMMAND );
    clearQueryItem.addActionListener( this );
    mbar.add( menu );
    
    this.add( mbar, BorderLayout.NORTH );
    this.add( fullPane );
    
    //this.setContentPane( fullPanel );
    //fullPanel.setSize( this.getSize() );
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    boolean graphChanged = false;
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
      graphChanged = true;
    }
    else if ( command.equals( QueryEditorPanel.EDGE_SELECTED ) ) {
      edgePanel.linkToEdge( (Edge)e.getSource() );
      ppLayout.show( propertiesPanel, EDGE );
    }
    else if ( command.equals( QueryEditorPanel.EDGE_DESELECTED ) ) {
      edgePanel.unlinkEdge();
      ppLayout.show( propertiesPanel, EMPTY );
    }
    else if ( command.equals( EdgePropertiesPanel.EDGE_CHANGED ) ) {
      nodePanel.repaint();
      graphChanged = true;
    }
    else if ( command.equals( QueryEditorPanel.GRAPH_CHANGED ) ||
        command.equals( NodePropertiesPanel.NODE_CHANGED ) )
      graphChanged = true;
    else if ( command.equals( this.CLEAR_QUERY_COMMAND ) ) 
      clear();
    
    if (graphChanged)
      textPanel.setQuery( graphPanel.getQuery() );
    /*
    else if ( command.equals( QueryEditorPanel.NODE_DELETED ) ) {
      nodePanel.unlinkNode();
      ppLayout.show( propertiesPanel, EMPTY );
    }*/
  }
  
  /**
   * add action listener for submission
   */
  public void addActionListener( ActionListener listener ) {
    textPanel.addActionListener( listener );
  }
  
  public String getQuery() {
    return textPanel.getQuery();
  }
  
  public static void main( String[] args ) {
    VisualQueryFrame frame = new VisualQueryFrame();
    frame.setDefaultCloseOperation( EXIT_ON_CLOSE );
    frame.setVisible( true );
  }

  public void clear() {
    nodePanel.unlinkNode();
    edgePanel.unlinkEdge();
    ppLayout.show( propertiesPanel, EMPTY );
    graphPanel.clearGraph();
  }
  
}
