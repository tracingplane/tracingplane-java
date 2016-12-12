package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

import edu.brown.cs.systems.tracingplane.atom_layer.BaggageAtoms;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagOptions;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagOptions.MergeBehavior;

/**
 * This class has the bitwise logic for different bags prefixes.
 * 
 * For now the protocol is such that the first byte of each atom is its prefix, and the remaining bytes are its payload.
 */
public class AtomPrefixTypes {

    private AtomPrefixTypes() {}

    private static final AtomType[] bagTypes = new AtomType[AtomType.VALUES];
    private static final HeaderType[] headerTypes = new HeaderType[HeaderType.VALUES];

    /**
     * The first bit of a prefix is the atom type:
     * 
     * 0 is a data atom <br>
     * 1 is a header atom <br>
     * 
     * There is also an overflow marker {@link BaggageAtoms#OVERFLOW_MARKER} that is just the empty byte array (ie, it
     * has no prefix) and therefore lexicographically less than all other atoms
     */
    public enum AtomType {
                          Data(0), Header(1);

        public static final int BITS = 1;
        public static final int VALUES = 2;

        private static final int mask = 0x80;
        private static final int offset = 7;

        public final int id;
        public final byte byteValue;

        private AtomType(int id) {
            this.id = id;
            this.byteValue = (byte) ((id << offset) & mask);
            bagTypes[id] = this;
        }

        public static AtomType fromByte(byte b) {
            int id = (b & mask) >>> offset;
            return bagTypes[id];
        }

        public boolean match(byte b) {
            return (b & mask) == byteValue;
        }
    }

    /**
     * <p>
     * Levels are used by headers. Within a header prefix, bytes 2 through 5 specify the {@link Level} of the header,
     * while the final two bits specify the {@link HeaderType}.
     * </p>
     * 
     * Since four bits are used for a level, there are 16 possible valid levels. Levels {@code l} is encoded as
     * {@code 15 - l} to ensure that, lexicographically, lower levels come last. Specifically:
     * 
     * level 0 is 1111 <br>
     * level 1 is 1110 <br>
     * level 2 is 1101 <br>
     * level 3 is 1100 <br>
     * level 4 is 1011 <br>
     * level 5 is 1010 <br>
     * level 6 is 1001 <br>
     * level 7 is 1000 <br>
     * level 8 is 0111 <br>
     * level 9 is 0110 <br>
     * level 10 is 0101 <br>
     * level 11 is 0100 <br>
     * level 12 is 0011 <br>
     * level 13 is 0010 <br>
     * level 14 is 0001 <br>
     * level 15 is 0000 <br>
     */
    public static class Level {

        public static final int LEVELS = 16;

        private static final int MAXLEVEL = 15;
        private static final int mask = 0x78;
        private static final int offset = 3;

        static final Level[] levels = new Level[LEVELS];

        static {
            for (int i = 0; i < LEVELS; i++) {
                levels[i] = new Level(i);
            }
        }

        public final int level;
        public final byte byteValue;

        private Level(int level) {
            this.level = level;
            this.byteValue = (byte) (((MAXLEVEL - level) << offset) & mask);
        }

        public static boolean isValidLevel(int level) {
            return (level & 0xF0) == 0;
        }

        public static Level get(int level) {
            if (isValidLevel(level)) {
                return levels[level];
            } else {
                return null;
            }
        }

        public static int valueOf(byte b) {
            return MAXLEVEL - ((b & mask) >> offset);
        }

        public static Level fromByte(byte b) {
            return levels[valueOf(b)];
        }

        public boolean match(byte b) {
            return (b & mask) == byteValue;
        }

    }

    /**
     * There are two different types of header. Within a header prefix, the middle four bits specify the {@link Level}
     * of the header, while the final two bits specify the {@link HeaderType}.
     * 
     * 00 is not currently used <br>
     * 01 is an indexed header <br>
     * 10 is a keyed header <br>
     * 11 is not currently used
     */
    public enum HeaderType {
                            Indexed(0), Keyed(1);

        public static final int BITS = 1;
        public static final int VALUES = 2;

        private static final int mask = 0x04;
        private static final int offset = 2;

        public final int id;
        public final byte byteValue;

        private HeaderType(int id) {
            this.id = id;
            this.byteValue = (byte) ((id << offset) & mask);
            headerTypes[id] = this;
        }

        /**
         * Inspect the final two bits of the provided byte and return the corresponding {@link HeaderType}
         * 
         * @param b an atom prefix
         * @return the HeaderType encoded in this prefix
         */
        public static HeaderType fromByte(byte b) {
            int id = (b & mask) >> offset;
            return headerTypes[id];
        }

        public boolean match(byte b) {
            return (b & mask) == byteValue;
        }

    }

    public static class BagOptionsInPrefix {

        public static final int mask = 0x01;
        public static final int offset = 0;

        public final int id;
        public final MergeBehavior merge;
        public final byte byteValue;

        static final BagOptionsInPrefix[] values = new BagOptionsInPrefix[2];
        static {
            values[0] = new BagOptionsInPrefix(0, MergeBehavior.TakeAll);
            values[1] = new BagOptionsInPrefix(1, MergeBehavior.TakeFirst);
        }

        public BagOptionsInPrefix(int id, MergeBehavior merge) {
            this.id = id;
            this.merge = merge;
            this.byteValue = (byte) ((merge.ordinal() << offset) & mask);
        }

        public static BagOptionsInPrefix fromByte(byte b) {
            int merge = (b & mask) >> offset;
            return values[merge];
        }

        public static BagOptionsInPrefix get(BagOptions options) {
            return values[options.merge.ordinal()];
        }

        public boolean match(byte b) {
            return (b & mask) == byteValue;
        }

    }
}