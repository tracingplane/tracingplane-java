package brown.tracingplane.atomlayer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import com.google.common.collect.Lists;
import brown.tracingplane.atomlayer.MergeTwoIterator;

public class TestMergeTwoIterator {

    private static final Comparator<Integer> integerComparator = new Comparator<Integer>() {
        public int compare(Integer o1, Integer o2) {
            return Integer.compare(o1, o2);
        }
    };

    @Test
    public void testMergeIterator() {
        List<Integer> evens = Lists.newArrayList();
        List<Integer> odds = Lists.newArrayList();

        for (int i = 0; i < 20; i++) {
            if (i % 2 == 0) {
                evens.add(i);
            } else {
                odds.add(i);
            }
        }

        MergeTwoIterator<Integer> merged =
                new MergeTwoIterator<Integer>(evens.iterator(), odds.iterator(), integerComparator);

        assertTrue(merged.hasNext());
        for (Integer i = 0; i < 20; i++) {
            assertTrue(merged.hasNext());
            assertEquals(i, merged.next());
        }
        assertFalse(merged.hasNext());

        // Same test, reverse order
        merged = new MergeTwoIterator<Integer>(odds.iterator(), evens.iterator(), integerComparator);

        assertTrue(merged.hasNext());
        for (Integer i = 0; i < 20; i++) {
            assertTrue(merged.hasNext());
            assertEquals(i, merged.next());
        }
        assertFalse(merged.hasNext());
    }

    @Test
    public void testMergeIterator2() {
        List<Integer> tens = Lists.newArrayList();
        List<Integer> twenties = Lists.newArrayList();

        for (int i = 0; i < 10; i++) {
            tens.add(i);
            twenties.add(i + 10);
        }

        MergeTwoIterator<Integer> merged =
                new MergeTwoIterator<Integer>(tens.iterator(), twenties.iterator(), integerComparator);

        assertTrue(merged.hasNext());
        for (Integer i = 0; i < 20; i++) {
            assertTrue(merged.hasNext());
            assertEquals(i, merged.next());
        }
        assertFalse(merged.hasNext());

        // Same test, reverse order
        merged = new MergeTwoIterator<Integer>(twenties.iterator(), tens.iterator(), integerComparator);

        assertTrue(merged.hasNext());
        for (Integer i = 0; i < 20; i++) {
            assertTrue(merged.hasNext());
            assertEquals(i, merged.next());
        }
        assertFalse(merged.hasNext());
    }

    @Test
    public void testMergeDuplicates() {
        List<Integer> a = Lists.newArrayList();

        for (int i = 0; i < 10; i++) {
            a.add(i);
        }

        MergeTwoIterator<Integer> merged = new MergeTwoIterator<Integer>(a.iterator(), a.iterator(), integerComparator);

        assertTrue(merged.hasNext());
        for (Integer i = 0; i < 10; i++) {
            assertTrue(merged.hasNext());
            assertEquals(i, merged.next());
        }
        assertFalse(merged.hasNext());
    }

    @Test
    public void testMultipleEmptyIterators() {
        List<Integer> empty = Lists.newArrayList();
        MergeTwoIterator<Integer> merged =
                new MergeTwoIterator<Integer>(empty.iterator(), empty.iterator(), integerComparator);
        assertFalse(merged.hasNext());
    }

    @Test
    public void testEmptyIteratorPlusOne() {
        List<Integer> list = Lists.newArrayList();
        Random r = new Random(1);
        for (int i = 0; i < 100; i++) {
            list.add(r.nextInt());
        }

        List<Integer> empty = Lists.newArrayList();
        MergeTwoIterator<Integer> merged =
                new MergeTwoIterator<Integer>(empty.iterator(), list.iterator(), integerComparator);

        for (Integer i = 0; i < 100; i++) {
            assertTrue(merged.hasNext());
            assertEquals(list.get(i), merged.next());
        }
        assertFalse(merged.hasNext());

        merged = new MergeTwoIterator<Integer>(list.iterator(), empty.iterator(), integerComparator);

        for (Integer i = 0; i < 100; i++) {
            assertTrue(merged.hasNext());
            assertEquals(list.get(i), merged.next());
        }
        assertFalse(merged.hasNext());
    }

}
