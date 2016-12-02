package edu.brown.cs.systems.tracingplane.baggage_layer.impl;

import java.nio.ByteBuffer;

import edu.brown.cs.systems.tracingplane.context_layer.types.Lexicographic;

/**
 * BagHeaders are atoms that demarcate the beginning of a child bag
 */
public abstract class BagHeader implements Comparable<BagHeader> {

	final int priority;

	BagHeader(int priority) {
		this.priority = priority;
	}

	public static class InlineBagHeader extends BagHeader {
		public final int index;

		InlineBagHeader(int index) {
			super(0);
			this.index = index;
		}

		@Override
		public int compareTo(BagHeader o) {
			if (o == null) {
				return 1;
			} else if (o instanceof InlineBagHeader) {
				return Integer.compare(index, ((InlineBagHeader) o).index);
			} else {
				return Integer.compare(priority, o.priority);
			}
		}
	}

	public static class IndexedBagHeader extends BagHeader {
		public final int index;
		public final ByteBuffer options;

		IndexedBagHeader(int index, ByteBuffer options) {
			super(1);
			this.index = index;
			this.options = options;
		}

		@Override
		public int compareTo(BagHeader o) {
			if (o == null) {
				return 1;
			} else if (o instanceof IndexedBagHeader) {
				IndexedBagHeader oi = (IndexedBagHeader) o;
				if (index == oi.index) {
					return Lexicographic.compare(options, oi.options);
				} else {
					return Integer.compare(index, oi.index);
				}
			} else {
				return Integer.compare(priority, o.priority);
			}
		}
	}

	public static class KeyedBagHeader extends BagHeader {
		public final ByteBuffer key;
		public final ByteBuffer options;

		KeyedBagHeader(ByteBuffer key, ByteBuffer options) {
			super(2);
			this.key = key;
			this.options = options;
		}

		@Override
		public int compareTo(BagHeader o) {
			if (o == null) {
				return 1;
			} else if (o instanceof KeyedBagHeader) {
				KeyedBagHeader ok = (KeyedBagHeader) o;
				int comparison = Lexicographic.compare(key, ok.key);
				if (comparison == 0) {
					return Lexicographic.compare(options, ok.options);
				} else {
					return comparison;
				}
			} else {
				return Integer.compare(priority, o.priority);
			}
		}
	}

	public static BagHeader inline(int childIndex) throws BaggageSerializationException {
		if (!AtomPrefixes.InlineFieldPrefix.idCanBeInlined(childIndex)) {
			throw BaggageSerializationException.invalidInlineField(childIndex);
		}
		return new InlineBagHeader(childIndex);
	}

	public static BagHeader indexed(int childIndex) {
		return indexed(childIndex, null);
	}

	public static BagHeader indexed(int childIndex, ByteBuffer childOptions) {
		return new IndexedBagHeader(childIndex, childOptions);
	}

	public static BagHeader keyed(ByteBuffer childKey) {
		return keyed(childKey, null);
	}

	public static BagHeader keyed(ByteBuffer childKey, ByteBuffer childOptions) {
		return new KeyedBagHeader(childKey, childOptions);
	}

}
