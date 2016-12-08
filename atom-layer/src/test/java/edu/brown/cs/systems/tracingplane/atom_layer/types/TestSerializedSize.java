package edu.brown.cs.systems.tracingplane.atom_layer.types;

import java.nio.ByteBuffer;
import org.junit.Test;
import junit.framework.TestCase;

public class TestSerializedSize extends TestCase {

    @Test
    public void testSerializedSizeUnsigned32() {
        int[] values = { 0, 1, 127, 128, 16383, 16384, 2097151, 2097152, 268435455, 268435456, Integer.MAX_VALUE,
                         Integer.MIN_VALUE, -1 };
        int[] sizes = { 1, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 5, 5 };

        ByteBuffer buf = ByteBuffer.allocate(10);
        for (int i = 0; i < values.length; i++) {
            assertEquals(sizes[i], UnsignedLexVarint.encodedLength(values[i]));

            buf.position(0);
            UnsignedLexVarint.writeLexVarUInt32(buf, values[i]);
            assertEquals(sizes[i], buf.position());
        }
    }

    @Test
    public void testSerializedSizeUnsigned64() {
        long[] values = { 0, 1, 127, 128, 16383, 16384, 2097151, 2097152, 268435455, 268435456, Integer.MAX_VALUE,
                          Integer.MIN_VALUE, Long.MAX_VALUE, Long.MIN_VALUE, -1 };
        int[] sizes = { 1, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 9, 9, 9, 9 };

        ByteBuffer buf = ByteBuffer.allocate(10);
        for (int i = 0; i < values.length; i++) {
            assertEquals(sizes[i], UnsignedLexVarint.encodedLength(values[i]));

            buf.position(0);
            UnsignedLexVarint.writeLexVarUInt64(buf, values[i]);
            assertEquals(sizes[i], buf.position());
        }
    }

    @Test
    public void testSerializedSizeSigned32() {
        int[] values = { 0, 1, -1, 63, -64, 64, -65, 8191, -8192, 8192, -8193, 1048575, -1048576, 1048576, -1048577,
                         134217727, -134217728, 134217728, -134217729, Integer.MAX_VALUE, Integer.MIN_VALUE };
        int[] sizes = { 1, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5 };

        ByteBuffer buf = ByteBuffer.allocate(10);
        for (int i = 0; i < values.length; i++) {
            assertEquals(sizes[i], SignedLexVarint.encodedLength(values[i]));

            buf.position(0);
            SignedLexVarint.writeLexVarInt32(buf, values[i]);
            assertEquals(sizes[i], buf.position());
        }
    }

    @Test
    public void testSerializedSizeUnsigned32_full() {
        int length = 1;
        int v = 128;
        ByteBuffer buf = ByteBuffer.allocate(10);
        while (length < 5) {
            assertEquals(length, UnsignedLexVarint.encodedLength(v - 1));
            assertEquals(length + 1, UnsignedLexVarint.encodedLength(v));

            buf.position(0);
            UnsignedLexVarint.writeLexVarUInt32(buf, v - 1);
            assertEquals(length, buf.position());
            buf.position(0);
            UnsignedLexVarint.writeLexVarUInt32(buf, v);
            assertEquals(length + 1, buf.position());

            v *= 128;
            length++;
        }
    }

    @Test
    public void testSerializedSizeSigned32_full() {
        int length = 1;
        int v = 64;
        ByteBuffer buf = ByteBuffer.allocate(10);
        while (length < 5) {
            assertEquals(length, SignedLexVarint.encodedLength(v - 1));
            assertEquals(length + 1, SignedLexVarint.encodedLength(v));

            buf.position(0);
            SignedLexVarint.writeLexVarInt32(buf, v - 1);
            assertEquals(length, buf.position());
            buf.position(0);
            SignedLexVarint.writeLexVarInt32(buf, v);
            assertEquals(length + 1, buf.position());

            v *= 128;
            length++;
        }
    }

    @Test
    public void testSerializedSizeUnsigned64_full() {
        int length = 1;
        long v = 128;
        ByteBuffer buf = ByteBuffer.allocate(10);
        while (length < 9) {
            assertEquals(length, UnsignedLexVarint.encodedLength(v - 1));
            assertEquals(length + 1, UnsignedLexVarint.encodedLength(v));

            buf.position(0);
            UnsignedLexVarint.writeLexVarUInt64(buf, v - 1);
            assertEquals(length, buf.position());
            buf.position(0);
            UnsignedLexVarint.writeLexVarUInt64(buf, v);
            assertEquals(length + 1, buf.position());

            v *= 128;
            length++;
        }
    }

    @Test
    public void testSerializedSizeSigned64_full() {
        int length = 1;
        long v = 64;
        ByteBuffer buf = ByteBuffer.allocate(10);
        while (length < 9) {
            assertEquals(length, SignedLexVarint.encodedLength(v - 1));
            assertEquals(length + 1, SignedLexVarint.encodedLength(v));

            buf.position(0);
            SignedLexVarint.writeLexVarInt64(buf, v - 1);
            assertEquals(length, buf.position());
            buf.position(0);
            SignedLexVarint.writeLexVarInt64(buf, v);
            assertEquals(length + 1, buf.position());

            v *= 128;
            length++;
        }
    }

}
