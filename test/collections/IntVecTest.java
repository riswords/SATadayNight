package collections;

import static org.junit.Assert.*;

import org.junit.Test;

public class IntVecTest {

    /**
     * Push 1 element into a vector. 
     * Check that:
     *      the size is 1
     *      the element at index 0 is the one inserted
     *      the result of last() is the element inserted 
     */
    @Test
    public void testPushToEmptyVecHasExpectedProperties() {
        IntVec vec = new IntVec();
        vec.push(5);
        assertEquals(1, vec.size());
        assertEquals(5, vec.get(0));
        assertEquals(5, vec.last());
    }
    
    /**
     * Create a vector of size 2, and push 3 elements into the vector.
     * Check that no exceptions occur (implicitly), and the final size is 3.  
     */
    @Test
    public void testSizeIncreasesIfNeededInPush() {
        IntVec vec = new IntVec();
        vec.push(5);
        vec.push(7);
        vec.push(9);
        assertEquals(3, vec.size());
        assertEquals(9, vec.get(2));
        assertEquals(9, vec.last());
    }
    
    /**
     * Push 2 elements into a vector and then remove the first one.
     * Check that the size is 1 and the element at index 0 is the second one pushed.
     */
    @Test
    public void testRemoveDecreasesSize() {
        IntVec vec = new IntVec();
        vec.push(5);
        vec.push(7);
        vec.remove(5);
        assertEquals(1, vec.size());
        assertEquals(7, vec.get(0));
    }

    /**
     * Push 3 elements into a vector and then shrinkBy(2).
     * Check that
     *      the size is 1
     *      the element at index 0 is the first one pushed
     *      last() returns the first element pushed
     */
    @Test
    public void testShrinkByDecreasesSize() {
        IntVec vec = new IntVec();
        vec.push(5);
        vec.push(7);
        vec.push(9);
        vec.shrinkBy(2);
        assertEquals(1, vec.size());
        assertEquals(5, vec.get(0));
        assertEquals(5, vec.last());
    }
    
    /**
     * Push 1 element into a vector and then growTo(5).
     * Check that:
     *      size is 5
     *      element at index 0 is first element pushed
     *      elements at indices 1-4 are the default value
     *      last() returns the default value
     */
    @Test
    public void testGrowToPadsToCorrectSizeWithDefaultValue() {
        int defaultValue = 2;
        int newSize = 5;
        IntVec vec = new IntVec(0, defaultValue);
        vec.push(8);
        vec.growTo(newSize);
        assertEquals(newSize, vec.size());
        assertEquals(8, vec.get(0));
        assertEquals(defaultValue, vec.last());
        for(int i=1; i<newSize; ++i) {
            assertEquals(defaultValue, vec.get(i));
        }
    }
    
    /**
     * Push 2 elements onto a vector and then pop()
     * Check that:
     *      size is 1
     *      element at index 0 is first element pushed
     *      last() returns the first element pushed
     */
    @Test
    public void testPopRemovesLastElement() {
        IntVec vec = new IntVec();
        vec.push(5);
        vec.push(7);
        vec.pop();
        assertEquals(1, vec.size());
        assertEquals(5, vec.get(0));
        assertEquals(5, vec.last());
    }
}
