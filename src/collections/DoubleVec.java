package collections;

import java.util.Arrays;

import collections.exception.IndexOutOfBoundsException;
import collections.exception.UnderflowException;
import exception.IllegalArgumentException;

public class DoubleVec {
    private double[] vec;
    private int numElements = 0;
    private double defaultValue = 0.0;
    
    // Special marker for elements in the underlying array that are outside the range of numElements (and thus not
    // part of the vector).
    private static final double NULL_VALUE = Double.MIN_VALUE;
    // Tolerance for comparing doubles for equality
    private static final double TOLERANCE = 1e-9;

    public DoubleVec() {
        this(0, 0.0);
    }

    public DoubleVec(int size) {
        this(size, 0.0);
    }

    public DoubleVec(int size, double defaultValue) {
        vec = new double[size];
        Arrays.fill(vec, defaultValue);
        this.defaultValue = defaultValue;
        numElements = size;
    }

    /**
     * Return the number of elements in the vector.
     */
    public int size() {
        return numElements;
    }

    /**
     * Remove the last {@code numElements} elements from the vector.
     */
    public void shrinkBy(int numElementsToRemove) {
        if(numElements < numElementsToRemove)
            throw new IndexOutOfBoundsException("Size of vector (" + numElements + ") is less than " + 
                    numElementsToRemove);
        while(numElementsToRemove > 0) {
            pop();
            numElementsToRemove -= 1;
        }
    }

    /**
     * Remove the last element from the vector.
     */
    public void pop() {
        if(numElements < 1)
            throw new UnderflowException("Unable to pop from an empty vector.");
        numElements -= 1;
        vec[numElements] = NULL_VALUE;
    }

    /**
     * Increase the size of the vector to {@code size}, padding with the default value.
     */
    public void growTo(int size) {
        growTo(size, defaultValue);
    }

    /**
     * Increase the size of the vector to {@code size}, padding with {@code pad}.
     */
    public void growTo(int size, double pad) {
        if(size < numElements)
            throw new IllegalArgumentException("Vector already contains more than " + size + " elements.");
        ensureCapacity(size);
        Arrays.fill(vec, numElements, size, pad);
        numElements = size;
    }
    
    /**
     * Ensure that the underlying array can support at least size elements. Increase the size of the 
     * array if needed (padding with NULL_VALUE), but do not change numElements.
     */
    private void ensureCapacity(int size) {
        if(size > vec.length) {
            // increase by at least 16 elements at a time so that repeated pushing isn't as inefficient
            int newSize = Math.max(size, vec.length + 16);
            int oldLength = vec.length;
            vec = Arrays.copyOf(vec, newSize);
            Arrays.fill(vec, oldLength, newSize, NULL_VALUE);
        }
        assert(vec.length >= size) :
            "Unable to allocate " + size + "-element vector.";
    }

    /**
     * Remove all elements from the vector.
     */
    public void clear() {
        Arrays.fill(vec, 0, numElements, NULL_VALUE);
        numElements = 0;
    }

    /**
     * Push the default element into the vector (at the end).
     */
    public void push() {
        push(defaultValue);
    }

    /**
     * Push a specific element into the vector (at the end).
     */
    public void push(double elem) {
        ensureCapacity(numElements + 1);
        vec[numElements] = elem;
        numElements += 1;
    }

    /**
     * Return the last element in the vector.
     */
    public double last() {
        return get(numElements - 1);
    }

    /**
     * Return the element at index {@code index}.
     */
    public double get(int index) {
        if(index < 0 || index >= numElements)
            throw new IndexOutOfBoundsException(index);
        return vec[index];
    }

    /**
     * Copy contents of this vector, numElements, and defaultValue to {@code copy}.
     */
    public void copyTo(DoubleVec copy) {
        copy.ensureCapacity(numElements);
        copy.numElements = numElements;
        copy.defaultValue = defaultValue;
        System.arraycopy(vec, 0, copy.vec, 0, numElements);
    }

    /**
     * Move the contents of this vector to the destination {@code dest}. 
     * Per paper, should complete in constant time and also clear out the contents of the source vector.
     */
    public void moveTo(DoubleVec dest) {
        copyTo(dest);
        clear();
    }

    /**
     * Remove element {@code element} from the vector and return {@code true}. Return {@code false} if the element is 
     * not found. 
     */
    public boolean remove(double element) {
        boolean found = false;
        for(int i=0; i<numElements; ++i) {
            if(found)
                vec[i-1] = vec[i];
            else if(doublesEqual(vec[i], element))
                found = true;
        }
        if(found) {
            numElements -= 1;
            vec[numElements] = NULL_VALUE;
        }
        return found;
    }
    
    /**
     * Return {@code true} if the difference between {@code a} and {@code b} is less than or equal to 
     * {@code TOLERANCE}, i.e., they are "equal" within an epsilon value. 
     */
    private boolean doublesEqual(double a, double b) {
        return Math.abs(a - b) <= TOLERANCE;
    }

    /**
     * Set the element at index {@code index} to be {@code element}.
     */
    public void set(int index, double element) {
        if(index < 0 || index >= numElements)
            throw new IndexOutOfBoundsException(index);
        vec[index] = element;
    }
    
    /**
     * Set the default value to be used by push() or growTo(int).
     * The default value may also be set in the constructor. It is {@code -1} if unset.
     */
    public void setDefault(int defaultValue) {
        this.defaultValue = defaultValue;
    }
}
