package edu.brown.cs.systems.baggage.datatypes;

import java.nio.ByteBuffer;
import java.util.Random;

import org.junit.Test;

import edu.brown.cs.systems.baggage.data.DataLayerException;
import junit.framework.TestCase;

public class TestXUnsignedVarint64 extends TestCase {
	
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
