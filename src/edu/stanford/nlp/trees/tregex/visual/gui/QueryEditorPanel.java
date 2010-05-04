package edu.stanford.nlp.trees.tregex.visual.gui;

import java.awt.Color;
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
import java.awt.event.MouseMotionListener;
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

import edu.stanford.nlp.trees.tregex.visual.QueryEdge;
import edu.stanford.nlp.trees.tregex.visual.QueryGraph;
import edu.stanford.nlp.trees.tregex.visual.QueryNode;

import net.java.swingfx.jdraggable.DefaultDraggableManager;
import net.java.swingfx.jdraggable.DragPolicy;
import net.java.swingfx.jdraggable.DraggableListener;

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
  JPopupMenu edgeMenu;
  JMenuItem flipEdgeItem;
  JMenuItem deleteEdgeItem;
  
  // Node popup menu
  JPopupMenu nodeMenu;
  JMenuItem addEdgeItem;
  JMenuItem deleteNodeItem;

  // popup menu
  JPopupMenu popupMenu;
  JMenuItem addNodeItem;

  // underlying query graph
  QueryGraph queryGraph;
  int mouseX, mouseY;

  // state
  Node selectedNode;
  Node headNode;
  List<Edge> graphEdges;
  Edge selectedEdge;
  
  boolean choosingSecond = false;
  Node firstNode;
  Node popupTarget;
  
  public QueryEditorPanel() {
    this.setLayout( null );
    
    dm = new DefaultDraggableManager( this );
    dm.setDragPolicy( DragPolicy.STRICT );
    
    initializePopupMenu();

    queryGraph = new QueryGraph();
    graphEdges = new ArrayList<Edge>();
    
    this.addMouseListener( this );
  }

  private void initializePopupMenu() {
    popupMenu = new JPopupMenu();
    popupMenu.add( addNodeItem = new JMenuItem("Add Node") );
    //addNodeItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_N, ActionEvent.CTRL_MASK ) );
    addNodeItem.addActionListener( this );
    this.add( popupMenu );
    this.addMouseListener( new PopupListener(this, popupMenu) );
    
    // node menu
    nodeMenu = new JPopupMenu();
    nodeMenu.add( deleteNodeItem = new JMenuItem("Delete Node") );
    deleteNodeItem.addActionListener( this );
    nodeMenu.add( addEdgeItem = new JMenuItem("Add Edge") );
    addEdgeItem.addActionListener( this );
    
    //updatePopupMenu();
  }
  
  private void updatePopupMenu() {
    addEdgeItem.setEnabled( queryGraph.getNumNodes() > 1 );
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
      if ( popupTarget != null && popupTarget != headNode ) {
        deleteNode( popupTarget );
      }
    }

    // adding an edge
    if ( source == addEdgeItem ) {
      // switch to second-node state
      choosingSecond = true;
      firstNode = popupTarget;
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
    
    // deselect the node if already selected
    if ( node == selectedNode )
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
  private void createNode(int x, int y) {
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
    NodeListener listener = new NodeListener(n, this);
    n.addMouseListener( listener );
    n.addMouseMotionListener( listener );
    
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
  
  private void createEdge( Node n1, Node n2 ) {
    // create the edge add to list
    QueryEdge qe = queryGraph.createEdge(n1.getQueryNode(), n2.getQueryNode());
    Edge edge = new Edge(n1, n2, qe);
    graphEdges.add( edge );
    
    // select the edge
    //selectEdge( edge );
    
    // repaint
    this.repaint();
    
    System.out.println( "Created edge" );
  }
  
  @Override
  public void paintComponent( Graphics g ) {
    super.paintComponent( g );
    
    // render all of the edges
    for ( Edge e: graphEdges )
      e.render( g );
    
    Graphics2D g2 = (Graphics2D)g;
    g2.setColor( Color.BLACK );
    g2.drawLine( 100, 100, 300, 300 );
  }
  /*
  private void selectEdge(Edge edge) {
    // unselect any currently-selected nodes
    if (selectedEdge != null) {
      selectedEdge.setSelected( false );
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
  }*/

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
    @Override
    public void mouseMoved(MouseEvent e) {
      if ( panel.dm.dragging() )
        this.panel.repaint();
    }
    @Override
    public void mouseDragged(MouseEvent e) {
      if ( panel.dm.dragging() )
        this.panel.repaint();
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
      // select this node if it's not a right click
      if (!e.isPopupTrigger()) {
        // if we're choosing a second node to create an edge,
        // and it's not the same as this one
        if (panel.choosingSecond) {
          panel.choosingSecond = false;
          
          // grab the selecting node
          Node from = panel.firstNode;
          
          // create the edge
          panel.createEdge( from, target );
          
        }
        else {
          // otherwise, just select the target
          panel.selectNode( target );
        }
      }
      
      maybeShowPopup( e );
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
      maybeShowPopup( e );
    }
    
    public void maybeShowPopup(MouseEvent e) {
      if (e.isPopupTrigger()) {
        updatePopupMenu( );
        panel.popupTarget = target;
        panel.nodeMenu.show( e.getComponent(), e.getX(), e.getY() );
      }
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
    
    // check to see if any of the edges were selected
    Edge s = null;
    for ( Edge e: graphEdges )
      if ( e.clickedOn( arg0.getX(), arg0.getY() ) ) {
        s = e;
        break;
      }
    
    //selectEdge( s );
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
