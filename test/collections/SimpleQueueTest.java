package collections;

import static org.junit.Assert.*;

import org.junit.Test;

public class SimpleQueueTest {

    /**
     * Insert 1 item into the queue.
     * Check that the size is 1.
     */
    @Test
    public void testInsertToEmptyQueueHasSize1() {
        SimpleQueue<String> queue = new SimpleQueue<String>();
        queue.insert("a");
        assertEquals(1, queue.size());
    }
    
    /**
     * Insert 1 item into the queue, and then dequeue.
     * Check that:
     *      the size is 0
     *      the item dequeued is the one that was inserted
     */
    @Test
    public void testInsert1AndDequeueHasExpectedProperties() {
        SimpleQueue<String> queue = new SimpleQueue<String>();
        queue.insert("a");
        String actual = queue.dequeue();
        assertEquals(0, queue.size());
        assertEquals("a", actual);
    }
    
    /**
     * Insert 2 items into the queue, and then clear the queue.
     * Check that the size is empty.
     */
    @Test
    public void testClearEmptiesTheQueue() {
        SimpleQueue<String> queue = new SimpleQueue<String>();
        queue.insert("a");
        queue.insert("b");
        queue.clear();
        assertEquals(0, queue.size());
    }
    
    /**
     * Create a queue with waste tolerance of 2.
     * Insert 5 items into the queue, dequeue 2 items, insert 1 more to trigger shift.
     * Check that:
     *      size is 4
     *      dequeueing last 4 items results in items 3-6
     * 
     * Test uses extra items to be sure that if we shift left  when there are more items in the queue than the 
     * tolerance, we don't lose any of them, get exceptions, etc.
     * 
     * TODO: to test reliably, should probably ensure that shiftQueue is actually called, but this requires use of a 
     * mocking/spying framework.
     */
    @Test
    public void testWasteShiftingWorks() {
        SimpleQueue<String> queue = new SimpleQueue<>(2);
        queue.insert("a");
        queue.insert("b");
        queue.insert("c");
        queue.insert("d");
        queue.insert("e");
        assertEquals(5, queue.size());
        
        assertEquals("a", queue.dequeue());
        assertEquals(4, queue.size());
        
        assertEquals("b", queue.dequeue());
        assertEquals(3, queue.size());
        
        queue.insert("f");  // should trigger shift
        assertEquals(4, queue.size());
        
        assertEquals("c", queue.dequeue());
        assertEquals(3, queue.size());
        
        assertEquals("d", queue.dequeue());
        assertEquals(2, queue.size());
        
        assertEquals("e", queue.dequeue());
        assertEquals(1, queue.size());
        
        assertEquals("f", queue.dequeue());
        assertEquals(0, queue.size());
    }
}
