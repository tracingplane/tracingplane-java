package edu.brown.cs.systems.baggage.datalayer;

import java.util.Comparator;

import com.google.common.primitives.UnsignedBytes;

public class DataUtils {
	
	private static Comparator<byte[]> byteComparator = UnsignedBytes.lexicographicalComparator();
	
	/** Naive implementation of byte array comparison using guava comparator */
	public static int compare(byte[] a, byte[] b) {
		return byteComparator.compare(a, b);
	}
	
//	/** Merge two payloads */
//	public static List<ByteBuffer> merge(List<ByteBuffer> a, List<ByteBuffer> b) {
//		List<Frame> merged = new ArrayList<>(a.size() + b.size());
//		int ia = 0, ib = 0;
//		while (ia < a.size() && ib < b.size()) {
//			Frame fa = a.get(ia), fb = b.get(ib);
//			int comparison = compare(fa, fb);
//			if (comparison == 0) {
//				merged.add(fa);
//				ia++; ib++;
//			} else if (comparison < 0) {
//				merged.add(fa);
//				ia++;
//			} else {
//				merged.add(fb);
//				ib++;
//			}
//		}
//		while (ia < a.size()) {
//			merged.add(a.get(ia++));
//		}
//		while (ib < b.size()) {
//			merged.add(b.get(ib++));
//		}
//		return merged;
//	}
	
//	public static List<Frame> drop(List<Frame> input, int maxSize) {
//		int totalBytes = 0;
//		for (int i = 0; i < input.size(); i++) {
//			totalBytes += input.get(i).toByteArray().length;
//			if (totalBytes > maxSize) {
//				return input.subList(0, i);
//			}
//		}
//		return input;
//	}
	
	/** Naive implementation of byte array equals */
	public static boolean equals(byte[] a, byte[] b) {
		if (a.length == b.length) {
			return compare(a, b) == 0;
		} else {
			return false;
		}
	}
	
	public static String toBinaryString(byte b) {
		return String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
	}
	
	public static String toHexString(byte b) {
		return String.format("%2s", Integer.toHexString(b & 0xFF)).replace(' ', '0');
	}
	
	public static byte makeByte(String bitPattern) {
		bitPattern = bitPattern.replaceAll(" ", "");
		int[] ints = new int[bitPattern.length()];
		for (int i = 0; i < bitPattern.length(); i++) {
			ints[i] = Integer.valueOf(bitPattern.substring(i, i+1));
		}
		return makeByte(ints);
	}
	
	public static byte makeByte(int... bitPattern) {
		byte result = 0;
		for (int bit : bitPattern) {
			result = (byte) ((result << 1) ^ (bit == 0 ? 0 : 1));
		}
		return result;
	}
}
