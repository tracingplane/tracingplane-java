package edu.brown.cs.systems.tracingplane.atom_layer.types;

import java.util.Comparator;
import java.util.Iterator;

/** Like {@link MergeIterator} but for two iterators only */
public class MergeTwoIterator<T> implements Iterator<T> {

    private final Iterator<T> a;
    private final Iterator<T> b;

    private T nexta = null;
    private T nextb = null;
    private Comparator<? super T> comparator;

    public MergeTwoIterator(Iterator<T> a, Iterator<T> b, Comparator<? super T> comparator) {
        this.a = a;
        this.b = b;
        this.comparator = comparator;
        advanceA();
        advanceB();
    }

    private void advanceA() {
        if (a.hasNext()) {
            nexta = a.next();
        } else {
            nexta = null;
        }
    }

    private void advanceB() {
        if (b.hasNext()) {
            nextb = b.next();
        } else {
            nextb = null;
        }
    }

    @Override
    public boolean hasNext() {
        return nexta != null || nextb != null;
    }

    @Override
    public T next() {
        T result = null;
        if (nexta == null) {
            result = nextb;
            advanceB();
        } else if (nextb == null) {
            result = nexta;
            advanceA();
        } else {
            int comparison = comparator.compare(nexta, nextb);
            if (comparison < 0) {
                result = nexta;
                advanceA();
            } else if (comparison == 0) {
                result = nexta;
                advanceA();
                advanceB();
            } else {
                result = nextb;
                advanceB();
            }
        }
        return result;
    }

}
