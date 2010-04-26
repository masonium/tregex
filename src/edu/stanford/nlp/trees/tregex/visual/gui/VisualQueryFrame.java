package edu.stanford.nlp.trees.tregex.visual.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

public class VisualQueryFrame extends JFrame {

  private JSplitPane contentPropertyPane;
  private JSplitPane fullPane;
  private QueryEditorPanel graphPanel;
  private PropertiesPanel propertiesPanel;
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
    propertiesPanel = new PropertiesPanel();
    propertiesPanel.setMinimumSize( new Dimension(275, 200) );
    
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
  
  public static void main( String[] args ) {
    VisualQueryFrame frame = new VisualQueryFrame();
    frame.setDefaultCloseOperation( EXIT_ON_CLOSE );
    frame.setVisible( true );
  }
  
}
