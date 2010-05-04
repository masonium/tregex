package edu.stanford.nlp.trees.tregex.visual;

public abstract class Ownable<T> {
  T owner;
  
  public void setOwner(T owner) {
    this.owner = owner;
  }
  
  public T getOwner() {
    return owner;
  }
}
