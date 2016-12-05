package edu.brown.cs.systems.tracingplane.atom_layer.types;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.primitives.UnsignedBytes;

public class Lexicographic {

	public static final Comparator<byte[]> BYTE_ARRAY_COMPARATOR = UnsignedBytes.lexicographicalComparator();
	public static final Comparator<ByteBuffer> BYTE_BUFFER_COMPARATOR = UnsignedByteBuffer.lexicographicalComparator();

	/** Compares the two byte arrays lexicographically */
	public static int compare(byte[] a, byte[] b) {
		return BYTE_ARRAY_COMPARATOR.compare(a, b);
	}

	/** Compares the two byte buffers lexicographically */
	public static int compare(ByteBuffer a, ByteBuffer b) {
		return BYTE_BUFFER_COMPARATOR.compare(a, b);
	}

	/**
	 * Sort the provided array of bytebuffers lexicographically. The original
	 * array is modified. Returns the original array.
	 */
	public static ByteBuffer[] sort(ByteBuffer[] bufs) {
		Arrays.sort(bufs, BYTE_BUFFER_COMPARATOR);
		return bufs;
	}

	/**
	 * Sort the provided list of bytebuffers lexicographically. The original
	 * list is modified. Returns the original list.
	 */
	public static List<ByteBuffer> sort(List<ByteBuffer> bufs) {
		Collections.sort(bufs, BYTE_BUFFER_COMPARATOR);
		return bufs;
	}

	/**
	 * Merges the provided bytebuffers using lexicographical comparison to
	 * determine the merge order. Duplicate entries are discarded
	 */
	public static List<ByteBuffer> merge(List<ByteBuffer> a, List<ByteBuffer> b) {
		if (a == b) {
			return a;
		}
		int ia = 0, ib = 0, size_a = a.size(), size_b = b.size();
		final List<ByteBuffer> merged = new ArrayList<>(size_a + size_b);
		boolean different = false;
		while (ia < size_a && ib < size_b) {
			int comparison = BYTE_BUFFER_COMPARATOR.compare(a.get(ia), b.get(ib));
			if (comparison == 0) {
				merged.add(a.get(ia));
				ia++;
				ib++;
			} else if (comparison < 0) {
				merged.add(a.get(ia));
				ia++;
				different = true;
			} else if (comparison > 0) {
				merged.add(b.get(ib));
				ib++;
				different = true;
			}
		}
		
		while (ia < size_a) {
			merged.add(a.get(ia++));
		}
		
		while (ib < size_b) {
			merged.add(b.get(ib++));
		}
		
		if (different) {
			return merged;
		} else {
			return a;
		}
	}

	/**
	 * Write a 32 bit signed integer value into the provided buffer, encoded as
	 * a lexicographically comparable varint
	 */
	public static int writeVarInt32(ByteBuffer buf, int value) {
		return SignedLexVarint.writeLexVarInt32(buf, value);
	}

	/**
	 * Write a 64 bit signed integer value into the provided buffer, encoded as
	 * a lexicographically comparable varint
	 */
	public static int writeVarInt64(ByteBuffer buf, long value) {
		return SignedLexVarint.writeLexVarInt64(buf, value);
	}

	/**
	 * Write a 32 bit unsigned integer value into the provided buffer, encoded
	 * as a lexicographically comparable varint
	 */
	public static int writeVarUInt32(ByteBuffer buf, int value) {
		return UnsignedLexVarint.writeLexVarUInt32(buf, value);
	}

	/**
	 * Write a 64 bit unsigned integer value into the provided buffer, encoded
	 * as a lexicographically comparable varint
	 */
	public static int writeVarUInt64(ByteBuffer buf, int value) {
		return UnsignedLexVarint.writeLexVarUInt64(buf, value);
	}

	/**
	 * Returns the byte representation of the provided 32 bit signed integer
	 * encoded as a lexicographically comparable varint
	 */
	public static byte[] writeVarInt32(int value) {
		ByteBuffer buf = ByteBuffer.allocate(SignedLexVarint.encodedLength(value));
		SignedLexVarint.writeLexVarInt32(buf, value);
		return buf.array();
	}

	/**
	 * Returns the byte representation of the provided 64 bit signed integer
	 * encoded as a lexicographically comparable varint
	 */
	public static byte[] writeVarInt64(long value) {
		ByteBuffer buf = ByteBuffer.allocate(SignedLexVarint.encodedLength(value));
		SignedLexVarint.writeLexVarInt64(buf, value);
		return buf.array();
	}

	/**
	 * Returns the byte representation of the provided 32 bit unsigned integer
	 * encoded as a lexicographically comparable varint
	 */
	public static byte[] writeVarUInt32(int value) {
		ByteBuffer buf = ByteBuffer.allocate(UnsignedLexVarint.encodedLength(value));
		UnsignedLexVarint.writeLexVarUInt32(buf, value);
		return buf.array();
	}

	/**
	 * Returns the byte representation of the provided 64 bit unsigned integer
	 * encoded as a lexicographically comparable varint
	 */
	public static byte[] writeVarUInt64(long value) {
		ByteBuffer buf = ByteBuffer.allocate(UnsignedLexVarint.encodedLength(value));
		UnsignedLexVarint.writeLexVarUInt64(buf, value);
		return buf.array();
	}

	/**
	 * Reads a 32 bit signed varint from the provided buffer
	 */
	public static int readVarInt32(ByteBuffer buf) throws AtomLayerException {
		return SignedLexVarint.readLexVarInt32(buf);
	}

	/**
	 * Reads a 64 bit signed varint from the provided buffer
	 */
	public static long readVarInt64(ByteBuffer buf) throws AtomLayerException {
		return SignedLexVarint.readLexVarInt64(buf);
	}

	/**
	 * Reads a 32 bit unsigned varint from the provided buffer
	 */
	public static int readVarUInt32(ByteBuffer buf) throws AtomLayerException {
		return UnsignedLexVarint.readLexVarUInt32(buf);
	}

	/**
	 * Reads a 64 bit unsigned varint from the provided buffer
	 */
	public static long readVarUInt64(ByteBuffer buf) throws AtomLayerException {
		return UnsignedLexVarint.readLexVarUInt64(buf);
	}

	/**
	 * Reads a 32 bit signed varint from the provided buffer
	 */
	public static int readVarInt32(byte[] bytes) throws AtomLayerException {
		return SignedLexVarint.readLexVarInt32(ByteBuffer.wrap(bytes));
	}

	/**
	 * Reads a 64 bit signed varint from the provided buffer
	 */
	public static long readVarInt64(byte[] bytes) throws AtomLayerException {
		return SignedLexVarint.readLexVarInt64(ByteBuffer.wrap(bytes));
	}

	/**
	 * Reads a 32 bit unsigned varint from the provided buffer
	 */
	public static int readVarUInt32(byte[] bytes) throws AtomLayerException {
		return UnsignedLexVarint.readLexVarUInt32(ByteBuffer.wrap(bytes));
	}

	/**
	 * Reads a 64 bit unsigned varint from the provided buffer
	 */
	public static long readVarUInt64(byte[] bytes) throws AtomLayerException {
		return UnsignedLexVarint.readLexVarUInt64(ByteBuffer.wrap(bytes));
	}

}
