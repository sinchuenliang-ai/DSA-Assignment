package adt;

import java.io.Serializable;

/**
 *
 * @author Clement Chow Quan Liang
 */
public class BinarySearchTreeADT<T extends Comparable<T>> implements TreeInterface<T>, Serializable {

  private class Node implements Serializable {
    private T data;
    private Node left;
    private Node right;

    public Node(T data) {
      this.data = data;
      this.left = null;
      this.right = null;
    }
  }

  private Node root;
  private int size;

  public BinarySearchTreeADT() {
    root = null;
    size = 0;
  }

  @Override
  public boolean isEmpty() {
    return root == null;
  }

  @Override
  public int getSize() {
    return size;
  }

  @Override
  public boolean add(T entry) {
    int initialSize = size;
    root = addRecursive(root, entry);
    return size > initialSize;
  }

  private Node addRecursive(Node current, T entry) {
    if (current == null) {
      size++;
      return new Node(entry);
    }

    int comp = entry.compareTo(current.data);
    if (comp < 0) {
      current.left = addRecursive(current.left, entry);
    } else if (comp > 0) {
      current.right = addRecursive(current.right, entry);
    }
    return current;
  }

  @Override
  public T search(T entry) {
    return searchRecursive(root, entry);
  }

  private T searchRecursive(Node current, T entry) {
    if (current == null) {
      return null;
    }

    int comp = entry.compareTo(current.data);
    if (comp == 0) {
      return current.data;
    } else if (comp < 0) {
      return searchRecursive(current.left, entry);
    } else {
      return searchRecursive(current.right, entry);
    }
  }

  @Override
  public Iterator<T> getInorderIterator() {
    return new InorderIterator();
  }

  private class InorderIterator implements Iterator<T> {
    private Object[] elements;
    private int currentIndex = 0;
    private int count = 0;

    public InorderIterator() {
      elements = new Object[size];
      inorder(root);
    }

    private void inorder(Node node) {
      if (node != null) {
        inorder(node.left);
        elements[count++] = node.data;
        inorder(node.right);
      }
    }

    @Override
    public boolean hasNext() {
      return currentIndex < count;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T next() {
      if (!hasNext()) {
        throw new RuntimeException("No more elements");
      }
      return (T) elements[currentIndex++];
    }
    
  }
  
  @Override
  public boolean remove(T entry) {
    int initialSize = size;
    root = removeRecursive(root, entry);
    return size < initialSize;
  }

  private Node removeRecursive(Node current, T entry) {
    if (current == null) {
      return null;
    }
    int comp = entry.compareTo(current.data);
    if (comp < 0) {
      current.left = removeRecursive(current.left, entry);
    } else if (comp > 0) {
      current.right = removeRecursive(current.right, entry);
    } else {
      // Node to delete found
      size--;
      if (current.left == null) return current.right;
      if (current.right == null) return current.left;
      
      // Node with two children: Get inorder successor
      current.data = findSmallestValue(current.right);
      current.right = removeRecursive(current.right, current.data);
      size++; // Adjust size because recursive call decremented it
    }
    return current;
  }

  private T findSmallestValue(Node root) {
    return root.left == null ? root.data : findSmallestValue(root.left);
  }
}
