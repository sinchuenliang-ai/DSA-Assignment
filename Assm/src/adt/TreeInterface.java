package adt;

import java.util.Iterator;

/**
 * Interface for Tree ADT operations.
 *
 * @param <T>
 */
public interface TreeInterface<T extends Comparable<T>> {

  public boolean add(T newEntry);

  public T search(T entry);

  public boolean isEmpty();

  public int getSize();

  public Iterator<T> getInorderIterator();
}