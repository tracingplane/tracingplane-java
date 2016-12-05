package edu.brown.cs.systems.tracingplane.baggage_layer.impl;

public abstract class BagKey implements Comparable<BagKey> {

	protected static final int PRIORITY_INDEXED_BAG = 0;
	protected static final int PRIORITY_NAMED_BAG = 1;

	protected final int priority;
	public final BagOptions options;

	protected BagKey(int priority, BagOptions options) {
		this.priority = priority;
		this.options = options;
	}

	public final class IndexedBagKey extends BagKey {

		public final int index;

		public IndexedBagKey(int index, BagOptions options) {
			super(PRIORITY_INDEXED_BAG, options);
			this.index = index;
		}

		@Override
		public int compareTo(BagKey o) {
			if (priority == o.priority) {
				if (o instanceof IndexedBagKey) {
					int otherIndex = ((IndexedBagKey) o).index;
					return index < otherIndex ? -1 : (index == otherIndex ? options.compareTo(o.options) : 1);
				} else {
					return options.compareTo(o.options);
				}
			} else {
				return priority < o.priority ? -1 : 1;
			}
		}

	}
	
	public final class NamedBagKey extends BagKey {
		
		public final String name;
		
		public NamedBagKey(String name, BagOptions options) {
			super(PRIORITY_NAMED_BAG, options);
			this.name = name;
		}
		
		@Override
		public int compareTo(BagKey o) {
			if (priority == o.priority) {
				if (o instanceof NamedBagKey) {
					int nameComparison = name.compareTo(((NamedBagKey) o).name);
					return nameComparison == 0 ? options.compareTo(o.options) : nameComparison;
				} else {
					return options.compareTo(o.options);
				}
			} else {
				return priority < o.priority ? -1 : 1;
			}
		}
		
	}

}
