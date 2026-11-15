public class BST<K extends Comparable<K>, T> implements IBST<K, T>
{
    BSTNode<K, T> root, current;
    int count;
    
    public BST()
    {
        root = current = null;
        count = 0;
    }

    public boolean empty()
    {
        return root == null;
    }

    public int size() 
    {
        return count;
    }

    public boolean findKey(K key)
    {
        throw new UnsupportedOperationException("TODO: BST.findKey");
    }

    public T retrieve()
    {
        return current.data;
    }
        
    public boolean insert(K key, T data) 
    {
        throw new UnsupportedOperationException("TODO: BST.insert");
    }

    public boolean removeKey(K key) 
    {
        throw new UnsupportedOperationException("TODO: BST.removeKey");
    }
 
    public LinkedList<T> inOrder() 
    {
        throw new UnsupportedOperationException("TODO: BST.inOrder");
    }
 
    public LinkedList<T> rangeSearch(K low, K high) 
    {
        throw new UnsupportedOperationException("TODO: BST.rangeSearch");
    }
 
    public int height() 
    {
        throw new UnsupportedOperationException("TODO: BST.height");
    }
 
    public int nbKeyComp(K key) 
    {
        throw new UnsupportedOperationException("TODO: BST.nbKeyComp");
    }
}
