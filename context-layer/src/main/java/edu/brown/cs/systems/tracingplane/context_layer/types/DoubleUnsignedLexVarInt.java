package edu.brown.cs.systems.tracingplane.context_layer.types;

import java.nio.ByteBuffer;

/**
 * Varint that encodes two integers in such a way that the lexicographic
 * comparison of the binary encoding is consistent with the numeric comparison
 * of the numeric values.
 *
 */
public class DoubleUnsignedLexVarInt {

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

	public static int readLexVarUInt32(ByteBuffer buf) throws ContextLayerException {
		return (int) readLexVarUInt64(buf);
	}

	/**
	 * Reads a varint that is lexicographically comparable with other varints
	 */
	public static long readLexVarUInt64(ByteBuffer buf) throws ContextLayerException {
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

	static long readUInt64(ByteBuffer buf, int numBytes) throws ContextLayerException {
		if (numBytes > 8 || numBytes <= 0) {
			throw new ContextLayerException("Invalid UInt64 with " + numBytes + " bytes");
		}
		long result = 0;
		for (int i = 0; i < numBytes; i++) {
			result = (result << 8) + (buf.get() & 0xff);
		}
		return result;
	}
}
