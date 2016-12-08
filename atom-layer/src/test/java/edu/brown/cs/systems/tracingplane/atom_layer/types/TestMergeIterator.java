package edu.brown.cs.systems.tracingplane.atom_layer.types;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import com.google.common.collect.Lists;
import junit.framework.TestCase;

public class TestMergeIterator extends TestCase {

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

        MergeIterator<Integer> merged =
                new MergeIterator<Integer>(Lists.<Iterator<Integer>> newArrayList(evens.iterator(), odds.iterator()),
                                           integerComparator);

        assertTrue(merged.hasNext());
        for (Integer i = 0; i < 20; i++) {
            assertTrue(merged.hasNext());
            assertEquals(i, merged.next());
        }
        assertFalse(merged.hasNext());

        // Same test, reverse order
        merged = new MergeIterator<Integer>(Lists.<Iterator<Integer>> newArrayList(odds.iterator(), evens.iterator()),
                                            integerComparator);

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

        MergeIterator<Integer> merged =
                new MergeIterator<Integer>(Lists.<Iterator<Integer>> newArrayList(tens.iterator(), twenties.iterator()),
                                           integerComparator);

        assertTrue(merged.hasNext());
        for (Integer i = 0; i < 20; i++) {
            assertTrue(merged.hasNext());
            assertEquals(i, merged.next());
        }
        assertFalse(merged.hasNext());

        // Same test, reverse order
        merged = new MergeIterator<Integer>(Lists.<Iterator<Integer>> newArrayList(twenties.iterator(),
                                                                                   tens.iterator()),
                                            integerComparator);

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

        MergeIterator<Integer> merged =
                new MergeIterator<Integer>(Lists.<Iterator<Integer>> newArrayList(a.iterator(), a.iterator()),
                                           integerComparator);

        assertTrue(merged.hasNext());
        for (Integer i = 0; i < 10; i++) {
            assertTrue(merged.hasNext());
            assertEquals(i, merged.next());
        }
        assertFalse(merged.hasNext());
    }

    @Test
    public void testMergeDuplicatesAndDifferentEndPoints() {
        int numLists = 10;
        List<List<Integer>> allLists = Lists.newArrayList();
        for (int i = 0; i < numLists; i++) {
            List<Integer> l = new ArrayList<>();
            for (int j = 0; j <= i; j++) {
                l.add(j);
            }
            allLists.add(l);
        }

        List<Iterator<Integer>> iterators = Lists.newArrayList();
        for (List<Integer> list : allLists) {
            iterators.add(list.iterator());
        }

        MergeIterator<Integer> merged = new MergeIterator<Integer>(iterators, integerComparator);

        assertTrue(merged.hasNext());
        for (Integer i = 0; i < numLists; i++) {
            assertTrue(merged.hasNext());
            assertEquals(i, merged.next());
        }
        assertFalse(merged.hasNext());
    }

    @Test
    public void testNoIterators() {
        List<Iterator<Integer>> iterators = Lists.newArrayList();
        MergeIterator<Integer> merged = new MergeIterator<Integer>(iterators, integerComparator);
        assertFalse(merged.hasNext());
    }

    @Test
    public void testOneIterator() {
        List<Integer> list = Lists.newArrayList();
        Random r = new Random(1);
        for (int i = 0; i < 100; i++) {
            list.add(r.nextInt());
        }

        List<Iterator<Integer>> iterators = Lists.newArrayList(list.iterator());
        MergeIterator<Integer> merged = new MergeIterator<Integer>(iterators, integerComparator);

        for (Integer i = 0; i < 100; i++) {
            assertTrue(merged.hasNext());
            assertEquals(list.get(i), merged.next());
        }
        assertFalse(merged.hasNext());

    }

    @Test
    public void testEmptyIterator() {
        List<Integer> empty = Lists.newArrayList();
        List<Iterator<Integer>> iterators = Lists.newArrayList(empty.iterator());
        MergeIterator<Integer> merged = new MergeIterator<Integer>(iterators, integerComparator);
        assertFalse(merged.hasNext());
    }

    @Test
    public void testMultipleEmptyIterators() {
        List<Integer> empty = Lists.newArrayList();
        List<Iterator<Integer>> iterators = Lists.newArrayList(empty.iterator(), empty.iterator(), empty.iterator());
        MergeIterator<Integer> merged = new MergeIterator<Integer>(iterators, integerComparator);
        assertFalse(merged.hasNext());
    }

    @Test
    public void testMultipleEmptyIteratorsPlusOne() {
        List<Integer> list = Lists.newArrayList();
        Random r = new Random(1);
        for (int i = 0; i < 100; i++) {
            list.add(r.nextInt());
        }

        List<Integer> empty = Lists.newArrayList();
        List<Iterator<Integer>> iterators =
                Lists.newArrayList(empty.iterator(), empty.iterator(), list.iterator(), empty.iterator());
        MergeIterator<Integer> merged = new MergeIterator<Integer>(iterators, integerComparator);

        for (Integer i = 0; i < 100; i++) {
            assertTrue(merged.hasNext());
            assertEquals(list.get(i), merged.next());
        }
        assertFalse(merged.hasNext());
    }

}
