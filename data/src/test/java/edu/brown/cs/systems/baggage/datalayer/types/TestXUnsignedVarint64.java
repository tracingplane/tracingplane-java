package edu.brown.cs.systems.baggage.datalayer.types;

import java.nio.ByteBuffer;
import java.util.Random;

import org.junit.Test;

import edu.brown.cs.systems.baggage.datalayer.DataLayerException;
import edu.brown.cs.systems.baggage.datalayer.DataUtils;
import edu.brown.cs.systems.baggage.datalayer.types.UnsignedLexVarInt;
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
				assertEquals((value >>> (8*(8-i))), UnsignedLexVarInt.readUInt64(b, i));
			}
		}
	}
	
	@Test
	public void testEstimatedSize() {
		assertEquals(1, UnsignedLexVarInt.encodedLength(0L));
		assertEquals(2, UnsignedLexVarInt.encodedLength(128L));
		assertEquals(3, UnsignedLexVarInt.encodedLength(128L*256));
		assertEquals(4, UnsignedLexVarInt.encodedLength(128L*256*256));
		assertEquals(5, UnsignedLexVarInt.encodedLength(128L*256*256*256));
		assertEquals(5, UnsignedLexVarInt.encodedLength((long) Integer.MAX_VALUE));
		assertEquals(6, UnsignedLexVarInt.encodedLength(128L*256*256*256*256));
		assertEquals(7, UnsignedLexVarInt.encodedLength(128L*256*256*256*256*256));
		assertEquals(8, UnsignedLexVarInt.encodedLength(128L*256*256*256*256*256*256));
		assertEquals(9, UnsignedLexVarInt.encodedLength((long) Integer.MIN_VALUE));
		assertEquals(9, UnsignedLexVarInt.encodedLength(Long.MAX_VALUE));
		assertEquals(9, UnsignedLexVarInt.encodedLength(Long.MIN_VALUE));
		assertEquals(9, UnsignedLexVarInt.encodedLength((long) -1));
	}
	
	@Test
	public void testLexVarPrefixSize() throws DataLayerException {
		for (int i = 0; i < 128; i++) {
			assertEquals(1, UnsignedLexVarInt.interpretSize((byte) i));
		}
		for (int i = 128; i < 192; i++) {
			assertEquals(2, UnsignedLexVarInt.interpretSize((byte) i));
		}
		for (int i = 192; i < 224; i++) {
			assertEquals(3, UnsignedLexVarInt.interpretSize((byte) i));
		}
		for (int i = 224; i < 240; i++) {
			assertEquals(4, UnsignedLexVarInt.interpretSize((byte) i));
		}
		for (int i = 240; i < 248; i++) {
			assertEquals(5, UnsignedLexVarInt.interpretSize((byte) i));
		}
		for (int i = 248; i < 252; i++) {
			assertEquals(6, UnsignedLexVarInt.interpretSize((byte) i));
		}
		assertEquals(7, UnsignedLexVarInt.interpretSize((byte) 252));
		assertEquals(7, UnsignedLexVarInt.interpretSize((byte) 253));
		assertEquals(8, UnsignedLexVarInt.interpretSize((byte) 254));
		assertEquals(9, UnsignedLexVarInt.interpretSize((byte) 255));
	}
	
	@Test
	public void testWriteLexVarUInt64() {
		ByteBuffer b = ByteBuffer.allocate(1);
		
		for (int i = 0; i < 128; i++) {
			b.rewind();
			int size = UnsignedLexVarInt.writeLexVarUInt64(b, i);
			
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
			int size = UnsignedLexVarInt.writeLexVarUInt64(b, i);
			
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
				int sizeWritten = UnsignedLexVarInt.writeLexVarUInt64(b, value);
				assertEquals(size, sizeWritten);

				b.rewind();
				long valueRead = UnsignedLexVarInt.readLexVarUInt64(b);
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
			int sizeWritten = UnsignedLexVarInt.writeLexVarUInt64(b, value);
			assertEquals(9, sizeWritten);

			b.rewind();
			long valueRead = UnsignedLexVarInt.readLexVarUInt64(b);
			assertEquals(value, valueRead);
			
		}
	}
	
	private static Random r = new Random(10);
	private static long generate(int size) {
		if (size == 9) {
			long value;
			do {
				value = r.nextLong();
			} while (value >= 0);
			return value;
		}
		long min = 0;
		long max = 128;
		for (int i = 1; i < size; i++) {
			min = max;
			max *= 128;
		}
		
		long value;
		do {
			value = r.nextLong() % (max-min);
		} while (value < 0);
		value += min;
		return value;
	}
	
	@Test
	public void testUnsignedVarint64Comparison() {
		byte[] imax = Lexicographic.writeVarUInt64(Long.MAX_VALUE);
		byte[] imax2 = Lexicographic.writeVarUInt64(-1);
		
		assertTrue(DataUtils.compare(imax, imax2) < 0);
		

		int numtests = 100;
		for (int sizea = 1; sizea <= 9; sizea++) {
			ByteBuffer bufa = ByteBuffer.allocate(sizea);
			for (int sizeb = sizea; sizeb <= 9; sizeb++) {
				ByteBuffer bufb = ByteBuffer.allocate(sizeb);
				for (int i = 0; i < numtests; i++) {
					long a = generate(sizea);
					long b = generate(sizeb);
					
					bufa.rewind();
					assertEquals(sizea, UnsignedLexVarInt.writeLexVarUInt64(bufa, a));
					
					bufb.rewind();
					assertEquals(sizeb, UnsignedLexVarInt.writeLexVarUInt64(bufb, b));

					boolean a_smaller = a >= 0 ? (b < 0 || a < b) : (b < 0 && a < b);
					
					assertEquals(a==b, DataUtils.compare(bufa.array(), bufb.array()) == 0);
					assertEquals(a_smaller, DataUtils.compare(bufa.array(), bufb.array()) < 0);
					assertEquals(!a_smaller, DataUtils.compare(bufb.array(), bufa.array()) < 0);
				}
			}
		}
	}
}
