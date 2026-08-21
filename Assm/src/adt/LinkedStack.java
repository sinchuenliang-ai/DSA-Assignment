package adt;

public class LinkedStack<T> implements StackInterface<T> {
    
    private Node topNode;
    private int numberOfEntries;

    private class Node {
        private T data;
        private Node next;

        private Node(T data, Node next) {
            this.data = data;
            this.next = next;
        }
    }

    public LinkedStack() {
        topNode = null;
        numberOfEntries = 0;
    }

    @Override
    public void push(T newEntry) {
        Node newNode = new Node(newEntry, topNode);
        topNode = newNode;
        numberOfEntries++;
    }

    @Override
    public T pop() {
        if (isEmpty()) {
            return null;
        }
        T result = topNode.data;
        topNode = topNode.next;
        numberOfEntries--;
        return result;
    }

    @Override
    public T peek() {
        if (isEmpty()) {
            return null;
        }
        return topNode.data;
    }

    @Override
    public boolean isEmpty() {
        return topNode == null;
    }

    @Override
    public int size() {
        return numberOfEntries;
    }

    @Override
    public void clear() {
        topNode = null;
        numberOfEntries = 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Node current = topNode;
        while (current != null) {
            sb.append(current.data);
            if (current.next != null) {
                sb.append("\n");
            }
            current = current.next;
        }
        return sb.toString();
    }
}