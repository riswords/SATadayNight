package collections;

import static org.junit.Assert.*;

import org.junit.Test;

public class SimpleVecTest {

    /**
     * Push 1 element into a vector. 
     * Check that:
     *      the size is 1
     *      the element at index 0 is the one inserted
     *      the result of last() is the element inserted 
     */
    @Test
    public void testPushToEmptyVecHasExpectedProperties() {
        SimpleVec<String> vec = new SimpleVec<String>();
        vec.push("a");
        assertEquals(1, vec.size());
        assertEquals("a", vec.get(0));
        assertEquals("a", vec.last());
    }
    
    /**
     * Create a vector of size 2, and push 3 elements into the vector.
     * Check that no exceptions occur (implicitly), and the final size is 3.  
     */
    @Test
    public void testSizeIncreasesIfNeededInPush() {
        SimpleVec<String> vec = new SimpleVec<String>();
        vec.push("a");
        vec.push("b");
        vec.push("c");
        assertEquals(3, vec.size());
        assertEquals("a", vec.get(0));
        assertEquals("b", vec.get(1));
        assertEquals("c", vec.get(2));
        assertEquals("c", vec.last());
    }
    
    /**
     * Push 2 elements into a vector and then remove the first one.
     * Check that the size is 1 and the element at index 0 is the second one pushed.
     */
    @Test
    public void testRemoveDecreasesSize() {
        SimpleVec<String> vec = new SimpleVec<String>();
        vec.push("a");
        vec.push("b");
        vec.remove("a");
        assertEquals(1, vec.size());
        assertEquals("b", vec.get(0));
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
        SimpleVec<String> vec = new SimpleVec<String>();
        vec.push("a");
        vec.push("b");
        vec.push("c");
        vec.shrinkBy(2);
        assertEquals(1, vec.size());
        assertEquals("a", vec.get(0));
        assertEquals("a", vec.last());
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
        String defaultValue = "q";
        int newSize = 5;
        SimpleVec<String> vec = new SimpleVec<String>();
        vec.push("a");
        vec.growTo(newSize, defaultValue);
        assertEquals(newSize, vec.size());
        assertEquals("a", vec.get(0));
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
        SimpleVec<String> vec = new SimpleVec<String>();
        vec.push("a");
        vec.push("b");
        vec.pop();
        assertEquals(1, vec.size());
        assertEquals("a", vec.get(0));
        assertEquals("a", vec.last());
    }
}
