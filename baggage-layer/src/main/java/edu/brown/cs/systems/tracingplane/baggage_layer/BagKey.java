package edu.brown.cs.systems.tracingplane.baggage_layer;

import java.nio.ByteBuffer;
import java.util.List;

import com.google.common.collect.Lists;

import edu.brown.cs.systems.tracingplane.atom_layer.types.Lexicographic;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.AtomPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.IndexedHeaderPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.KeyedHeaderPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BagKeySerialization;

public abstract class BagKey implements Comparable<BagKey> {

	public static class BagPath implements Comparable<BagPath> {

		public final List<BagKey> keys;

		public BagPath(BagKey... keys) {
			this(Lists.newArrayList(keys));
		}

		public BagPath(List<BagKey> keys) {
			this.keys = keys;
		}

		@Override
		public int compareTo(BagPath o) {
			int size = Math.min(keys.size(), o.keys.size());
			for (int i = 0; i < size; i++) {
				int comparison = keys.get(i).compareTo(o.keys.get(i));
				if (comparison != 0) {
					return comparison;
				}
			}
			return Integer.compare(keys.size(), o.keys.size());
		}

		@Override
		public boolean equals(Object o) {
			if (o != null && o instanceof BagPath) {
				return keys.equals(((BagPath) o).keys);
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return keys.hashCode();
		}

	}

	protected static final int PRIORITY_INDEXED_BAG = 0;
	protected static final int PRIORITY_KEYED_BAG = 1;

	protected final int priority;
	public final BagOptions options;

	private BagKey(int priority, BagOptions options) {
		this.priority = priority;
		this.options = options;
	}

	public abstract AtomPrefix atomPrefix(int level);

	public abstract ByteBuffer atomPayload();

	public static BagKey indexed(int index) {
		return indexed(index, null);
	}

	public static BagKey indexed(int index, BagOptions options) {
		if (options == null) {
			options = BagOptions.DEFAULT_OPTIONS;
		}
		return new Indexed(index, options);
	}

	public static BagKey named(String name) {
		return named(name, null);
	}

	public static BagKey named(String name, BagOptions options) {
		return named(ByteBuffer.wrap(name.getBytes()), options);
	}

	public static BagKey named(ByteBuffer name) {
		return named(name, null);
	}

	public static BagKey named(ByteBuffer name, BagOptions options) {
		if (options == null) {
			options = BagOptions.DEFAULT_OPTIONS;
		}
		if (name == null) {
			name = ByteBuffer.allocate(0);
		}
		return new Keyed(name, options);
	}

	public static final class Indexed extends BagKey {

		private ByteBuffer byteRepr = null;
		public final int index;

		private Indexed(int index, BagOptions options) {
			super(PRIORITY_INDEXED_BAG, options);
			this.index = index;
		}

		@Override
		public int compareTo(BagKey o) {
			if (priority == o.priority) {
				if (o instanceof Indexed) {
					int otherIndex = ((Indexed) o).index;
					return index < otherIndex ? -1 : (index == otherIndex ? options.compareTo(o.options) : 1);
				} else {
					return options.compareTo(o.options);
				}
			} else {
				return priority < o.priority ? -1 : 1;
			}
		}

		@Override
		public boolean equals(Object o) {
			if (o != null && o instanceof Indexed) {
				Indexed io = (Indexed) o;
				return priority == io.priority && index == io.index && options.compareTo(io.options) == 0;
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			int h = 0;
			h = 31 * h + priority;
			h = 31 * h + index;
			h = 31 * h + options.hashCode();
			return h;
		}

		@Override
		public AtomPrefix atomPrefix(int level) {
			return IndexedHeaderPrefix.prefixFor(level);
		}

		@Override
		public ByteBuffer atomPayload() {
			if (byteRepr == null) {
				byteRepr = BagKeySerialization.serialize(index, options);
			}
			return byteRepr;
		}

	}

	public static final class Keyed extends BagKey {

		private ByteBuffer byteRepr = null;
		public final ByteBuffer key;

		private Keyed(ByteBuffer key, BagOptions options) {
			super(PRIORITY_KEYED_BAG, options);
			this.key = key;
		}

		@Override
		public int compareTo(BagKey o) {
			if (priority == o.priority) {
				if (o instanceof Keyed) {
					int nameComparison = Lexicographic.compare(key, ((Keyed) o).key);
					return nameComparison == 0 ? options.compareTo(o.options) : nameComparison;
				} else {
					return options.compareTo(o.options);
				}
			} else {
				return priority < o.priority ? -1 : 1;
			}
		}

		@Override
		public boolean equals(Object o) {
			if (o != null && o instanceof Keyed) {
				Keyed no = (Keyed) o;
				return priority == no.priority && key.equals(no.key) && options.equals(no.options);
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			int h = 0;
			h = 31 * h + priority;
			h = 31 * h + key.hashCode();
			h = 31 * h + options.hashCode();
			return h;
		}

		@Override
		public AtomPrefix atomPrefix(int level) {
			return KeyedHeaderPrefix.prefixFor(level);
		}

		@Override
		public ByteBuffer atomPayload() {
			if (byteRepr == null) {
				byteRepr = BagKeySerialization.serialize(key, options);
			}
			return byteRepr;
		}

	}

}
