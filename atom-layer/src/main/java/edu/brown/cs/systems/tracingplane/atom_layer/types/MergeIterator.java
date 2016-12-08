package edu.brown.cs.systems.tracingplane.atom_layer.types;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import com.google.common.collect.Lists;

/** <p>
 * Merges multiple iterators and produces elements according to the provided comparator.
 * </p>
 * <p>
 * The input iterators do not need to be sorted. However, if two iterators produce the same element at the same time,
 * the element will only be output once.
 * </p>
 * 
 * <p>
 * eg, for inputs [1, 2, 3] and [0, 1, 2], the output will be [0, 1, 2, 3] <br />
 * however, for inputs [1, 2, 1] and [1, 3, 2], the output will be [1, 2, 1, 3, 2]
 * </p>
 * 
 *
 * @param <T> */
public class MergeIterator<T> implements Iterator<T> {

    private class IteratorContainer {

        final Iterator<? extends T> it;
        T next;

        public IteratorContainer(Iterator<? extends T> it) {
            this.it = it;
            this.next = it.next();
        }

        void advance() {
            next = it.hasNext() ? it.next() : null;
        }

    }

    private final Comparator<? super T> comparator;
    private final List<IteratorContainer> iterators;
    private final List<IteratorContainer> iteratorsWithNextValue;
    private T nextValue = null;

    public MergeIterator(Comparator<? super T> comparator, @SuppressWarnings("unchecked") Iterator<T>... iterators) {
        this(Lists.newArrayList(iterators), comparator);
    }

    public MergeIterator(List<Iterator<T>> iterators, Comparator<? super T> comparator) {
        int size = iterators.size();

        this.comparator = comparator;
        this.iterators = new ArrayList<IteratorContainer>(size);
        this.iteratorsWithNextValue = new ArrayList<IteratorContainer>(size);
        this.nextValue = null;

        for (Iterator<? extends T> it : iterators) {
            if (it != null && it.hasNext()) {
                this.iterators.add(new IteratorContainer(it));
            }
        }

        advance();
    }

    private void advance() {
        // Advance all of the iterators that produced the previous value
        for (IteratorContainer it : iteratorsWithNextValue) {
            it.advance();
            if (it.next == null) {
                iterators.remove(it);
            }
        }

        // Get the next min value
        T next = null;
        for (IteratorContainer it : iterators) {
            int comparison = next == null ? -1 : comparator.compare(it.next, next);
            if (comparison < 0) {
                next = it.next;
                iteratorsWithNextValue.clear();
                iteratorsWithNextValue.add(it);
            } else if (comparison == 0) {
                iteratorsWithNextValue.add(it);
            }
        }
        nextValue = next;
    }

    @Override
    public boolean hasNext() {
        return nextValue != null;
    }

    @Override
    public T next() {
        T ret = nextValue;
        advance();
        return ret;
    }

}
