package adt;

public interface MapInterface<K, V> {
    void put(K key, V value);
    V get(K key);
    boolean containsKey(K key);
    boolean isEmpty();
    int size();
    ListInterface<V> values();
    ListInterface<K> keys();
}
