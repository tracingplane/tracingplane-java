package edu.brown.cs.systems.tracingplane.atom_layer.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.nio.ByteBuffer;
import java.util.Random;
import org.junit.Test;

public class TestXSignedVarint64 {

    private static ByteBuffer make(String... ss) {
        ByteBuffer buf = ByteBuffer.allocate(ss.length);
        for (String s : ss) {
            buf.put(TypeUtils.makeByte(s));
        }
        buf.rewind();
        return buf;
    }

    private static byte[] write(long value) {
        ByteBuffer buf = ByteBuffer.allocate(9);
        SignedLexVarint.writeLexVarInt64(buf, value);
        buf.flip();
        byte[] bytes = new byte[buf.remaining()];
        buf.get(bytes);
        return bytes;
    }

    private static String writeString(long value) {
        byte[] bytes = write(value);
        String s = "";
        for (byte b : bytes) {
            s = s + TypeUtils.toBinaryString(b);
        }
        return s;
    }

    @Test
    public void testXSignedVarint64Simple() throws AtomLayerException {
        assertEquals(Long.MIN_VALUE,
                     SignedLexVarint.readLexVarInt64(make("00000000", "00000000", "00000000", "00000000", "00000000",
                                                          "00000000", "00000000", "00000000", "00000000")));
        assertEquals(-65, SignedLexVarint.readLexVarInt64(make("0011 1111", "1011 1111")));
        assertEquals(-64, SignedLexVarint.readLexVarInt64(make("0100 0000")));
        assertEquals(-19, SignedLexVarint.readLexVarInt64(make("0110 1101")));
        assertEquals(-4, SignedLexVarint.readLexVarInt64(make("0111 1100")));
        assertEquals(-1, SignedLexVarint.readLexVarInt64(make("0111 1111")));
        assertEquals(0, SignedLexVarint.readLexVarInt64(make("1000 0000")));
        assertEquals(1, SignedLexVarint.readLexVarInt64(make("1000 0001")));
        assertEquals(19, SignedLexVarint.readLexVarInt64(make("1001 0011")));
        assertEquals(63, SignedLexVarint.readLexVarInt64(make("1011 1111")));
        assertEquals(64, SignedLexVarint.readLexVarInt64(make("1100 0000", "0100 0000")));
        assertEquals(Long.MAX_VALUE,
                     SignedLexVarint.readLexVarInt64(make("11111111", "11111111", "11111111", "11111111", "11111111",
                                                          "11111111", "11111111", "11111111", "11111111")));
    }

    @Test
    public void testXSignedVarint64Simple2() throws AtomLayerException {
        assertEquals("000000000000000000000000000000000000000000000000000000000000000000000000",
                     writeString(Long.MIN_VALUE));
        assertEquals("0011111110111111", writeString(-65));
        assertEquals("01000000", writeString(-64));
        assertEquals("01101101", writeString(-19));
        assertEquals("01111100", writeString(-4));
        assertEquals("01111111", writeString(-1));
        assertEquals("10000000", writeString(-0));
        assertEquals("10000001", writeString(1));
        assertEquals("10010011", writeString(19));
        assertEquals("10111111", writeString(63));
        assertEquals("1100000001000000", writeString(64));
        assertEquals("111111111111111111111111111111111111111111111111111111111111111111111111",
                     writeString(Long.MAX_VALUE));
    }

