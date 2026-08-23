package adt;

public class HashMap<K, V> implements MapInterface<K, V> {
    private ListInterface<Entry<K, V>> entries = new LinkedList<>();
    
    private static class Entry<K, V> {
        K key; 
        V value;
        Entry(K k, V v) { key = k; value = v; }
    }
    
    @Override
    public void put(K key, V value) {
        for (int i = 1; i <= entries.getNumberOfEntries(); i++) {
            Entry<K, V> e = entries.getEntry(i);
            if (e.key.equals(key)) {
                e.value = value;
                return;
            }
        }
        entries.add(new Entry<>(key, value));
    }
    
    @Override
    public V get(K key) {
        for (int i = 1; i <= entries.getNumberOfEntries(); i++) {
            Entry<K, V> e = entries.getEntry(i);
            if (e.key.equals(key)) {
                return e.value;
            }
        }
        return null;
    }
    
    @Override
    public boolean containsKey(K key) {
        return get(key) != null;
    }
    
    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }
    
    @Override
    public int size() {
        return entries.getNumberOfEntries();
    }
    @Override
    public ListInterface<V> values() {
        ListInterface<V> vals = new LinkedList<>();
        for(int i=1; i<=entries.getNumberOfEntries(); i++) vals.add(entries.getEntry(i).value);
        return vals;
    }
    @Override
    public ListInterface<K> keys() {
        ListInterface<K> ks = new LinkedList<>();
        for(int i=1; i<=entries.getNumberOfEntries(); i++) ks.add(entries.getEntry(i).key);
        return ks;
    }
}
