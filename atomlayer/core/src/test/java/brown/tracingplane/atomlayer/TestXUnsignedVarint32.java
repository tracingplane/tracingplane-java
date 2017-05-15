package brown.tracingplane.atomlayer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Random;
import org.junit.Test;
import brown.tracingplane.atomlayer.AtomLayerException;
import brown.tracingplane.atomlayer.Lexicographic;
import brown.tracingplane.atomlayer.TypeUtils;
import brown.tracingplane.atomlayer.UnsignedLexVarint;

public class TestXUnsignedVarint32 {

    @Test
    public void testLexVarInt32() throws AtomLayerException {
        ByteBuffer b = ByteBuffer.allocate(1);
        for (int i = 0; i < 128; i++) {
            b.rewind();
            b.put((byte) i);
            b.rewind();

            assertEquals(i, UnsignedLexVarint.readLexVarUInt32(b));
            b.rewind();
            assertEquals(i, UnsignedLexVarint.readLexVarUInt64(b));

            assertEquals(1, UnsignedLexVarint.encodedLength(i));
        }

        for (int i = 128; i < 256; i++) {
            b.rewind();
            b.put((byte) i);
            b.rewind();
            try {
                UnsignedLexVarint.readLexVarUInt32(b);
                fail("Should not be able to read lexvarint " + TypeUtils.toBinaryString((byte) i) +
                     " due to insufficient bytes");
            } catch (BufferUnderflowException e) {
                // success
            }
        }
    }

    @Test
    public void testLexVarInt32_2() throws AtomLayerException {
        ByteBuffer b = ByteBuffer.allocate(2);

        int expect = 0;
        for (int b0 = 128; b0 < 192; b0++) {
            for (int b1 = 0; b1 < 256; b1++) {
                b.rewind();
                b.put((byte) b0);
                b.put((byte) b1);
                b.rewind();

                if (b0 > 128) {
                    assertEquals(2, UnsignedLexVarint.encodedLength(expect));
                }
                assertEquals(expect, UnsignedLexVarint.readLexVarUInt32(b));
                b.rewind();
                assertEquals(expect++, UnsignedLexVarint.readLexVarUInt64(b));
            }
        }
    }

    @Test
    public void testLexVarInt32_3() throws AtomLayerException {
        ByteBuffer b = ByteBuffer.allocate(3);

        int expect = 0;
        for (int b0 = 192; b0 < 224; b0++) {
            for (int b1 = 0; b1 < 256; b1++) {
                for (int b2 = 0; b2 < 256; b2++) {
                    b.rewind();
                    b.put((byte) b0);
                    b.put((byte) b1);
                    b.put((byte) b2);
                    b.rewind();

                    if (b0 > 192) {
                        assertEquals(3, UnsignedLexVarint.encodedLength(expect));
                    }
                    assertEquals(expect, UnsignedLexVarint.readLexVarUInt32(b));
                    b.rewind();
                    assertEquals(expect++, UnsignedLexVarint.readLexVarUInt64(b));
                }
            }
        }
    }

    @Test
    public void testLexVarInt32_4() throws AtomLayerException {
        ByteBuffer b = ByteBuffer.allocate(4);

        int expect = 0;
        for (int b0 = 224; b0 < 240; b0++) {
            for (int b1 = 0; b1 < 256; b1++) {
                for (int b2 = 0; b2 < 256; b2++) {
                    for (int b3 = 0; b3 < 256; b3++) {
                        b.rewind();
                        b.put((byte) b0);
                        b.put((byte) b1);
                        b.put((byte) b2);
                        b.put((byte) b3);
                        b.rewind();

                        if (b0 > 224) {
                            assertEquals(4, UnsignedLexVarint.encodedLength(expect));
                        }
                        assertEquals(expect, UnsignedLexVarint.readLexVarUInt32(b));
                        b.rewind();
                        assertEquals(expect++, UnsignedLexVarint.readLexVarUInt64(b));
                    }
                }
            }
        }
    }

    @Test
    public void testBigLexVarInt32() throws AtomLayerException {
        ByteBuffer b = ByteBuffer.allocate(5);
        b.put((byte) -16);
        b.putInt(Integer.MAX_VALUE);
        b.rewind();
        assertEquals(Integer.MAX_VALUE, UnsignedLexVarint.readLexVarUInt32(b));

        b.rewind();
        b.put((byte) -16);
        b.putInt(Integer.MIN_VALUE);
        b.rewind();
        assertEquals(Integer.MIN_VALUE, UnsignedLexVarint.readLexVarUInt32(b));

        int[] bad_prefixes = { -8, -4, -2, -1 };
        for (int bad : bad_prefixes) {
            b.rewind();
            b.put((byte) bad);
            b.putInt(Integer.MAX_VALUE);
            b.rewind();
            try {
                UnsignedLexVarint.readLexVarUInt32(b);
                fail("Expected buffer underflow due to insufficient bits");
            } catch (BufferUnderflowException e) {}
        }

    }

    @Test
    public void testEstimatedSize() {
        assertEquals(1, UnsignedLexVarint.encodedLength(0));
        assertEquals(2, UnsignedLexVarint.encodedLength(128));
        assertEquals(3, UnsignedLexVarint.encodedLength(128 * 256));
        assertEquals(4, UnsignedLexVarint.encodedLength(128 * 256 * 256));
        assertEquals(5, UnsignedLexVarint.encodedLength(128 * 256 * 256 * 256));
        assertEquals(5, UnsignedLexVarint.encodedLength(Integer.MAX_VALUE));
        assertEquals(5, UnsignedLexVarint.encodedLength(Integer.MIN_VALUE));
        assertEquals(5, UnsignedLexVarint.encodedLength(-1));
    }