    @Test
    public void testEstimatedSize() {
        assertEquals(1, SignedLexVarint.encodedLength((long) -64));
        assertEquals(1, SignedLexVarint.encodedLength((long) -1));
        assertEquals(1, SignedLexVarint.encodedLength((long) 0));
        assertEquals(1, SignedLexVarint.encodedLength((long) 63));

        assertEquals(2, SignedLexVarint.encodedLength((long) -64 * 128));
        assertEquals(2, SignedLexVarint.encodedLength((long) -64 - 1));
        assertEquals(2, SignedLexVarint.encodedLength((long) 64));
        assertEquals(2, SignedLexVarint.encodedLength((long) 64 * 128 - 1));

        assertEquals(3, SignedLexVarint.encodedLength((long) -64 * 128 * 128));
        assertEquals(3, SignedLexVarint.encodedLength((long) -64 * 128 - 1));
        assertEquals(3, SignedLexVarint.encodedLength((long) 64 * 128));
        assertEquals(3, SignedLexVarint.encodedLength((long) 64 * 128 * 128 - 1));

        assertEquals(4, SignedLexVarint.encodedLength((long) -64 * 128 * 128 * 128));
        assertEquals(4, SignedLexVarint.encodedLength((long) -64 * 128 * 128 - 1));
        assertEquals(4, SignedLexVarint.encodedLength((long) 64 * 128 * 128));
        assertEquals(4, SignedLexVarint.encodedLength((long) 64 * 128 * 128 * 128 - 1));

        assertEquals(5, SignedLexVarint.encodedLength((long) -64 * 128 * 128 * 128 * 128));
        assertEquals(5, SignedLexVarint.encodedLength((long) -64 * 128 * 128 * 128 - 1));
        assertEquals(5, SignedLexVarint.encodedLength((long) 64 * 128 * 128 * 128));
        assertEquals(5, SignedLexVarint.encodedLength((long) Integer.MAX_VALUE));
        assertEquals(5, SignedLexVarint.encodedLength((long) Integer.MIN_VALUE));
        assertEquals(5, SignedLexVarint.encodedLength((long) 64 * 128 * 128 * 128 * 128 - 1));

        assertEquals(6, SignedLexVarint.encodedLength((long) -64 * 128 * 128 * 128 * 128 * 128));
        assertEquals(6, SignedLexVarint.encodedLength((long) -64 * 128 * 128 * 128 * 128 - 1));
        assertEquals(6, SignedLexVarint.encodedLength((long) 64 * 128 * 128 * 128 * 128));
        assertEquals(6, SignedLexVarint.encodedLength((long) 64 * 128 * 128 * 128 * 128 * 128 - 1));

        assertEquals(7, SignedLexVarint.encodedLength((long) -64 * 128 * 128 * 128 * 128 * 128 * 128));
        assertEquals(7, SignedLexVarint.encodedLength((long) -64 * 128 * 128 * 128 * 128 * 128 - 1));
        assertEquals(7, SignedLexVarint.encodedLength((long) 64 * 128 * 128 * 128 * 128 * 128));
        assertEquals(7, SignedLexVarint.encodedLength((long) 64 * 128 * 128 * 128 * 128 * 128 * 128 - 1));

        assertEquals(8, SignedLexVarint.encodedLength((long) -64 * 128 * 128 * 128 * 128 * 128 * 128 * 128));
        assertEquals(8, SignedLexVarint.encodedLength((long) -64 * 128 * 128 * 128 * 128 * 128 * 128 - 1));
        assertEquals(8, SignedLexVarint.encodedLength((long) 64 * 128 * 128 * 128 * 128 * 128 * 128));
        assertEquals(8, SignedLexVarint.encodedLength((long) 64 * 128 * 128 * 128 * 128 * 128 * 128 * 128 - 1));

        assertEquals(9, SignedLexVarint.encodedLength((long) -64 * 128 * 128 * 128 * 128 * 128 * 128 * 128 * 128));
        assertEquals(9, SignedLexVarint.encodedLength((long) -64 * 128 * 128 * 128 * 128 * 128 * 128 * 128 - 1));
        assertEquals(9, SignedLexVarint.encodedLength((long) 64 * 128 * 128 * 128 * 128 * 128 * 128 * 128));
        assertEquals(9, SignedLexVarint.encodedLength((long) 64 * 128 * 128 * 128 * 128 * 128 * 128 * 128 * 128 - 1));
        assertEquals(9, SignedLexVarint.encodedLength((long) Long.MAX_VALUE));
        assertEquals(9, SignedLexVarint.encodedLength((long) Long.MIN_VALUE));
    }

