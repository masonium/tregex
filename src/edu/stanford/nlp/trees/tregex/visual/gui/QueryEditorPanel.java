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
import java.awt.geom.Rectangle2D;

import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import edu.stanford.nlp.trees.tregex.visual.QueryGraph;

import net.java.swingfx.jdraggable.DefaultDraggableManager;
import net.java.swingfx.jdraggable.DragPolicy;

public class QueryEditorPanel extends JPanel implements ActionListener {

  /**
   * 
   */
  private static final long serialVersionUID = 3495240357289682684L;

  DefaultDraggableManager dm;

  // Edge popup menu
  
  // Node popup menu
  JPopupMenu popupMenu;
  JMenuItem addNodeItem;
  JMenuItem addEdgeItem;

  // underlying query graph
  QueryGraph queryGraph;
  int mouseX, mouseY;
  
  public QueryEditorPanel() {
    this.setLayout( new BoxLayout(this, BoxLayout.Y_AXIS) );
    
    dm = new DefaultDraggableManager( this );
    dm.setDragPolicy( DragPolicy.STRICT );
    
    initializePopupMenu();
    this.add( new Node("Hello") );
    this.add( new Node("world!") );

    queryGraph = new QueryGraph();
  }

  private void initializePopupMenu() {
    // add node button
    popupMenu = new JPopupMenu();
    popupMenu.add( addNodeItem = new JMenuItem("Add Node") );
    addNodeItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_N, ActionEvent.CTRL_MASK ) );
    addNodeItem.addActionListener( this );
    popupMenu.addSeparator();
    popupMenu.add( addEdgeItem = new JMenuItem("Add Edge") );
    addEdgeItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_E, ActionEvent.CTRL_MASK ) );
    addEdgeItem.addActionListener( this );
    
    this.add( popupMenu );
    this.addMouseListener( new PopupListener(this, popupMenu) );
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    Object source = arg0.getSource();
    if (source == addNodeItem) {
      System.out.print( "(" + mouseX + ", " + mouseY + ") " );
      Node n = new Node("abcd");
      n.setLocation( mouseX, mouseY );
      this.add( n );
      this.repaint();
      System.out.println( this.getComponentCount() );
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
}
