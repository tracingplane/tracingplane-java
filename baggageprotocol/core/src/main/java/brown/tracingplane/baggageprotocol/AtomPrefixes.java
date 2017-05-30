package brown.tracingplane.baggageprotocol;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import brown.tracingplane.atomlayer.Lexicographic;
import brown.tracingplane.atomlayer.TypeUtils;
import brown.tracingplane.baggageprotocol.AtomPrefixTypes.AtomType;
import brown.tracingplane.baggageprotocol.AtomPrefixTypes.BagOptionsInPrefix;
import brown.tracingplane.baggageprotocol.AtomPrefixTypes.HeaderType;
import brown.tracingplane.baggageprotocol.AtomPrefixTypes.Level;

/**
 * <p>
 * This class has the logic for creating and checking bag prefixes. It does not directly create or deal with bit
 * representations; that is contained in {@link AtomPrefixTypes}.
 * </p>
 * 
 * <p>
 * The baggage protocol only uses the first byte of an atom as a prefix. Consequently, during parsing, instead of
 * interpreting the first byte, we just look it up in an array of 256 prefix objects.
 * </p>
 */
public class AtomPrefixes {

    private static final AtomPrefix[] prefixes;

    static {
        // Initialize all prefixes as unsupported
        prefixes = new AtomPrefix[256];
        for (int i = 0; i < prefixes.length; i++) {
            prefixes[i] = new UnsupportedPrefix((byte) i);
        }

        // Construct all valid prefixes
        List<AtomPrefix> allowedAtoms = new ArrayList<>(256);
        allowedAtoms.add(DataPrefix.prefix());
        for (IndexedHeaderPrefix atom : IndexedHeaderPrefix.prefixes) {
            allowedAtoms.add(atom);
        }
        for (KeyedHeaderPrefix atom : KeyedHeaderPrefix.prefixes) {
            allowedAtoms.add(atom);
        }

        // Insert into array
        for (AtomPrefix atom : allowedAtoms) {
            if (atom.prefix >= 0) {
                prefixes[atom.prefix] = atom;
            } else {
                prefixes[256 + atom.prefix] = atom;
            }
        }
    }

    private AtomPrefixes() {}

    /**
     * Get the {@link AtomPrefix} object for the specified prefix
     * 
     * @param prefix the first byte of an atom
     * @return the {@link AtomPrefix} object that corresponds to this prefix
     */
    public static AtomPrefix get(byte prefix) {
        if (prefix >= 0) {
            return prefixes[prefix];
        } else {
            return prefixes[256 + prefix];
        }
    }

    /** AtomPrefix has some convenience methods for checking what kind of prefix a byte is and what it supports */
    public static abstract class AtomPrefix implements Comparable<AtomPrefix> {

        protected final AtomType atomType;
        public final byte prefix;

        public AtomPrefix(AtomType atomType, byte prefix) {
            this.atomType = atomType;
            this.prefix = prefix;
        }

        boolean isValid() {
            return true;
        }

        boolean isHeader() {
            return false;
        }

        boolean isData() {
            return false;
        }

        int level(int currentLevel) {
            return currentLevel + 1;
        }

        @Override
        public int compareTo(AtomPrefix o) {
            return Lexicographic.compare(prefix, o.prefix);
        }
    }

    public static abstract class HeaderPrefix extends AtomPrefix {

        public static final AtomType atomType = AtomType.Header;

        protected static final int distinctLevels = Level.LEVELS;
        protected static final int distinctBagOptions = BagOptionsInPrefix.values.length;
        protected static final int distinctPrefixes = distinctLevels * distinctBagOptions;

        protected final Level level;
        protected final HeaderType headerType;
        protected final BagOptionsInPrefix options;

        public HeaderPrefix(Level level, HeaderType headerType, BagOptionsInPrefix options) {
            super(AtomType.Header,
                  (byte) (AtomType.Header.byteValue | level.byteValue | headerType.byteValue | options.byteValue));
            this.level = level;
            this.headerType = headerType;
            this.options = options;
        }

        protected static int indexOf(int level, BagOptions options) {
            return indexOf(level, BagOptionsInPrefix.get(options));
        }

