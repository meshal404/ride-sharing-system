public interface List<T> {
    void findFirst();
    void findNext();
    T retrieve();
    void update(T e);
    void insert(T e);
    void remove();
    boolean full();
    boolean empty();
    boolean last();
}
