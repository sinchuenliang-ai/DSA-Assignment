package adt;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Custom Binary Search Tree implementation for non-linear searching and sorting.
 *
 * @param <T> Comparable element type
 * @author Front Desk Team
 */
public class BinarySearchTreeADT<T extends Comparable<T>> {

    private class Node {
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

    public boolean isEmpty() {
        return root == null;
    }

    public int getSize() {
        return size;
    }

    public void add(T entry) {
        root = addRecursive(root, entry);
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
        return current; // Duplicates ignored for key fields
    }

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

    // In-order traversal iterator yielding elements in sorted order
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
                throw new NoSuchElementException();
            }
            return (T) elements[currentIndex++];
        }
    }
}