package edu.brown.cs.systems.baggage.datatypes;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Random;

import org.junit.Test;

import edu.brown.cs.systems.baggage.data.DataLayerException;
import edu.brown.cs.systems.baggage.data.DataUtils;
import junit.framework.TestCase;

public class TestXUnsignedVarint extends TestCase {
	
	@Test
	public void testLexVarInt32() throws DataLayerException {
		ByteBuffer b = ByteBuffer.allocate(1);
		for (int i = 0; i < 128; i++) {
			b.rewind();
			b.put((byte) i);
			b.rewind();

			assertEquals(i, XUnsignedVarint.readLexVarUInt32(b));
			b.rewind();
			assertEquals(i, XUnsignedVarint.readLexVarUInt64(b));
			
			assertEquals(1, XUnsignedVarint.encodedLength(i));
		}
		
		for (int i = 128; i < 256; i++) {
			b.rewind();
			b.put((byte) i);
			b.rewind();
			try {
				XUnsignedVarint.readLexVarUInt32(b);
				fail("Should not be able to read lexvarint " + DataUtils.toBinaryString((byte) i) + " due to insufficient bytes");
			} catch (BufferUnderflowException e) {
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
				
				if (b0 > 128) {
					assertEquals(2, XUnsignedVarint.encodedLength(expect));
				}
				assertEquals(expect, XUnsignedVarint.readLexVarUInt32(b));
				b.rewind();
				assertEquals(expect++, XUnsignedVarint.readLexVarUInt64(b));
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

					if (b0 > 192) {
						assertEquals(3, XUnsignedVarint.encodedLength(expect));
					}
					assertEquals(expect, XUnsignedVarint.readLexVarUInt32(b));
					b.rewind();
					assertEquals(expect++, XUnsignedVarint.readLexVarUInt64(b));
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

						if (b0 > 224) {
							assertEquals(4, XUnsignedVarint.encodedLength(expect));
						}
						assertEquals(expect, XUnsignedVarint.readLexVarUInt32(b));
						b.rewind();
						assertEquals(expect++, XUnsignedVarint.readLexVarUInt64(b));
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
		assertEquals(Integer.MAX_VALUE, XUnsignedVarint.readLexVarUInt32(b));

		b.rewind();
		b.put((byte) -16);
		b.putInt(Integer.MIN_VALUE);
		b.rewind();
		assertEquals(Integer.MIN_VALUE, XUnsignedVarint.readLexVarUInt32(b));
		
		int[] bad_prefixes = { -8, -4, -2, -1 };
		for (int bad : bad_prefixes) {
			b.rewind();
			b.put((byte) bad);
			b.putInt(Integer.MAX_VALUE);
			b.rewind();
			try {
				XUnsignedVarint.readLexVarUInt32(b);
				fail("Expected buffer underflow due to insufficient bits");
			} catch (BufferUnderflowException e) {
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
				assertEquals((value >>> (8*(8-i))), XUnsignedVarint.readUInt64(b, i));
			}
		}
	}
	
	@Test
	public void testLexVarPrefixSize() throws DataLayerException {
		for (int i = 0; i < 128; i++) {
			assertEquals(1, XUnsignedVarint.interpretSize((byte) i));
		}
		for (int i = 128; i < 192; i++) {
			assertEquals(2, XUnsignedVarint.interpretSize((byte) i));
		}
		for (int i = 192; i < 224; i++) {
			assertEquals(3, XUnsignedVarint.interpretSize((byte) i));
		}
		for (int i = 224; i < 240; i++) {
			assertEquals(4, XUnsignedVarint.interpretSize((byte) i));
		}
		for (int i = 240; i < 248; i++) {
			assertEquals(5, XUnsignedVarint.interpretSize((byte) i));
		}
		for (int i = 248; i < 252; i++) {
			assertEquals(6, XUnsignedVarint.interpretSize((byte) i));
		}
		assertEquals(7, XUnsignedVarint.interpretSize((byte) 252));
		assertEquals(7, XUnsignedVarint.interpretSize((byte) 253));
		assertEquals(8, XUnsignedVarint.interpretSize((byte) 254));
		assertEquals(9, XUnsignedVarint.interpretSize((byte) 255));
	}
	
	@Test
	public void testWriteLexVarUInt64() {
		ByteBuffer b = ByteBuffer.allocate(1);
		
		for (int i = 0; i < 128; i++) {
			b.rewind();
			int size = XUnsignedVarint.writeLexVarUInt64(b, i);
			
			assertEquals(1, size);
			b.rewind();
			assertEquals((byte) i, (byte) b.get());
		}
	}
	
	@Test
	public void testWriteLexVarUInt64_2() {
		ByteBuffer b = ByteBuffer.allocate(2);
		
		for (int i = 128; i < 16384; i++) {
			b.rewind();
			int size = XUnsignedVarint.writeLexVarUInt64(b, i);
			
			assertEquals(2, size);
			b.rewind();
			byte b0 = b.get();
			byte b1 = b.get();

			assertEquals((byte) (i >>> 8) + Byte.MIN_VALUE, b0);
			assertEquals((byte) i, b1);
			
			b.rewind();
			short s = b.getShort();
			assertEquals(i + Short.MIN_VALUE, s);
		}
	}
	
	@Test
	public void testWriteReadLexVarUInt64() throws DataLayerException {
		Random r = new Random(0);
		int numtests = 1000;

		long min = 0;
		long max = 128;
		for (int size = 1; size < 9; size++) {
			ByteBuffer b = ByteBuffer.allocate(size);
			for (int i = 0; i < numtests; i++) {
				long value;
				do {
					value = r.nextLong() % (max-min);
				} while (value < 0);
				value += min;
				assertTrue(value >= min);
				assertTrue(value < max);
				
				b.rewind();
				int sizeWritten = XUnsignedVarint.writeLexVarUInt64(b, value);
				assertEquals(size, sizeWritten);

				b.rewind();
				long valueRead = XUnsignedVarint.readLexVarUInt64(b);
				assertEquals(value, valueRead);
			}
			
			min = max;
			max = min * 128;
		}
		
		ByteBuffer b = ByteBuffer.allocate(9);
		for (int i = 0; i < numtests; i++) {
			long value = r.nextLong();
			if (value >= 0) {
				value = -value;
			}
			assertTrue(value < 0);
			
			b.rewind();
			int sizeWritten = XUnsignedVarint.writeLexVarUInt64(b, value);
			assertEquals(9, sizeWritten);

			b.rewind();
			long valueRead = XUnsignedVarint.readLexVarUInt64(b);
			assertEquals(value, valueRead);
			
		}
	}
}
