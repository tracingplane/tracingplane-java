package edu.brown.cs.systems.baggage.datalayer.impl;

public class Utils {
	
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
