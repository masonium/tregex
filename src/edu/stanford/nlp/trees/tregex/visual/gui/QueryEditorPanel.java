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
import java.awt.geom.AffineTransform;
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

  private static final long serialVersionUID = 3495240357289682684L;

  // event types produced by this component
  public static final String NODE_SELECTED = "Node Selected";
  public static final String NODE_DESELECTED = "Node Deselected";
  public static final String EDGE_SELECTED = "Edge Selected";
  public static final String EDGE_DESELECTED = "Edge Deselected";
  //public static final String NODE_DELETED = "Node Deleted";
  //public static final String EDGE_DELETED = "Edge Deleted";
  public static final String GRAPH_CHANGED = "Graph Changed";
  
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
  Selectable selectedItem;
  Node headNode;
  List<Edge> graphEdges;
  
  boolean choosingSecond = false;
  Node firstNode;
  Node targetNode;
  Edge targetEdge;
  
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
    //this.addMouseListener( new PopupListener(this, popupMenu) );
    
    // node menu
    nodeMenu = new JPopupMenu();
    nodeMenu.add( deleteNodeItem = new JMenuItem("Delete Node") );
    deleteNodeItem.addActionListener( this );
    nodeMenu.add( addEdgeItem = new JMenuItem("Add Edge") );
    addEdgeItem.addActionListener( this );
    
    // edge menu
    edgeMenu = new JPopupMenu();
    edgeMenu.add( flipEdgeItem = new JMenuItem("Flip Edge") );
    flipEdgeItem.addActionListener( this );
    edgeMenu.add(  deleteEdgeItem = new JMenuItem("Delete Edge") );
    deleteEdgeItem.addActionListener( this );
    
    //updatePopupMenu();
  }
  
  private void updatePopupMenu() {
    addEdgeItem.setEnabled( queryGraph.getNumNodes() > 1 );
    if (targetNode != null)
      deleteNodeItem.setEnabled( !targetNode.isHeadNode() );
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
      if ( targetNode != null && targetNode != headNode ) {
        deleteNode( targetNode );
      }
    }
    // adding an edge
    if ( source == addEdgeItem ) {
      // switch to second-node state
      choosingSecond = true;
      firstNode = targetNode;
    }
    // delete edge
    if (source == deleteEdgeItem) {
      if (targetEdge != null)
        deleteGraphEdge( targetEdge );
    }
    if (source == flipEdgeItem) {
      if (targetEdge != null) {
        flipEdge( targetEdge );
        targetEdge = null;
      }
    }
  }  
  
  private void flipEdge(Edge edge) {
    // delete the edge
    deleteGraphEdge( edge );
    
    // create a new edge in the reverse direction
    Edge newEdge = createEdge( edge.n2, edge.n1, false );
    
    // copy over the properties
    newEdge.getQueryEdge().cloneDescriptor( edge.getQueryEdge().getDescriptor() );
    
    fireEvent( newEdge, GRAPH_CHANGED );
  }

  public void deleteNode( Node node ) {
    deleteNode( node, false );
  }
  
  /**
   * deletes the selected node, usually 
   * @param node
   * @param b 
   */
  public void deleteNode( Node node, boolean forceHeadNodeDeletion ) {
    assert( node != null );
    if ( node == headNode && !forceHeadNodeDeletion )
      return;
    
    // delete all of the connected edges
    List<QueryEdge> adjacentEdges = node.getQueryNode().getEdges();
    for ( QueryEdge e: adjacentEdges )
      deleteGraphEdge( e.getOwner() );
      
    // deselect the node if already selected
    if ( node == selectedItem )
      selectNode( null );
    
    // delete the node from the query graph
    queryGraph.removeNode( node.getQueryNode() );
    
    // remove the component from the panel
    this.remove( node );
    
    // signal deletion
    fireEvent( node, GRAPH_CHANGED );
  }
  
  private void deleteGraphEdge(Edge edge) {
    // make sure we aren't deleting a null edge
    assert( edge != null );
    assert( edge.type == Edge.Type.GRAPH_EDGE );
    
    // deselect if necessary
    if (edge == selectedItem)
      selectEdge( null );
    
    queryGraph.removeEdge( edge.getQueryEdge() );
    graphEdges.remove( edge );
    
    // signal deletion
    fireEvent( edge, GRAPH_CHANGED );
  }

  public void selectItem( Selectable item, String selectEvent, String deselectEvent ) {
    // don't do anything if the item is already selected
    if ( item == selectedItem )
      return;
    
    // deselect the currently selected item
    if (this.selectedItem != null) {
      selectedItem.setSelected( false );
      fireEvent( selectedItem, deselectEvent );
    }
    
    selectedItem = item;

    // repaint
    this.repaint();
    
    if (item == null)
      return;
    
    // select and store the item
    item.setSelected( true );    
      
    // tell interested parties that we've done this
    fireEvent( item, selectEvent );
  }
  
  public void selectNode( Node nodeToSelect ) {
    selectItem( nodeToSelect, NODE_SELECTED, NODE_DESELECTED );
  }
  public void selectEdge( Edge edgeToSelect ) {
    selectItem( edgeToSelect, EDGE_SELECTED, EDGE_DESELECTED );
  }
  
  /**
   * Create a node at the most recent popup location
   */  
  private void createNode(int x, int y) {
    QueryNode qn = queryGraph.createNode();
    Node node = new Node( this, qn );

    // make it the head node if it is currently the only node
    if ( queryGraph.getNumNodes() == 1 ) {
      node.setHeadNode( true );
      setHeadNode( node );
    }
    
    // place on the screen
    node.setLocation( x, y );
    this.add( node );
    this.repaint();
    
    // add the popup
    NodeListener listener = new NodeListener(node, this);
    node.addMouseListener( listener );
    node.addMouseMotionListener( listener );
    
    // select this new node
    selectNode( node );
    fireEvent( node, GRAPH_CHANGED );
    
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
    createEdge( n1, n2, true );
  }
  
  private Edge createEdge( Node n1, Node n2, boolean select ) {
    QueryEdge qe = queryGraph.createEdge(n1.getQueryNode(), n2.getQueryNode());
    if (qe == null)
      return null;
    
    Edge edge = new Edge(n1, n2, qe);
    graphEdges.add( edge );
    
    // select the edge after creating it
    if (select)
      selectEdge( edge );
    
    fireEvent( edge, GRAPH_CHANGED );
        
    System.out.println( "Created edge" );
    return edge;
  }
  
  @Override
  public void paintComponent( Graphics g ) {
    super.paintComponent( g );
    
    // render all of the edges
    for ( Edge e: graphEdges )
      e.render( g );
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
          // grab the selecting node
          Node from = panel.firstNode;

          panel.choosingSecond = false;
          
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
        panel.targetNode = target;
        updatePopupMenu( );
        panel.nodeMenu.show( e.getComponent(), e.getX(), e.getY() );
      }
    }
  }
    
  /*class PopupListener extends MouseAdapter {
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
  }*/

  @Override
  public void mouseClicked(MouseEvent arg0) {

    Edge s = edgeHit( arg0.getX(), arg0.getY() );
    
    if (arg0.isPopupTrigger())
      showCorrectPopup( s, arg0 );
    else {
      // if we actually get this click, we didn't click a node
      selectNode( null );
      // select whatever edge we did hit, if any
      selectEdge( s );
    }
  }

  @Override
  public void mousePressed(MouseEvent arg0) {
    if (arg0.isPopupTrigger())
      showCorrectPopup( edgeHit( arg0.getX(), arg0.getY() ), arg0 );
  }

  @Override
  public void mouseReleased(MouseEvent arg0) {
    if (arg0.isPopupTrigger())
      showCorrectPopup( edgeHit( arg0.getX(), arg0.getY() ), arg0 );    
  }

  @Override
  public void mouseEntered(MouseEvent arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void mouseExited(MouseEvent arg0) {
    // TODO Auto-generated method stub
    
  }
  
  private void showCorrectPopup( Edge edge, MouseEvent arg0 ) {
    if (edge != null) {
      targetEdge = edge;
      edgeMenu.show( arg0.getComponent(), arg0.getX(), arg0.getY() );
    }
    else {
      mouseX = arg0.getX();
      mouseY = arg0.getY();
      popupMenu.show( arg0.getComponent(), arg0.getX(), arg0.getY() );
    }
  }
  
  private Edge edgeHit(int x, int y) {
    // check to see if any of the edges were selected
    for ( Edge e: graphEdges )
      if ( e.clickedOn( x, y ) )
        return e;
    return null;
  }

  /*
   * Get the text query represented by the underlying graph
   */
  public String getQuery() {
    if (queryGraph.getNumNodes() == 0)
      return "";
    
    return queryGraph.toTregexQuery( headNode.getQueryNode() );
  }

  public void clearGraph() {
    List<QueryNode> nodeList = queryGraph.getNodes();
    for (QueryNode n: nodeList)
      deleteNode( n.getOwner(), true );    
  }
}
