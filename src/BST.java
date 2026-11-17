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
        BSTNode<K, T> p = root, q = root;

        if(empty())
            return false;
        
        while(p != null)
        {
            q = p;
            int cmp = key.compareTo(p.key);

            if(cmp == 0)
            {
                current = p;
                return true;
            }

            else if(cmp < 0)
            {
                p = p.left;
            }

            else
            {
                p = p.right;
            }
        }

        current = q;
        return false;
    }

    public T retrieve()
    {
        return current.data;
    }
        
    public boolean insert(K key, T data) 
    {
        BSTNode<K, T> q = current;

        if(findKey(key))
        {
            current = q;
            return false; //key already in the BST
        }

        BSTNode<K, T> p = new BSTNode<K, T>(key, data);

        if(empty())
        {
            root = current = p;
            count++;
            return true;
        }

        else
        {
            int cmp = key.compareTo(current.key);

            // current is pointing to parent of the new key
            if(cmp < 0)
                current.left = p;

            else
                current.right = p;

            current = p;
            count++;
            return true;
        }
    }

    public boolean removeKey(K key) 
    {
        BSTNode<K, T> p = root;
        BSTNode<K, T> q = null;
        boolean found = false;

        // Search for the node containing the key while tracking its parent (q)
        while (p != null && !found) 
        {
            int cmp = key.compareTo(p.key);

            if (cmp == 0) 
            {
                found = true;
            } 

            else 
            {
                q = p;

                if (cmp < 0)
                    p = p.left;

                else 
                    p = p.right;
                
            }
        }

        if (found) 
        {
            // Check the three cases

            // Case 3: two children
            if ((p.left != null) && (p.right != null)) 
            { 
                // Search for the min in the right subtree
                BSTNode<K, T> min = p.right;
                q = p;
                while (min.left != null) 
                {
                    q = min;
                    min = min.left;
                }
                p.key = min.key;
                p.data = min.data;
                deleteNode(min, q);
            } 

            // Case 1 and 2
            else 
            { 
                deleteNode(p, q);
            }
            
            current = root;
            count--;
            return true;
        }
        
        return false; // Not found
    }

    private void deleteNode(BSTNode<K, T> n, BSTNode<K, T> parent) 
    {
        // parent is the parent of n. The method deletes node n (case 1 and 2)
        BSTNode<K, T> child;
        
        if (n.left != null)
            child = n.left;
        else
            child = n.right; // If both are null, child will be null (Case 1)


        if (parent == null)  // No parent for p, root must change
            root = child;
        
        else 
        {
            // Use compareTo instead of subtraction for generic keys
            if (n.key.compareTo(parent.key) < 0) 
                parent.left = child;

            else 
                parent.right = child;
            
        }
    }
 
    public LinkedList<T> inOrder() 
    {
        LinkedList<T> result = new LinkedList<T>();
        travInOrder(root, result);
        return result;
    }

    private void travInOrder(BSTNode<K, T> n, LinkedList<T> result)
    {
        //base case
        if(n == null)
            return;

        travInOrder(n.left, result);    //1. left subtree
        result.insert(n.data);          //2. current node
        travInOrder(n.right, result);   //3. right subtree
    }
 
    public LinkedList<T> rangeSearch(K low, K high) 
    {
        LinkedList<T> result = new LinkedList<T>();
        rangeSearchHelper(root, low, high, result);
        return result;
    }
 
    private void rangeSearchHelper(BSTNode<K,T> n, K low, K high, LinkedList<T> result) 
    {
        if(n == null)
            return;

        //go to left subtree if n.key is above low
        if(low.compareTo(n.key) < 0)
            rangeSearchHelper(n.left, low, high, result);

        //current node
        if(low.compareTo(n.key) <= 0 && n.key.compareTo(high) <= 0)
            result.insert(n.data);

        //go to right subtree if n.key is below high
        if(n.key.compareTo(high) < 0)
            rangeSearchHelper(n.right, low, high, result);
    }

    public int height() 
    {
        return heightHelper(root);
    }

    private int heightHelper(BSTNode<K, T> n)
    {
        //base case
        if(n == null)
            return 0;

        int leftHeight = heightHelper(n.left);
        int rightHeight = heightHelper(n.right);

        return 1 + Math.max(leftHeight, rightHeight);
    }
 
    public int nbKeyComp(K key) 
    {
        int num = 0;
        BSTNode<K, T> p = root;

        while(p != null)
        {
            num++;
            int cmp = key.compareTo(p.key);

            if(cmp == 0)
                return num; // found

            else if(cmp < 0)
                p = p.left;

            else
                p = p.right;
        }

        return num; // not found
    }
}
