package edu.stanford.nlp.trees.tregex.visual.gui;

import java.awt.FlowLayout;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class TextQueryPanel extends JPanel {

  private static final long serialVersionUID = 1610762501069495667L;

  public final static String COPY_QUERY = "Copy Query to Box";
  
  JLabel queryLabel;
  JTextField textQuery;
  JButton copyButton;
  
  public TextQueryPanel() {
    setLayout( new FlowLayout() );
    this.add( queryLabel = new JLabel("Text Query: ") );
    this.add( textQuery = new JTextField(50) );
    textQuery.setEnabled( true );
    textQuery.setEditable( false );
    this.add( copyButton = new JButton("Copy to Search Box") );
    copyButton.setActionCommand( COPY_QUERY );
  }
  
  public void setQuery( String query ) {
    textQuery.setText( query );
  }
  
  public void addActionListener( ActionListener listener ) {
    copyButton.addActionListener( listener );
  }

  public String getQuery() {
    return textQuery.getText();
  }
}
