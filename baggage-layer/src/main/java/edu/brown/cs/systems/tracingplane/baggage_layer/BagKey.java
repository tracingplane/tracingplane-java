package edu.brown.cs.systems.tracingplane.baggage_layer;

import java.nio.ByteBuffer;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.Lists;
import edu.brown.cs.systems.tracingplane.atom_layer.types.Lexicographic;
import edu.brown.cs.systems.tracingplane.atom_layer.types.TypeUtils;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.AtomPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.IndexedHeaderPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.KeyedHeaderPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.HeaderSerialization;

/**
 * <p>
 * A {@link BagKey} is used to look up data items and child bags within a baggage instance. A {@link BagPath} is a list
 * of zero or more bag keys that acts much like a path in a file system.
 * </p>
 * 
 * <p>
 * BagKey instances should be created with the static methods {@link indexed} and {@link named}. As per the Baggage
 * protocol, fields can either be named (using arbitrary bytes) or indexed (with an unsigned integer). Indexes for
 * fields are statically assigned for efficiency in more established protocols. It is recommended to use named keys for
 * ad-hoc data.
 * </p>
 */
public abstract class BagKey implements Comparable<BagKey> {

    /**
     * Zero or more BagKeys represent a path to data in baggage. Paths can be used to access data from BaggageContents.
     */
    public static class BagPath implements Comparable<BagPath> {

        public final BagKey[] keys;

        private BagPath(BagKey[] keys) {
            this.keys = keys;
        }

        /** Creates and returns a new path that joins the provided keys */
        public static BagPath from(BagKey... keys) {
            return new BagPath(keys);
        }

        /** Creates and returns a new path that joins the provided keys */
        public static BagPath from(List<BagKey> keys) {
            return from(keys.toArray(new BagKey[keys.size()]));
        }

        /** Returns a new BagPath that is equal to this path plus the provided key appended to the end */
        public BagPath append(BagKey key) {
            BagKey[] newKeys = new BagKey[keys.length + 1];
            System.arraycopy(keys, 0, newKeys, 0, keys.length);
            newKeys[keys.length] = key;
            return new BagPath(newKeys);
        }

        /**
         * Creates and appends a new indexed key to the end of this bag path. Returns a new BagPath that is equal to
         * this path plus the created key appended to the end.
         */
        public BagPath append(int index) {
            return append(indexed(index));
        }

        /**
         * Creates and appends a new indexed key to the end of this bag path. Returns a new BagPath that is equal to
         * this path plus the created key appended to the end.
         */
        public BagPath append(int index, BagOptions options) {
            return append(indexed(index, options));
        }

        /**
         * Creates and appends a new named key to the end of this bag path. Returns a new BagPath that is equal to this
         * path plus the created key appended to the end.
         */
        public BagPath append(ByteBuffer key) {
            return append(named(key));
        }

        /**
         * Creates and appends a new named key to the end of this bag path. Returns a new BagPath that is equal to this
         * path plus the created key appended to the end.
         */
        public BagPath append(String key) {
            return append(named(key));
        }

        /**
         * Creates and appends a new named key to the end of this bag path. Returns a new BagPath that is equal to this
         * path plus the created key appended to the end.
         */
        public BagPath append(ByteBuffer key, BagOptions options) {
            return append(named(key, options));
        }

        /**
         * Creates and appends a new named key to the end of this bag path. Returns a new BagPath that is equal to this
         * path plus the created key appended to the end.
         */
        public BagPath append(String key, BagOptions options) {
            return append(named(key, options));
        }

        @Override
        public int compareTo(BagPath o) {
            int size = Math.min(keys.length, o.keys.length);
            for (int i = 0; i < size; i++) {
                int comparison = keys[i].compareTo(o.keys[i]);
                if (comparison != 0) {
                    return comparison;
                }
            }
            return Integer.compare(keys.length, o.keys.length);
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
            options = BagOptions.defaultOptions;
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
            options = BagOptions.defaultOptions;
        }
        if (name == null) {
            name = ByteBuffer.allocate(0);
        }
        return new Keyed(name, options);
    }

    /** An Indexed BagKey is one that uses an integer as identifier (versus arbitrary bytes for a {@Keyed} key) */
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
                byteRepr = HeaderSerialization.serializePayload(this);
            }
            return byteRepr;
        }

    }

    /** A Keyed BagKey is one that uses arbitrary bytes as identifier (versus an integer for {@Indexed} key) */
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
                byteRepr = HeaderSerialization.serializePayload(this);
            }
            return byteRepr;
        }
        
        @Override
        public String toString() {
            List<String> bytes = Lists.newArrayList();
            for (int i = 0; i < key.remaining(); i++) {
                bytes.add(TypeUtils.toHexString(key.get(key.position() + i)));
            }
            return String.format("Keyed %s %s", StringUtils.join(bytes, " "), options);
        }

    }

}
