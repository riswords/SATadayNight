package collections;

import collections.exception.UnderflowException;

/**
 * A simple queue built on top of the SimpleVec implementation. The front of the queue starts off at index 0 in the 
 * vector. As items are dequeued, the front moves farther away from the beginning.
 * 
 * The {@code WASTE_TOLERANCE} parameter is used to control whether and when to shift the queue so that the head is at 
 * the beginning of the vector again. When an insert happens, check if the index of the head is greater than or equal 
 * to {@code WASTE_TOLERANCE}, and if so shift the whole queue back. To turn this off, set {@code WASTE_TOLERANCE} to 
 * a negative number with the constructor.
 * 
 * Note: this process doesn't actually result in freeing any additional memory, but does prevent the next several 
 * insertions from allocating additional memory. If space consumption is an issue, consider an alternative 
 * implementation where either the queue is recreated as a new vector containing only the existing elements, or 
 * use a vector that supports shrinking the capacity.
 */
public class SimpleQueue<T> implements Queue<T> {

    private SimpleVec<T> queue = new SimpleVec<T>();
    private int endIndex = 0;
    
    // amount of wasted space to allow before shifting the queue back to the start of the vector
    private final int WASTE_TOLERANCE; 
    
    public SimpleQueue() {
        this(64);
    }
    
    public SimpleQueue(int wasteTolerance) {
        WASTE_TOLERANCE = wasteTolerance;
    }
    
    @Override
    public void insert(T t) {
        if(WASTE_TOLERANCE > 0 && endIndex >= WASTE_TOLERANCE)
            shiftQueue();
        queue.push(t);
    }
    
    private void shiftQueue() {
        //System.out.println("SHIFT"); // poor man's check that this is being called
        for(int i=0; i<size(); ++i) {
            queue.set(i, queue.get(endIndex + i));
        }
        queue.shrinkBy(endIndex);
        endIndex = 0;
    }

    @Override
    public T dequeue() {
        if(endIndex < 0 || endIndex >= queue.size())
            throw new UnderflowException("Unable to remove from an empty queue.");
        
        T ret = queue.get(endIndex);
        queue.set(endIndex, null);
        endIndex += 1;
        return ret;
    }

    @Override
    public void clear() {
        queue.clear();
        endIndex = 0;
    }

    @Override
    public int size() {
        return queue.size() - endIndex;  
    }
}
