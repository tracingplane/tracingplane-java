package edu.brown.cs.systems.tracingplane.atom_layer.types;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

// TODO: tests
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

	public MergeIterator(List<Iterator<T>> iterators, Comparator<? super T> comparator) {
		int size = iterators.size();

		this.comparator = comparator;
		this.iterators = new ArrayList<IteratorContainer>(size);
		this.iteratorsWithNextValue = new ArrayList<IteratorContainer>(size);
		this.nextValue = null;

		for (Iterator<? extends T> it : iterators) {
			if (it.hasNext()) {
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
