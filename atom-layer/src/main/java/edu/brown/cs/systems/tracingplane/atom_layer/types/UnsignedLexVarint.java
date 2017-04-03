package edu.brown.cs.systems.tracingplane.atom_layer.types;

import java.nio.ByteBuffer;

/**
 * <p>
 * Unsigned varint (32 bit and 64 bit) that is encoded in such a way that the lexicographic comparison of the binary
 * encoding is consistent with the numeric comparison of the numeric values.
 * </p>
 * 
 * <p>
 * For example, consider the integers 130, 257, and 16385. Numerically, {@code 130 < 257 < 16385}. Consider their binary
 * encodings using protobuf-style varints:
 * </p>
 * 
 * <ul>
 * <li>130: 1000 0100 0000 0001</li>
 * <li>257: 1000 0010 0000 0010</li>
 * <li>16385: 1000 0001 1000 0000 0000 0001</li>
 * </ul>
 * 
 * <p>
 * The lexicographical comparison of these encodings will give us {@code 16385 < 257 < 130}.
 * </p>
 * 
 * <p>
 * This happens because the first bit of each byte of a protobuf-style varint says whether there are more bytes in the
 * binary representation (1 means 'more bytes', 0 means 'no more bytes'). Also, protobuf-style varints switch the order
 * of bytes of the encoded integer (that is, least- significant bytes go first).
 * </p>
 * 
 * <p>
 * To make binary encodings lexicographically comparable, we must place all of the 'more bytes' prefixes at the start of
 * the encoded representation, and encode bytes with most-significant bytes first.
 * </p>
 * 
 * <p>
 * For unsigned 64-bit integers, values in the range {@code [0, 2^7)} use 1 byte; {@code [2^7, 2^14)} use 2 bytes,
 * {@code [2^14, 2^21)} use 3 bytes, and so on. Negative 64-bit unsigned integers use 9 bytes. Negative 32-bit unsigned
 * integeers use 5 bytes.
 * </p>
 * 
 * @author jon
 */
public class UnsignedLexVarint {

    public static void main(String[] args) {
        long v = 128;
        for (int i = 0; i < 9; i++) {
            long mask = -v;
            System.out.println(v + " = 0x" + Long.toHexString(mask).toUpperCase() + "L");
            v *= 128;
        }
    }

    /**
     * @param value Any integer value
     * @return the length of the encoded representation of this value. Values in the range [0, 2^7) use 1 byte; [2^7,
     *         2^14) use 2 bytes, and so on. Negative values use 5 bytes.
     */
    public static int encodedLength(int value) {
        if ((value & 0xFFFFFF80) == 0) {
            return 1;
        } else if ((value & 0xFFFFC000) == 0) {
            return 2;
        } else if ((value & 0xFFE00000) == 0) {
            return 3;
        } else if ((value & 0xF0000000) == 0) {
            return 4;
        } else {
            return 5;
        }
    }

    /**
     * @param value Any long value
     * @return the length of the encoded representation of this value. Values in the range [0, 2^7) use 1 byte; [2^7,
     *         2^14) use 2 bytes, and so on. Negative values use 9 bytes.
     */
    public static int encodedLength(long value) {
        if ((value & 0xFFFFFFFFFFFFFF80L) == 0) {
            return 1;
        } else if ((value & 0xFFFFFFFFFFFFC000L) == 0) {
            return 2;
        } else if ((value & 0xFFFFFFFFFFE00000L) == 0) {
            return 3;
        } else if ((value & 0xFFFFFFFFF0000000L) == 0) {
            return 4;
        } else if ((value & 0xFFFFFFF800000000L) == 0) {
            return 5;
        } else if ((value & 0xFFFFFC0000000000L) == 0) {
            return 6;
        } else if ((value & 0xFFFE000000000000L) == 0) {
            return 7;
        } else if ((value & 0xFF00000000000000L) == 0) {
            return 8;
        } else {
            return 9;
        }
    }

    public static int writeLexVarUInt32(ByteBuffer buf, int value) {
        if (value < 0) {
            // Make positive so that it encodes in only 5 bytes instead of 9
            return writeLexVarUInt64(buf, value + 4294967296L);
        } else {
            return writeLexVarUInt64(buf, value);
        }
    }
    
    public static int writeReverseLexVarUInt32(ByteBuffer buf, int value) {
        if (value < 0) {
            // Make positive so that it encodes in only 5 bytes instead of 9
            return writeReverseLexVarUInt64(buf, value + 4294967296L);
        } else {
            return writeReverseLexVarUInt64(buf, value);
        }
    }

    public static int writeLexVarUInt64(ByteBuffer buf, long value) {
        int size = encodedLength(value);
        byte b0 = (byte) (0xff << (9 - size));
        byte mask = (byte) ~b0;
        buf.put((byte) (b0 | ((value >>> (8 * (size - 1))) & mask)));
        for (int i = size - 1; i > 0; i--) {
            buf.put((byte) (value >>> (8 * (i - 1))));
        }
        return size;
    }
    
    public static int writeReverseLexVarUInt64(ByteBuffer buf, long value) {
        int size = encodedLength(value);
        byte b0 = (byte) (0xff << (9 - size));
        byte mask = (byte) ~b0;
        buf.put((byte) ~(b0 | ((value >>> (8 * (size - 1))) & mask)));
        for (int i = size - 1; i > 0; i--) {
            buf.put((byte) ~(value >>> (8 * (i - 1))));
        }
        return size;
    }

