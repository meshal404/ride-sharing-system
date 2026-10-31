/*
  A custom generic singly linked list (the data structure all other
  classes in the project are built on; java.util collections are not used).
 
  The list keeps both a head and a tail reference, so adding at the end
  is O(1). Callers can walk the list node by node through getHead() and
  Node.getNext(), and can insert or remove at a known position in O(1)
  through insertAfter and removeAfter.
 
 */
public class LinkedList<T> {

    /* A single node of the list: one element plus a link to the next node. */
    public static class Node<T> {
        private T data;
        private Node<T> next;

        private Node(T data) {
            this.data = data;
        }

        /* Returns the element stored in this node. */
        public T getData() {
            return data;
        }

        /* Returns the next node, or null if this is the last node. */
        public Node<T> getNext() {
            return next;
        }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    /* Creates an empty list. */
    public LinkedList() {
        head = null;
        tail = null;
        size = 0;
    }

    /* Returns the first node of the list, or null if the list is empty. */
    public Node<T> getHead() {
        return head;
    }

    /* Returns the number of elements in the list. */
    public int size() {
        return size;
    }

    /* Returns true if the list has no elements. */
    public boolean isEmpty() {
        return size == 0;
    }

    /* Inserts an element at the front of the list. O(1). */
    public void addFirst(T data) {
        Node<T> node = new Node<>(data);
        node.next = head;
        head = node;
        if (tail == null) {
            tail = node;
        }
        size++;
    }

    /* Inserts an element at the end of the list. O(1). */
    public void addLast(T data) {
        Node<T> node = new Node<>(data);
        if (tail == null) {
            head = node;
        } else {
            tail.next = node;
        }
        tail = node;
        size++;
    }

    /* Same as addLast. */
    public void add(T data) {
        addLast(data);
    }

    /*
      Inserts an element right after the given node. If prev is null the
      element becomes the new head. O(1).
     */
    public void insertAfter(Node<T> prev, T data) {
        if (prev == null) {
            addFirst(data);
            return;
        }
        Node<T> node = new Node<>(data);
        node.next = prev.next;
        prev.next = node;
        if (prev == tail) {
            tail = node;
        }
        size++;
    }

    /*
      Removes the node that follows prev (or the head if prev is null) and
      returns its element, or null if there is no such node. O(1).
     */
    public T removeAfter(Node<T> prev) {
        Node<T> target = (prev == null) ? head : prev.next;
        if (target == null) {
            return null;
        }
        if (prev == null) {
            head = target.next;
        } else {
            prev.next = target.next;
        }
        if (target == tail) {
            tail = prev;
        }
        size--;
        return target.data;
    }

    /*
      Removes the first element that equals the given item (using equals).
      Returns true if an element was removed. O(n).
     */
    public boolean remove(T item) {
        Node<T> prev = null;
        Node<T> cur = head;
        while (cur != null) {
            if (cur.data == item || (cur.data != null && cur.data.equals(item))) {
                removeAfter(prev);
                return true;
            }
            prev = cur;
            cur = cur.next;
        }
        return false;
    }

    /* Returns true if the list contains an element equal to the item. O(n). */
    public boolean contains(T item) {
        Node<T> cur = head;
        while (cur != null) {
            if (cur.data == item || (cur.data != null && cur.data.equals(item))) {
                return true;
            }
            cur = cur.next;
        }
        return false;
    }
}