    @Test
    public void testLexVarPrefixSize() throws AtomLayerException {
        for (int i = 0; i < 128; i++) {
            assertEquals(1, UnsignedLexVarint.interpretSize((byte) i));
        }
        for (int i = 128; i < 192; i++) {
            assertEquals(2, UnsignedLexVarint.interpretSize((byte) i));
        }
        for (int i = 192; i < 224; i++) {
            assertEquals(3, UnsignedLexVarint.interpretSize((byte) i));
        }
        for (int i = 224; i < 240; i++) {
            assertEquals(4, UnsignedLexVarint.interpretSize((byte) i));
        }
        for (int i = 240; i < 248; i++) {
            assertEquals(5, UnsignedLexVarint.interpretSize((byte) i));
        }
        for (int i = 248; i < 252; i++) {
            assertEquals(6, UnsignedLexVarint.interpretSize((byte) i));
        }
        assertEquals(7, UnsignedLexVarint.interpretSize((byte) 252));
        assertEquals(7, UnsignedLexVarint.interpretSize((byte) 253));
        assertEquals(8, UnsignedLexVarint.interpretSize((byte) 254));
        assertEquals(9, UnsignedLexVarint.interpretSize((byte) 255));
    }

    @Test
    public void testWriteReadLexVarUInt32() throws AtomLayerException {
        Random r = new Random(0);
        int numtests = 1000;

        int min = 0;
        int max = 128;
        for (int size = 1; size < 5; size++) {
            ByteBuffer b = ByteBuffer.allocate(size);
            for (int i = 0; i < numtests; i++) {
                int value = r.nextInt(max - min) + min;
                assertTrue(value >= min);
                assertTrue(value < max);

                b.rewind();
                int sizeWritten = UnsignedLexVarint.writeLexVarUInt32(b, value);
                assertEquals(size, sizeWritten);

                b.rewind();
                int valueRead = UnsignedLexVarint.readLexVarUInt32(b);
                assertEquals(value, valueRead);
            }

            min = max;
            max = min * 128;
        }

        ByteBuffer b = ByteBuffer.allocate(5);
        for (int i = 0; i < numtests; i++) {
            int value = -r.nextInt(Integer.MAX_VALUE) - 1;
            assertTrue(value < 0);

            b.rewind();
            int sizeWritten = UnsignedLexVarint.writeLexVarUInt32(b, value);
            assertEquals(5, sizeWritten);

            b.rewind();
            long valueRead = UnsignedLexVarint.readLexVarUInt32(b);
            assertEquals(value, valueRead);

        }
    }

    private static Random r = new Random(10);

    private static int generate(int size) {
        long min = 0;
        int max = 128;
        for (int i = 1; i < size; i++) {
            min = max;
            max *= 128;
        }

        long value;
        do {
            value = r.nextLong() % (max - min);
        } while (value < 0);
        value += min;
        return (int) value;
    }

    @Test
    public void testUnsignedVarint32Comparison() {
        byte[] imax = UnsignedLexVarint.writeVarUInt32(Integer.MAX_VALUE);
        byte[] imax2 = UnsignedLexVarint.writeVarUInt32(-1);

        assertTrue(Lexicographic.compare(imax, imax2) < 0);

        int numtests = 100;
        for (int sizea = 1; sizea <= 5; sizea++) {
            ByteBuffer bufa = ByteBuffer.allocate(sizea);
            for (int sizeb = sizea; sizeb <= 5; sizeb++) {
                ByteBuffer bufb = ByteBuffer.allocate(sizeb);
                for (int i = 0; i < numtests; i++) {
                    int a = generate(sizea);
                    int b = generate(sizeb);

                    bufa.rewind();
                    assertEquals(sizea, UnsignedLexVarint.writeLexVarUInt32(bufa, a));

                    bufb.rewind();
                    assertEquals(sizeb, UnsignedLexVarint.writeLexVarUInt32(bufb, b));

                    boolean a_smaller = a >= 0 ? (b < 0 || a < b) : (b < 0 && a < b);

                    assertEquals(a == b, Lexicographic.compare(bufa.array(), bufb.array()) == 0);
                    assertEquals(a_smaller, Lexicographic.compare(bufa.array(), bufb.array()) < 0);
                    assertEquals(a_smaller, Lexicographic.compare(bufb.array(), bufa.array()) > 0);
                }
            }
        }
    }

    @Test
    public void testReverseUnsignedVarint32Comparison() {
        int numtests = 100;
        for (int sizea = 1; sizea <= 5; sizea++) {
            ByteBuffer bufa = ByteBuffer.allocate(sizea);
            for (int sizeb = sizea; sizeb <= 5; sizeb++) {
                ByteBuffer bufb = ByteBuffer.allocate(sizeb);
                for (int i = 0; i < numtests; i++) {
                    int a = generate(sizea);
                    int b = generate(sizeb);

                    bufa.rewind();
                    assertEquals(sizea, UnsignedLexVarint.writeReverseLexVarUInt32(bufa, a));

                    bufb.rewind();
                    assertEquals(sizeb, UnsignedLexVarint.writeReverseLexVarUInt32(bufb, b));

                    boolean a_smaller = a >= 0 ? (b < 0 || a < b) : (b < 0 && a < b);

                    assertEquals(a == b, Lexicographic.compare(bufa.array(), bufb.array()) == 0);
                    assertEquals(a_smaller, Lexicographic.compare(bufa.array(), bufb.array()) > 0);
                    assertEquals(a_smaller, Lexicographic.compare(bufb.array(), bufa.array()) < 0);
                }
            }
        }
    }
}
