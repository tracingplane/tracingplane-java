package edu.brown.cs.systems.baggage.data;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.common.primitives.UnsignedBytes;

public class DataUtils {
	
	private static Comparator<byte[]> byteComparator = UnsignedBytes.lexicographicalComparator();
	
	/** Naive implementation of byte array comparison using guava comparator */
	public static int compare(byte[] a, byte[] b) {
		return byteComparator.compare(a, b);
	}
	
	public static int compare(Frame a, Frame b) {
		return compare(a.toByteArray(), b.toByteArray());
	}
	
	/** Merge two payloads */
	public static List<Frame> merge(List<Frame> a, List<Frame> b) {
		List<Frame> merged = new ArrayList<>(a.size() + b.size());
		int ia = 0, ib = 0;
		while (ia < a.size() && ib < b.size()) {
			Frame fa = a.get(ia), fb = b.get(ib);
			int comparison = compare(fa, fb);
			if (comparison == 0) {
				merged.add(fa);
				ia++; ib++;
			} else if (comparison < 0) {
				merged.add(fa);
				ia++;
			} else {
				merged.add(fb);
				ib++;
			}
		}
		while (ia < a.size()) {
			merged.add(a.get(ia++));
		}
		while (ib < b.size()) {
			merged.add(b.get(ib++));
		}
		return merged;
	}
	
	public static List<Frame> drop(List<Frame> input, int maxSize) {
		int totalBytes = 0;
		for (int i = 0; i < input.size(); i++) {
			totalBytes += input.get(i).toByteArray().length;
			if (totalBytes > maxSize) {
				return input.subList(0, i);
			}
		}
		return input;
	}
	
	/** Naive implementation of byte array equals */
	public static boolean equals(byte[] a, byte[] b) {
		if (a.length == b.length) {
			return compare(a, b) == 0;
		} else {
			return false;
		}
	}
	
//	public static int sizeOfLexVarUInt64(long value) {
//		if (value < 0) {
//			return 9;
//		}
//		long cutoff = 128;
//		for (int i = 1; i < 9; i++) {
//			if (value < cutoff) {
//				return i;
//			} else {
//				cutoff *= 128;
//			}
//		}
//		return 9;
//	}
//	
//	public static int writeLexVarUInt32(ByteBuffer buf, int value) {
//		return writeLexVarUInt64(buf, value);
//	}
//	
//	/** Writes lexvarint32, returns number of bytes written */
//	public static int writeLexVarUInt64(ByteBuffer buf, long value) {
//		int intsize = sizeOfLexVarUInt64(value);
//		byte b0 = (byte) (0xff << (9-intsize));
//		byte mask = (byte) ~b0;
//		buf.put((byte) (b0 | ((value >>> (8*(intsize-1))) & mask)));
//		for (int i = intsize-1; i > 0; i--) {
//			buf.put((byte) (value >>> (8*(i-1))));
//		}
//		return intsize;
//	}
//	
//	public static void main(String[] args) {
//		System.out.println(toBinaryString((byte) 35));
//		System.out.println(toBinaryString((byte) (Byte.MIN_VALUE+35)));
//	}
//	
//	/** Reads a varint that is lexicographically comparable with other varints */
//	public static int readLexVarUInt32(ByteBuffer buf) throws DataLayerException {
//		byte b = buf.get();
//		if (b >= 0) {
//			return b;
//		} else {
//			int prefix = lexVarPrefixSize(b);
//			if (prefix > 4) {
//				throw new DataLayerException("Invalid LexVarInt32 " + toBinaryString(b));
//			} else if (prefix == 4 && b != -16) {
//				long encoded_value = ((b & (0xff >>> prefix)) << (8 * prefix)) + readUInt64(buf, prefix);
//				throw new DataLayerException("LexVarInt32 too big! " + encoded_value + " > " + Integer.MAX_VALUE);
//			} else {
//				return (int) (((b & (0xff >>> prefix)) << (8 * prefix)) + readUInt64(buf, prefix));
//			}
//		}
//	}
//	
//	/** Reads a varint that is lexicographically comparable with other varints */
//	public static long readLexVarUInt64(ByteBuffer buf) throws DataLayerException {
//		byte b = buf.get();
//		if (b >= 0) {
//			return b;
//		} else {
//			int prefix = lexVarPrefixSize(b);
//			long result = b & (0xff >>> prefix);
//			result <<= (8 * prefix);
//			result += readUInt64(buf, prefix);
//			return result;
//		}
//	}
//	
//	static int lsb(byte b, int count) {
//		return b & (0xff >>> count);
//	}
//	
//	static int lexVarPrefixSize(byte b) {
//		for (int i = 0; i < 8; i++) {
//			if ((b & (0x80 >>> i)) == 0) {
//				return i;
//			}
//		}
//		return 8;
//	}
//	
//	public static long readUInt64(ByteBuffer buf, int numBytes) throws DataLayerException {
//		if (numBytes > 8 || numBytes <= 0) {
//			throw new DataLayerException("Invalid UInt64 with " + numBytes + " bytes");
//		}
//		long result = 0;
//		for (int i = 0; i < numBytes; i++) {
//			result = (result << 8) + (buf.get() & 0xff);
//		}
//		return result;
//	}
//	
//	public static byte[] writeLexVarUint32(int value) throws DataLayerException {
//		return null;
//	}
	
	public static String toBinaryString(byte b) {
		return String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
	}
	
	public static String toHexString(byte b) {
		return String.format("%2s", Integer.toHexString(b & 0xFF)).replace(' ', '0');
	}
}