        protected static int indexOf(int level, BagOptionsInPrefix options) {
            return level * distinctBagOptions + options.id;
        }

        @Override
        boolean isHeader() {
            return true;
        }

        int level() {
            return this.level.level;
        }

        @Override
        int level(int currentLevel) {
            return this.level.level;
        }

        BagOptions options() {
            return BagOptions.create(options.merge);
        }

        abstract BagKey parse(ByteBuffer buf) throws BaggageLayerException;

    }

    public static class IndexedHeaderPrefix extends HeaderPrefix {

        public static final HeaderType headerType = HeaderType.Indexed;

        private static final IndexedHeaderPrefix[] prefixes;

        static {
            prefixes = new IndexedHeaderPrefix[distinctPrefixes];
            for (BagOptionsInPrefix options : BagOptionsInPrefix.values) {
                for (Level level : Level.levels) {
                    prefixes[indexOf(level.level, options)] = new IndexedHeaderPrefix(level, options);
                }
            }
        }

        public static IndexedHeaderPrefix prefixFor(int level, BagOptions options) {
            if (Level.isValidLevel(level)) {
                return prefixes[indexOf(level, options)];
            } else {
                return null;
            }
        }

        private IndexedHeaderPrefix(Level level, BagOptionsInPrefix options) {
            super(level, headerType, options);
        }

        @Override
        public String toString() {
            return String.format("[IndexedHeaderPrefix prefix=%s level=%d options=%s]",
                                 TypeUtils.toBinaryString(prefix), level.level, options);
        }

        @Override
        BagKey parse(ByteBuffer buf) throws BaggageLayerException {
            return HeaderSerialization.parseIndexedHeaderPayload(this, buf);
        }

    }

    public static class KeyedHeaderPrefix extends HeaderPrefix {

        public static final HeaderType headerType = HeaderType.Keyed;

        private static final KeyedHeaderPrefix[] prefixes;

        static {
            prefixes = new KeyedHeaderPrefix[distinctPrefixes];
            for (BagOptionsInPrefix options : BagOptionsInPrefix.values) {
                for (Level level : Level.levels) {
                    prefixes[indexOf(level.level, options)] = new KeyedHeaderPrefix(level, options);
                }
            }
        }

        public static KeyedHeaderPrefix prefixFor(int level, BagOptions options) {
            if (Level.isValidLevel(level)) {
                return prefixes[indexOf(level, options)];
            } else {
                return null;
            }
        }

        private KeyedHeaderPrefix(Level level, BagOptionsInPrefix options) {
            super(level, headerType, options);
        }

        @Override
        public String toString() {
            return String.format("[KeyedHeaderPrefix   prefix=%s level=%d, options=%s]",
                                 TypeUtils.toBinaryString(prefix), level.level, options);
        }

        @Override
        BagKey parse(ByteBuffer buf) throws BaggageLayerException {
            return HeaderSerialization.parseKeyedHeaderPayload(this, buf);
        }

    }

    public static class DataPrefix extends AtomPrefix {

        public static final AtomType atomType = AtomType.Data;
        public static final byte prefix = atomType.byteValue;
        static final DataPrefix instance = new DataPrefix();

        private DataPrefix() {
            super(atomType, DataPrefix.prefix);
        }

        public static DataPrefix prefix() {
            return instance;
        }

        @Override
        public boolean isData() {
            return true;
        }

        @Override
        public String toString() {
            return String.format("[DataPrefix prefix=%s]", TypeUtils.toBinaryString(super.prefix));
        }

    }

    public static class UnsupportedPrefix extends AtomPrefix {

        private UnsupportedPrefix(byte prefix) {
            super(null, prefix);
        }

        @Override
        boolean isValid() {
            return false;
        }

        @Override
        public String toString() {
            return String.format("[UnsupportedPrefix   prefix=%s]", TypeUtils.toBinaryString(prefix));
        }

    }

    public static void main(String[] args) {
        for (int i = 0; i < prefixes.length; i++) {
            System.out.printf("%d: %s\n", i, prefixes[i]);
        }
    }

}
