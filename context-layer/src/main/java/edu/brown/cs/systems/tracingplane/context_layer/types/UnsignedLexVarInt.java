package edu.brown.cs.systems.tracingplane.context_layer.types;

import java.nio.ByteBuffer;

import edu.brown.cs.systems.tracingplane.context_layer.DataLayerException;

/**
 * Unsigned varint (32 bit and 64 bit) that is encoded in such a way that the
 * lexicographic comparison of the binary encoding is consistent with the
 * numeric comparison of the numeric values.
 * 
 * For example, consider the integers 130, 257, and 16385
 * 
 * 130 < 257 < 16385
 * 
 * However, using protobuf-style varints, their binary encodings are:
 * 
 * 130: 1000 0100 0000 0001 257: 1000 0010 0000 0010 16385: 1000 0001 1000 0000
 * 0000 0001
 * 
 * However, the lexicographical comparison of these encodings has 16385 < 257 <
 * 130
 * 
 * This happens because the first bit of each byte of a protobuf-style varint
 * says whether there are more bytes in the binary representation (1 means 'more
 * bytes', 0 means 'no more bytes'). Also, protobuf-style varints switch the
 * order of bytes of the encoded integer (that is, least- significant bytes go
 * first).
 * 
 * To make binary encodings lexicographically comparable, we must place all of
 * the 'more bytes' prefixes at the start of the encoded representation, and
 * encode bytes with most-significant bytes first.
 * 
 * For unsigned 64-bit integers, values in the range [0, 2^7) use 1 byte; [2^7,
 * 2^14) use 2 bytes, [2^14, 2^21) use 3 bytes, and so on. Negative 64-bit
 * unsigned integers use 9 bytes. Negative 32-bit unsigned integeers use 5
 * bytes.
 * 
 * @author jon
 *
 */
public class UnsignedLexVarInt {

	/**
	 * @param value
	 *            Any integer value
	 * @return the length of the encoded representation of this value. Values in
	 *         the range [0, 2^7) use 1 byte; [2^7, 2^14) use 2 bytes, and so
	 *         on. Negative values use 5 bytes.
	 */
	public static int encodedLength(int value) {
		if (value < 0) {
			return 5;
		}
		long cutoff = 128;
		for (int i = 1; i < 5; i++) {
			if (value < cutoff) {
				return i;
			} else {
				cutoff *= 128;
			}
		}
		return 5;
	}

	/**
	 * @param value
	 *            Any long value
	 * @return the length of the encoded representation of this value. Values in
	 *         the range [0, 2^7) use 1 byte; [2^7, 2^14) use 2 bytes, and so
	 *         on. Negative values use 9 bytes.
	 */
	public static int encodedLength(long value) {
		if (value < 0) {
			return 9;
		}
		long cutoff = 128;
		for (int i = 1; i < 9; i++) {
			if (value < cutoff) {
				return i;
			} else {
				cutoff *= 128;
			}
		}
		return 9;
	}

	public static int writeLexVarUInt32(ByteBuffer buf, int value) {
		if (value < 0) {
			// Make positive so that it encodes in only 5 bytes instead of 9
			return writeLexVarUInt64(buf, value + 4294967296L);
		} else {
			return writeLexVarUInt64(buf, value);
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
	
	public static int readLexVarUInt32(ByteBuffer buf) throws DataLayerException {
		return (int) readLexVarUInt64(buf);
	}

	/**
	 * Reads a varint that is lexicographically comparable with other varints
	 */
	public static long readLexVarUInt64(ByteBuffer buf) throws DataLayerException {
		byte b0 = buf.get();
		int size = interpretSize(b0);
		if (size == 1) {
			return b0;
		}
		long result = b0 & (0xff >>> size);
		result <<= (8 * (size-1));
		result += readUInt64(buf, (size-1));
		return result;
	}

	static int interpretSize(byte b) {
		if (b > 0) {
			return 1;
		}
		for (int i = 0; i < 8; i++) {
			if ((b & (0x80 >>> i)) == 0) {
				return i+1;
			}
		}
		return 9;
	}

	static long readUInt64(ByteBuffer buf, int numBytes) throws DataLayerException {
		if (numBytes > 8 || numBytes <= 0) {
			throw new DataLayerException("Invalid UInt64 with " + numBytes + " bytes");
		}
		long result = 0;
		for (int i = 0; i < numBytes; i++) {
			result = (result << 8) + (buf.get() & 0xff);
		}
		return result;
	}
}
