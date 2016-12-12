package edu.brown.cs.systems.tracingplane.atom_layer.types;

/**
 * Some useful utility methods related to bits and bytes
 */
public class TypeUtils {
    
    private TypeUtils() {}

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
            ints[i] = Integer.valueOf(bitPattern.substring(i, i + 1));
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
    
    public static int offset(byte mask) {
        for (int i = 1; i <= 8; i++) {
            if (((mask >> i) << i) != mask) {
                return i-1;
            }
        }
        return 8;
    }
}
