package adt;

import java.util.Iterator;

/**
 *
 * @author Clement Chow Quan Liang
 */
public interface TreeInterface<T extends Comparable<T>> {

  public boolean add(T newEntry);

  public T search(T entry);

  public boolean isEmpty();

  public int getSize();

  public Iterator<T> getInorderIterator();

  public boolean remove(T entry);
}
