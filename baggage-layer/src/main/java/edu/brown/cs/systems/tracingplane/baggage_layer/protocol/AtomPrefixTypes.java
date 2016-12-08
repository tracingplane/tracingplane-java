package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

/** This class has the bitwise logic for different bags prefixes.
 * 
 * For now the protocol is such that the first byte of each atom is its prefix, and the remaining bytes are its
 * payload. */
public class AtomPrefixTypes {

    private AtomPrefixTypes() {}

    private static final AtomType[] bagTypes = new AtomType[4];
    private static final HeaderType[] headerTypes = new HeaderType[4];

    /** The first two bits of a prefix is the atom type:
     * 
     * 00 is not currently used <br />
     * 01 is a data atom <br />
     * 10 is a header atom <br />
     * 11 is not currently used <br />
     * 
     * There is also an overflow marker {@link BaggageAtoms.OVERFLOW_MARKER} that is just the empty byte array (ie, it
     * has no prefix) and therefore lexicographically less than all other atoms */
    public enum AtomType {
        Data(1), Header(2);

        public final int id;
        public final byte byteValue;

        private AtomType(int id) {
            this.id = id;
            this.byteValue = (byte) ((id << 6) & 0xC0);
            bagTypes[id] = this;
        }

        public static AtomType fromByte(byte b) {
            int id = (b & 0xC0) >>> 6;
            return bagTypes[id];
        }

        public boolean match(byte b) {
            return (b & 0xC0) == byteValue;
        }
    }

    /** <p>
     * Levels are used by headers. Within a header prefix, the middle four bits specify the {@link Level} of the header,
     * while the final two bits specify the {@link HeaderType}.
     * </p>
     * 
     * Since four bits are used for a level, there are 16 possible valid levels. Levels {@code l} is encoded as
     * {@code 15 - l} to ensure that, lexicographically, lower levels come last. Specifically:
     * 
     * level 0 is 1111 <br />
     * level 1 is 1110 <br />
     * level 2 is 1101 <br />
     * level 3 is 1100 <br />
     * level 4 is 1011 <br />
     * level 5 is 1010 <br />
     * level 6 is 1001 <br />
     * level 7 is 1000 <br />
     * level 8 is 0111 <br />
     * level 9 is 0110 <br />
     * level 10 is 0101 <br />
     * level 11 is 0100 <br />
     * level 12 is 0011 <br />
     * level 13 is 0010 <br />
     * level 14 is 0001 <br />
     * level 15 is 0000 <br />
    */
    public static class Level {

        public static final int LEVELS = 16;
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
            this.byteValue = (byte) (((15 - level) << 2) & 0x3C);
        }

        public static Level get(int level) {
            return levels[level];
        }

        public static int valueOf(byte b) {
            return 15 - ((b & 0x3C) >> 2);
        }

        public static Level fromByte(byte b) {
            return levels[valueOf(b)];
        }

        public boolean match(byte b) {
            return (b & 0x3C) == byteValue;
        }

    }

    /** There are two different types of header. Within a header prefix, the middle four bits specify the {@link Level}
     * of the header, while the final two bits specify the {@link HeaderType}.
     * 
     * 00 is not currently used <br />
     * 01 is an indexed header <br />
     * 10 is a keyed header <br />
     * 11 is not currently used */
    public enum HeaderType {
        Indexed(1), Keyed(2);

        public final int id;
        public final byte byteValue;

        private HeaderType(int id) {
            this.id = id;
            this.byteValue = (byte) (id & 0x03);
        }

        /** Inspect the final two bits of the provided byte and return the corresponding {@link HeaderType} */
        public static HeaderType fromByte(byte b) {
            int id = b & 0x03;
            return headerTypes[id];
        }

        public boolean match(byte b) {
            return (b & 0x03) == byteValue;
        }

    }
}