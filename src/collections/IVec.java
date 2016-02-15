package collections;

import java.util.Comparator;

public interface IVec<T> {
    /**
     * Return the number of elements in the vector.
     */
    public int size();

    /**
     * Remove the last {@code numElements} elements from the vector.
     */
    public void shrinkBy(int numElements);

    /**
     * Remove the last element from the vector.
     */
    public void pop();

    /**
     * Increase the size of the vector to {@code size}, padding with the default value.
     */
    public void growTo(int size);

    /**
     * Increase the size of the vector to {@code size}, padding with {@code pad}.
     */
    public void growTo(int size, T pad);

    /**
     * Remove all elements from the vector.
     */
    public void clear();

    /**
     * Push the default element into the vector (at the end).
     */
    public void push();

    /**
     * Push a specific element into the vector (at the end).
     */
    public void push(T elem);

    /**
     * Return the last element in the vector.
     */
    public T last();

    /**
     * Return the element at index {@code index}.
     */
    public T get(int index);

    /**
     * Copy contents of this vector (and relevant underlying variables) to {@code copy}.
     * May throw an {@code UnsupportedOperationException} if the copy destination is not of the same type.
     */
    public void copyTo(IVec<T> copy);

    /**
     * Move the contents of this vector to the destination {@code dest}. 
     * Per paper, should complete in constant time and also clear out the contents of the source vector.
     */
    public void moveTo(IVec<T> dest);

    /**
     * Remove the first occurrence of element {@code element} from the vector and return {@code true}. 
     * Return {@code false} if the element is not found. 
     */
    public boolean remove(T element);
    
    /**
     * Remove the element at index {@code index} from the vector. 
     */
    public void removeAtIndex(int index);

    /**
     * Set the element at index {@code index} to be {@code element}.
     */
    public void set(int index, T element);
    
    /**
     * Sort using a comparator.
     */
    public void sort(Comparator<T> comparator);
}
