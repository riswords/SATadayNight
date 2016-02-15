package collections;

import java.util.Arrays;
import java.util.Comparator;

import collections.exception.IndexOutOfBoundsException;
import collections.exception.UnderflowException;
import exception.IllegalArgumentException;

public class SimpleVec<T> implements IVec<T> {

    private T[] vec;
    private int numElements = 0;

    public SimpleVec() {
        this(0);
    }

    @SuppressWarnings("unchecked")
    public SimpleVec(int size) {
        vec = (T[]) new Object[size];
        Arrays.fill(vec, null);
        numElements = size;
    }

    @Override
    public int size() {
        return numElements;
    }

    @Override
    public void shrinkBy(int numElementsToRemove) {
        if(numElements < numElementsToRemove)
            throw new IndexOutOfBoundsException("Size of vector (" + numElements + ") is less than " + 
                    numElementsToRemove);
        while(numElementsToRemove > 0) {
            pop();
            numElementsToRemove -= 1;
        }
    }

    @Override
    public void pop() {
        if(numElements < 1)
            throw new UnderflowException("Unable to pop from an empty vector.");
        numElements -= 1;
        vec[numElements] = null;
    }

    @Override
    public void growTo(int size) {
        growTo(size, null);
    }

    @Override
    public void growTo(int size, T pad) {
        if(size < numElements)
            throw new IllegalArgumentException("Vector already contains more than " + size + " elements.");
        ensureCapacity(size);
        Arrays.fill(vec, numElements, size, pad);
        numElements = size;
    }
    
    /**
     * Ensures that the underlying array can support at least size elements. Increases the size of the 
     * array if needed (padding with null), but does not change numElements.
     */
    private void ensureCapacity(int size) {
        if(size > vec.length) {
            // increase by at least 16 elements at a time so that repeated pushing isn't as inefficient
            int newSize = Math.max(size, vec.length + 16);
            int oldLength = vec.length;
            vec = Arrays.copyOf(vec, newSize);
            Arrays.fill(vec, oldLength, newSize, null);
        }
        assert(vec.length >= size) :
            "Unable to allocate " + size + "-element vector.";
    }

    @Override
    public void clear() {
        Arrays.fill(vec, 0, numElements, null);
        numElements = 0;
    }

    @Override
    public void push() {
        push(null);
    }

    @Override
    public void push(T elem) {
        ensureCapacity(numElements + 1);
        vec[numElements] = elem;
        numElements += 1;
    }

    @Override
    public T last() {
        return get(numElements - 1);
    }

    @Override
    public T get(int index) {
        if(index < 0 || index >= numElements)
            throw new IndexOutOfBoundsException(index);
        return vec[index];
    }

    @Override
    public void copyTo(IVec<T> copy) {
        if(copy instanceof SimpleVec) {
            SimpleVec<T> castCopy = (SimpleVec<T>)copy;
            castCopy.ensureCapacity(numElements);
            castCopy.numElements = numElements;
            System.arraycopy(vec, 0, castCopy.vec, 0, numElements);
        }
        else {
            throw new UnsupportedOperationException("Unable to copy SimpleVec to a different type of IVec.");
        }
    }

    @Override
    public void moveTo(IVec<T> dest) {
        copyTo(dest);
        clear();
    }

    @Override
    public boolean remove(Object element) {
        boolean found = false;
        for(int i=0; i<numElements; ++i) {
            if(found)
                vec[i-1] = vec[i];
            else if(vec[i].equals(element))
                found = true;
        }
        if(found) {
            numElements -= 1;
            vec[numElements] = null;
        }
        return found;
    }
    
    @Override
    public void removeAtIndex(int index) {
        if(index < 0 || index >= numElements)
            throw new IndexOutOfBoundsException(index);
        System.arraycopy(vec, index + 1, vec, index, numElements - (index + 1));
    }

    @Override
    public void set(int index, T element) {
        if(index < 0 || index >= numElements)
            throw new IndexOutOfBoundsException(index);
        vec[index] = element;
    }
    
    @Override
    public void sort(Comparator<T> comparator) {
        Arrays.sort(vec, comparator);
    }
    
    /*
     * Additional methods from the Collections interface
     
    
    @Override
    public boolean add(T e) {
        push(e);
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        for(T element : c) {
            push(element);
        }
        return true;
    }

    @Override
    public boolean contains(Object o) {
        for(int i=0; i<numElements; ++i) {
            if(get(i).equals(o))
                return true;
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for(Object o : c) {
            if(!contains(o))
                return false;
        }
        return true;
    }

    @Override
    public boolean isEmpty() {
        return numElements == 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int curIndex = -1;
            private boolean canRemove = false;
            
            @Override
            public boolean hasNext() {
                return (curIndex + 1) < numElements;
            }

            @Override
            public T next() {
                canRemove = true;
                curIndex += 1;
                return vec[curIndex];
            }
            
            @Override
            public void remove() {
                if(canRemove) {
                    canRemove = false;
                    removeAtIndex(curIndex);
                }
                else {
                    throw new IllegalStateException("Remove may only be used after a call to next() and only once "
                            + "for each invocation of next().");
                }
            }
        };
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        boolean changed = false;
        for(Object c : collection)
            changed = changed || remove(c);
        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean changed = false;
        for(int i=0; i<numElements; ++i) {
            T element = vec[i];
            if(!c.contains(element)) {
                removeAtIndex(i);
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public Object[] toArray() {
        return vec;
    }

    // Stuck with this warning, so suppressing it. Some consolation in that this is allegedly the solution used within 
    // JDK 1.6 (according to http://stackoverflow.com/questions/4010924/java-how-to-implement-toarray-for-collection)
    @SuppressWarnings("unchecked")
    @Override
    public <E> E[] toArray(E[] a) {
        E[] ret = a.length >= numElements 
                ? a 
                : (E[]) Array.newInstance(a.getClass().getComponentType(), numElements);
        
        System.arraycopy(vec, 0, ret, 0, numElements);
        // null out the rest of the array
        if(ret.length > numElements)
            ret[numElements] = null;
        return ret;
    }*/
}