    /** Reads a lexvar uint32 from the specified buf, advancing the buf's position */
    public static int readLexVarUInt32(ByteBuffer buf) throws AtomLayerException {
        return (int) readLexVarUInt64(buf);
    }

    /** Reads a lexvar uint32 from the specified buf without advancing the buf's position */
    public static int readLexVarUInt32(ByteBuffer buf, int position) throws AtomLayerException {
        return (int) readLexVarUInt64(buf, position);
    }

    /** Reads a varint that is lexicographically comparable with other varints and advance the buf's position */
    public static long readLexVarUInt64(ByteBuffer buf) throws AtomLayerException {
        byte b0 = buf.get();
        int size = interpretSize(b0);
        if (size == 1) {
            return b0;
        }
        long result = b0 & (0xff >>> size);
        result <<= (8 * (size - 1));
        result += readUInt64(buf, (size - 1));
        return result;
    }

    /** Reads a varint that is reverse lexicographically comparable with other varints and advance the buf's position */
    public static long readReverseLexVarUInt64(ByteBuffer buf) throws AtomLayerException {
        byte b0 = (byte) ~buf.get();
        int size = interpretSize(b0);
        if (size == 1) {
            return b0;
        }
        long result = b0 & (0xff >>> size);
        result <<= (8 * (size - 1));
        result += readReverseUInt64(buf, (size - 1));
        return result;
    }

    /** Reads a varint that is lexicographically comparable with other varints without advancing the buf's position */
    public static long readLexVarUInt64(ByteBuffer buf, int position) throws AtomLayerException {
        byte b0 = buf.get(position++);
        int size = interpretSize(b0);
        if (size == 1) {
            return b0;
        }
        long result = b0 & (0xff >>> size);
        result <<= (8 * (size - 1));
        result += readUInt64(buf, position, (size - 1));
        return result;
    }

    /** Reads a varint that is reverse lexicographically comparable with other varints without advancing the buf's position */
    public static long readReverseLexVarUInt64(ByteBuffer buf, int position) throws AtomLayerException {
        byte b0 = (byte) ~buf.get(position++);
        int size = interpretSize(b0);
        if (size == 1) {
            return b0;
        }
        long result = b0 & (0xff >>> size);
        result <<= (8 * (size - 1));
        result += readReverseUInt64(buf, position, (size - 1));
        return result;
    }

    static int interpretSize(byte b) {
        if (b > 0) {
            return 1;
        }
        for (int i = 0; i < 8; i++) {
            if ((b & (0x80 >>> i)) == 0) {
                return i + 1;
            }
        }
        return 9;
    }

    static long readUInt64(ByteBuffer buf, int numBytes) throws AtomLayerException {
        if (numBytes > 8 || numBytes <= 0) {
            throw new AtomLayerException("Invalid UInt64 with " + numBytes + " bytes");
        }
        long result = 0;
        for (int i = 0; i < numBytes; i++) {
            result = (result << 8) + (buf.get() & 0xff);
        }
        return result;
    }

    static long readUInt64(ByteBuffer buf, int position, int numBytes) throws AtomLayerException {
        if (numBytes > 8 || numBytes <= 0) {
            throw new AtomLayerException("Invalid UInt64 with " + numBytes + " bytes");
        }
        long result = 0;
        for (int i = 0; i < numBytes; i++) {
            result = (result << 8) + (buf.get(position++) & 0xff);
        }
        return result;
    }
    
    static long readReverseUInt64(ByteBuffer buf, int numBytes) throws AtomLayerException {
        if (numBytes > 8 || numBytes <= 0) {
            throw new AtomLayerException("Invalid UInt64 with " + numBytes + " bytes");
        }
        long result = 0;
        for (int i = 0; i < numBytes; i++) {
            result = (result << 8) + (~buf.get() & 0xff);
        }
        return result;
    }
    
    static long readReverseUInt64(ByteBuffer buf, int position, int numBytes) throws AtomLayerException {
        if (numBytes > 8 || numBytes <= 0) {
            throw new AtomLayerException("Invalid UInt64 with " + numBytes + " bytes");
        }
        long result = 0;
        for (int i = 0; i < numBytes; i++) {
            result = (result << 8) + (~buf.get(position++) & 0xff);
        }
        return result;
    }

    /**
     * Returns the byte representation of the provided 32 bit unsigned integer encoded as a lexicographically comparable
     * varint
     */
    public static byte[] writeVarUInt32(int value) {
        ByteBuffer buf = ByteBuffer.allocate(UnsignedLexVarint.encodedLength(value));
        UnsignedLexVarint.writeLexVarUInt32(buf, value);
        return buf.array();
    }

    /**
     * Returns the byte representation of the provided 64 bit unsigned integer encoded as a lexicographically comparable
     * varint
     */
    public static byte[] writeVarUInt64(long value) {
        ByteBuffer buf = ByteBuffer.allocate(UnsignedLexVarint.encodedLength(value));
        UnsignedLexVarint.writeLexVarUInt64(buf, value);
        return buf.array();
    }
}
