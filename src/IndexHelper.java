public class IndexHelper 
{

    // Not instantiable: only static helpers
    private IndexHelper() {}

    // Adds a value under the given outer key. If the key is new, a new inner
    // BST is created for it.
    // return false (no change) if the inner BST already holds this id
    public static <K extends Comparable<K>, V> boolean addToIndex(IBST<K, IBST<Integer, V>> index, K key, int id, V value) 
    {
        if (index.findKey(key)) 
        {
            return index.retrieve().insert(id, value);
        }

        IBST<Integer, V> inner = new BST<Integer, V>();
        inner.insert(id, value);
        return index.insert(key, inner);
    }

    // Removes the value with this id from the inner BST of the given key.
    // If that leaves the inner BST empty, the key is removed from the outer BST.
    // return false if the key or the id is not in the index
    public static <K extends Comparable<K>, V> boolean removeFromIndex(IBST<K, IBST<Integer, V>> index, K key, int id) 
    {
        if (!index.findKey(key)) 
            return false;
        
        IBST<Integer, V> inner = index.retrieve();

        if (!inner.removeKey(id)) 
            return false;
        
        if (inner.empty()) 
            index.removeKey(key);
        
        return true;
    }

    // Returns every value stored under the key, sorted by id (an in-order
    // traversal of the inner BST). Returns an empty list if the key is missing.
    public static <K extends Comparable<K>, V> LinkedList<V> getAll(IBST<K, IBST<Integer, V>> index, K key) 
    {
        if (!index.findKey(key)) 
            return new LinkedList<V>();
        
        return index.retrieve().inOrder();
    }
}