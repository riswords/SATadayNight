package collections;

public interface IQueue<T> {

    public void insert(T t);

    public T dequeue();

    public void clear();

    public int size();
}
