package edu.brown.cs.systems.baggage.data;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Random;

import org.junit.Test;

import junit.framework.TestCase;

public class TestLexVarInt32 extends TestCase {
	
	@Test
	public void testLexVarInt32() throws DataLayerException {
		ByteBuffer b = ByteBuffer.allocate(1);
		for (int i = 0; i < 128; i++) {
			b.rewind();
			b.put((byte) i);
			b.rewind();

			assertEquals(i, DataUtils.readLexVarUInt32(b));
			b.rewind();
			assertEquals(i, DataUtils.readLexVarUInt64(b));
		}
		
		for (int i = 128; i < 248; i++) {
			b.rewind();
			b.put((byte) i);
			b.rewind();
			try {
				DataUtils.readLexVarUInt32(b);
				fail("Should not be able to read lexvarint " + DataUtils.toBinaryString((byte) i) + " due to insufficient bytes");
			} catch (BufferUnderflowException e) {
				// success
			}
		}
		
		for (int i = 248; i < 256; i++) {
			b.rewind();
			b.put((byte) i);
			b.rewind();
			try {
				DataUtils.readLexVarUInt32(b);
				fail("Should not be able to read lexvarint " + DataUtils.toBinaryString((byte) i) + " due to invalid leadin byte");
			} catch (DataLayerException e) {
				// success
			}
		}
	}
	
	@Test
	public void testLexVarInt32_2() throws DataLayerException {
		ByteBuffer b = ByteBuffer.allocate(2);
		
		int expect = 0;
		for (int b0 = 128; b0 < 192; b0++) {
			for (int b1 = 0; b1 < 256; b1++) {
				b.rewind();
				b.put((byte) b0);
				b.put((byte) b1);
				b.rewind();

				assertEquals(expect, DataUtils.readLexVarUInt32(b));
				b.rewind();
				assertEquals(expect++, DataUtils.readLexVarUInt64(b));
			}
		}
	}
	
	@Test
	public void testLexVarInt32_3() throws DataLayerException {
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

					assertEquals(expect, DataUtils.readLexVarUInt32(b));
					b.rewind();
					assertEquals(expect++, DataUtils.readLexVarUInt64(b));
				}
			}
		}
	}
	
	@Test
	public void testLexVarInt32_4() throws DataLayerException {
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

						assertEquals(expect, DataUtils.readLexVarUInt32(b));
						b.rewind();
						assertEquals(expect++, DataUtils.readLexVarUInt64(b));
					}
				}
			}
		}
	}
	
	@Test
	public void testBigLexVarInt32() throws DataLayerException {
		ByteBuffer b = ByteBuffer.allocate(5);
		b.put((byte) -16);
		b.putInt(Integer.MAX_VALUE);
		b.rewind();
		assertEquals(Integer.MAX_VALUE, DataUtils.readLexVarUInt32(b));

		b.rewind();
		b.put((byte) -16);
		b.putInt(Integer.MIN_VALUE);
		b.rewind();
		assertEquals(Integer.MIN_VALUE, DataUtils.readLexVarUInt32(b));
		
		int[] bad_prefixes = { -15, -14, -12, -8 };
		for (int bad : bad_prefixes) {
			b.rewind();
			b.put((byte) bad);
			b.putInt(Integer.MAX_VALUE);
			b.rewind();
			try {
				DataUtils.readLexVarUInt32(b);
				fail("Expected 32-bit integer overflow");
			} catch (DataLayerException e) {
			}
		}
		
	}
	
	@Test
	public void testReadUint64() throws DataLayerException {
		ByteBuffer b = ByteBuffer.allocate(8);
		
		Random r = new Random(1);
		for (int test_number=0; test_number < 1000; test_number++) {
			long value = r.nextLong();
			
			b.rewind();
			b.putLong(value);
			
			for (int i = 1; i <= 8; i++) {
				b.rewind();
				assertEquals((value >>> (8*(8-i))), DataUtils.readUInt64(b, i));
			}
		}
	}
	
	@Test
	public void testLexVarPrefixSize() throws DataLayerException {
		for (int i = 0; i < 128; i++) {
			assertEquals(0, DataUtils.lexVarPrefixSize((byte) i));
		}
		for (int i = 128; i < 192; i++) {
			assertEquals(1, DataUtils.lexVarPrefixSize((byte) i));
		}
		for (int i = 192; i < 224; i++) {
			assertEquals(2, DataUtils.lexVarPrefixSize((byte) i));
		}
		for (int i = 224; i < 240; i++) {
			assertEquals(3, DataUtils.lexVarPrefixSize((byte) i));
		}
		for (int i = 240; i < 248; i++) {
			assertEquals(4, DataUtils.lexVarPrefixSize((byte) i));
		}
		for (int i = 248; i < 252; i++) {
			assertEquals(5, DataUtils.lexVarPrefixSize((byte) i));
		}
		assertEquals(6, DataUtils.lexVarPrefixSize((byte) 252));
		assertEquals(6, DataUtils.lexVarPrefixSize((byte) 253));
		assertEquals(7, DataUtils.lexVarPrefixSize((byte) 254));
		assertEquals(8, DataUtils.lexVarPrefixSize((byte) 255));
	}
	
	@Test
	public void testLsb() {
		byte b = -1;
		assertEquals(-1, b);
		assertEquals(255, DataUtils.lsb(b, 0));
		assertEquals(127, DataUtils.lsb(b, 1));
		assertEquals(63, DataUtils.lsb(b, 2));
		assertEquals(31, DataUtils.lsb(b, 3));
		assertEquals(15, DataUtils.lsb(b, 4));
		assertEquals(7, DataUtils.lsb(b, 5));
		assertEquals(3, DataUtils.lsb(b, 6));
		assertEquals(1, DataUtils.lsb(b, 7));
		assertEquals(0, DataUtils.lsb(b, 8));
	}
}
