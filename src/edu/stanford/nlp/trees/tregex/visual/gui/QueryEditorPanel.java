package edu.stanford.nlp.trees.tregex.visual.gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import edu.stanford.nlp.trees.tregex.visual.QueryGraph;
import edu.stanford.nlp.trees.tregex.visual.QueryNode;

import net.java.swingfx.jdraggable.DefaultDraggableManager;
import net.java.swingfx.jdraggable.DragPolicy;

public class QueryEditorPanel extends ActionPanel implements ActionListener, MouseListener {

  /**
   * 
   */
  private static final long serialVersionUID = 3495240357289682684L;

  // event types produced by this component
  public static final String NODE_SELECTED = "Node Selected";
  public static final String NODE_DESELECTED = "Node Deselected";
  public static final String EDGE_SELECTED = "Edge Selected";
  public static final String EDGE_DESELCTED = "Edge Deselected";
  public static final String NODE_DELETED = "Node Deleted";  
  
  DefaultDraggableManager dm;

  // Edge popup menu
  
  // Node popup menu

  JPopupMenu nodeMenu;
  JMenuItem deleteNodeItem;

  // popup menu
  JPopupMenu popupMenu;
  JMenuItem addNodeItem;
  JMenuItem addEdgeItem;

  // underlying query graph
  QueryGraph queryGraph;
  int mouseX, mouseY;

  Node selectedNode;
  Node headNode;
  
  public QueryEditorPanel() {
    this.setLayout( null );
    
    dm = new DefaultDraggableManager( this );
    dm.setDragPolicy( DragPolicy.STRICT );
    
    initializePopupMenu();

    queryGraph = new QueryGraph();
    
    this.addMouseListener( this );
  }

  private void initializePopupMenu() {
    popupMenu = new JPopupMenu();
    popupMenu.add( addNodeItem = new JMenuItem("Add Node") );
    //addNodeItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_N, ActionEvent.CTRL_MASK ) );
    addNodeItem.addActionListener( this );
    popupMenu.addSeparator();
    popupMenu.add( addEdgeItem = new JMenuItem("Add Edge") );
    //addEdgeItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_E, ActionEvent.CTRL_MASK ) );
    addEdgeItem.addActionListener( this );
    this.add( popupMenu );
    this.addMouseListener( new PopupListener(this, popupMenu) );
    
    
    nodeMenu = new JPopupMenu();
    nodeMenu.add( deleteNodeItem = new JMenuItem("Delete Node") );
    deleteNodeItem.addActionListener( this );
  }
  
  @Override
  public void actionPerformed(ActionEvent arg0) {
    Object source = arg0.getSource();
    String command = arg0.getActionCommand();
    
    // menu item for adding a node
    if (source == addNodeItem) {
      System.out.print( "(" + mouseX + ", " + mouseY + ") " );
      createNode(mouseX, mouseY);
      System.out.println( this.getComponentCount() );
    }
    
    // menu item for deleting a node
    if (source == deleteNodeItem ) {
      if ( selectedNode != null && selectedNode != headNode ) {
        deleteNode( selectedNode );
      }
    }
  }
  
  /**
   * deletes the selected node, if it is not the head node
   * @param node
   */
  public void deleteNode( Node node ) {
    assert( node != null );
    if ( node == headNode )
      return;
    
    // deselect the node
    selectNode( null );
    
    // delete the node from the query graph
    queryGraph.removeNode( node.getQueryNode() );
    
    // remove the component from the panel
    this.remove( node );
    
    // signal deletion
    fireEvent( node, NODE_DELETED );
  }
  
  public void selectNode( Node nodeToSelect ) {
    // unselect any currently-selected nodes
    if (selectedNode != null) {
      selectedNode.setSelected( false );
      fireEvent( selectedNode, NODE_DESELECTED );
    }

    selectedNode = nodeToSelect;

    // [de]activate the delete option, depending on whether this is the head node or not
    deleteNodeItem.setEnabled( nodeToSelect != headNode );
    
    if (nodeToSelect == null)
      return;
    
    // select and store the input node
    nodeToSelect.setSelected( true );    
      
    // tell interested parties that we've done this
    fireEvent( selectedNode, NODE_SELECTED );
  }
  
  /**
   * Create a node at the most recent popup location
   */  
  public void createNode(int x, int y) {
    QueryNode qn = queryGraph.createNode();
    Node n = new Node( this, qn );

    // make it the head node if it is currently the only node
    if ( queryGraph.getNumNodes() == 1 ) {
      n.setHeadNode( true );
      setHeadNode( n );
    }
    
    // place on the screen
    n.setLocation( x, y );
    this.add( n );
    this.repaint();
    
    // add the popup
    n.addMouseListener( new NodeListener(n, this) );
    
    // select this new node
    selectNode( n );
    
    System.out.println( qn.label );
  }
  
  public void setHeadNode( Node node ) {
    if (headNode != null) {
      headNode.setHeadNode( false );
    }
    headNode = node;
    headNode.setHeadNode( true );
  }
  
  /**
   * Detects left and right clicks from a node
   * @author mason
   *
   */
  class NodeListener extends MouseAdapter {
    QueryEditorPanel panel;
    Node target;
    
    public NodeListener(Node n, QueryEditorPanel panel) {
      this.panel = panel;
      this.target = n;
    }
    
    public void mousePressed(MouseEvent e) {
      // select this node
      panel.selectNode( target );
      
      maybeShowPopup( e );
    }
    
    public void mouseReleased(MouseEvent e) {
      maybeShowPopup( e );
    }
    
    public void maybeShowPopup(MouseEvent e) {
      if (e.isPopupTrigger())
        panel.nodeMenu.show( e.getComponent(), e.getX(), e.getY() );
    }
  }
    
  class PopupListener extends MouseAdapter {
    JPopupMenu popup;
    QueryEditorPanel panel;
    
    public PopupListener(QueryEditorPanel panel, JPopupMenu popupMenu) {
      this.popup = popupMenu;
      this.panel = panel;
    }
    
    public void mousePressed(MouseEvent e) {
      maybeShowPopup( e );
    }

    public void mouseReleased(MouseEvent e) {
      maybeShowPopup( e );
    }

    private void maybeShowPopup(MouseEvent e) {
      if (e.isPopupTrigger()) {
        panel.mouseX = e.getX();
        panel.mouseY = e.getY();
        popup.show( e.getComponent(), e.getX(), e.getY() );
      }
    }
  }

  /*
   * @Override public void paint(Graphics gr) { //Graphics2D g = (Graphics2D)
   * gr; }
   */

  @Override
  public void mouseClicked(MouseEvent arg0) {
    // if we actually get this click, we didn't click a node
    selectNode( null );
  }

  @Override
  public void mouseEntered(MouseEvent arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void mouseExited(MouseEvent arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void mousePressed(MouseEvent arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void mouseReleased(MouseEvent arg0) {
    // TODO Auto-generated method stub
    
  }
}