    @Test
    public void testXSignedVarInt64EncodeDecode() throws AtomLayerException {
        int numtests = 1000;
        Random r = new Random(0);

        long min = 0;
        long max = 64;

        for (int size = 1; size < 9; size++) {
            ByteBuffer b = ByteBuffer.allocate(size);
            for (int i = 0; i < numtests; i++) {
                long value;
                do {
                    value = r.nextLong() % (max - min);
                } while (value < 0);
                value += min;
                assertTrue(value >= min);
                assertTrue(value < max);

                b.rewind();
                int sizeWritten = SignedLexVarint.writeLexVarInt64(b, value);
                assertEquals(size, sizeWritten);

                b.rewind();
                long valueRead = SignedLexVarint.readLexVarInt64(b);
                assertEquals(value, valueRead);

                b.rewind();
                sizeWritten = SignedLexVarint.writeLexVarInt64(b, -value - 1);
                assertEquals(size, sizeWritten);

                b.rewind();
                valueRead = SignedLexVarint.readLexVarInt64(b);
                assertEquals(-value - 1, valueRead);
            }

            min = max;
            max *= 128;
        }

        ByteBuffer b = ByteBuffer.allocate(9);
        for (int i = 0; i < numtests; i++) {
            long value;
            do {
                value = r.nextLong();
            } while (value < Long.MAX_VALUE / 2);

            assertTrue(value >= Long.MAX_VALUE / 2);
            assertTrue(value < Long.MAX_VALUE);

            b.rewind();
            int sizeWritten = SignedLexVarint.writeLexVarInt64(b, value);
            assertEquals(9, sizeWritten);

            b.rewind();
            long valueRead = SignedLexVarint.readLexVarInt64(b);
            assertEquals(value, valueRead);

            b.rewind();
            sizeWritten = SignedLexVarint.writeLexVarInt64(b, -value - 1);
            assertEquals(9, sizeWritten);

            b.rewind();
            valueRead = SignedLexVarint.readLexVarInt64(b);
            assertEquals(-value - 1, valueRead);
        }
    }

    private static final Random r = new Random(7);

    private static long generate(int size) {
        if (size == 9) {
            long value;
            do {
                value = r.nextLong();
            } while (value <= Long.MAX_VALUE / 2);
            return value;
        }
        long min = 0;
        long max = 64;
        for (; size > 1; size--) {
            min = max;
            max *= 128;
        }
        long value;
        do {
            value = r.nextLong() % (max - min);
        } while (value < 0);
        return value + min;
    }

    @Test
    public void testSignedVarint64Comparison() {
        int numtests = 100;
        for (int sizea = 1; sizea <= 9; sizea++) {
            ByteBuffer bufa = ByteBuffer.allocate(sizea);
            for (int sizeb = sizea; sizeb <= 9; sizeb++) {
                ByteBuffer bufb = ByteBuffer.allocate(sizeb);
                for (int i = 0; i < numtests; i++) {
                    long valuea = generate(sizea);
                    long valueb = generate(sizeb);

                    for (long a : new long[] { valuea, -valuea - 1 }) {
                        for (long b : new long[] { valueb, -valueb - 1 }) {

                            bufa.rewind();
                            assertEquals(sizea, SignedLexVarint.writeLexVarInt64(bufa, a));

                            bufb.rewind();
                            assertEquals(sizeb, SignedLexVarint.writeLexVarInt64(bufb, b));

                            assertEquals(Long.compare(a, b) == 0,
                                         Lexicographic.compare(bufa.array(), bufb.array()) == 0);
                            assertEquals(Long.compare(a, b) < 0, Lexicographic.compare(bufa.array(), bufb.array()) < 0);
                            assertEquals(Long.compare(b, a) < 0, Lexicographic.compare(bufb.array(), bufa.array()) < 0);
                        }
                    }
                }
            }
        }
    }
}
