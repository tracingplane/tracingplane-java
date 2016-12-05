package edu.brown.cs.systems.tracingplane.atom_layer.types;

import java.nio.ByteBuffer;
import java.util.Random;

import org.junit.Test;

import edu.brown.cs.systems.tracingplane.atom_layer.types.AtomLayerException;
import edu.brown.cs.systems.tracingplane.atom_layer.types.Lexicographic;
import edu.brown.cs.systems.tracingplane.atom_layer.types.UnsignedLexVarint;
import junit.framework.TestCase;

public class TestXUnsignedVarint64 extends TestCase {
	
	@Test
	public void testReadUint64() throws AtomLayerException {
		ByteBuffer b = ByteBuffer.allocate(8);
		
		Random r = new Random(1);
		for (int test_number=0; test_number < 1000; test_number++) {
			long value = r.nextLong();
			
			b.rewind();
			b.putLong(value);
			
			for (int i = 1; i <= 8; i++) {
				b.rewind();
				assertEquals((value >>> (8*(8-i))), UnsignedLexVarint.readUInt64(b, i));
			}
		}
	}
	
	@Test
	public void testEstimatedSize() {
		assertEquals(1, UnsignedLexVarint.encodedLength(0L));
		assertEquals(2, UnsignedLexVarint.encodedLength(128L));
		assertEquals(3, UnsignedLexVarint.encodedLength(128L*256));
		assertEquals(4, UnsignedLexVarint.encodedLength(128L*256*256));
		assertEquals(5, UnsignedLexVarint.encodedLength(128L*256*256*256));
		assertEquals(5, UnsignedLexVarint.encodedLength((long) Integer.MAX_VALUE));
		assertEquals(6, UnsignedLexVarint.encodedLength(128L*256*256*256*256));
		assertEquals(7, UnsignedLexVarint.encodedLength(128L*256*256*256*256*256));
		assertEquals(8, UnsignedLexVarint.encodedLength(128L*256*256*256*256*256*256));
		assertEquals(9, UnsignedLexVarint.encodedLength((long) Integer.MIN_VALUE));
		assertEquals(9, UnsignedLexVarint.encodedLength(Long.MAX_VALUE));
		assertEquals(9, UnsignedLexVarint.encodedLength(Long.MIN_VALUE));
		assertEquals(9, UnsignedLexVarint.encodedLength((long) -1));
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
	public void testWriteLexVarUInt64() {
		ByteBuffer b = ByteBuffer.allocate(1);
		
		for (int i = 0; i < 128; i++) {
			b.rewind();
			int size = UnsignedLexVarint.writeLexVarUInt64(b, i);
			
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
			int size = UnsignedLexVarint.writeLexVarUInt64(b, i);
			
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
	public void testWriteReadLexVarUInt64() throws AtomLayerException {
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
				int sizeWritten = UnsignedLexVarint.writeLexVarUInt64(b, value);
				assertEquals(size, sizeWritten);

				b.rewind();
				long valueRead = UnsignedLexVarint.readLexVarUInt64(b);
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
			int sizeWritten = UnsignedLexVarint.writeLexVarUInt64(b, value);
			assertEquals(9, sizeWritten);

			b.rewind();
			long valueRead = UnsignedLexVarint.readLexVarUInt64(b);
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
		
		assertTrue(Lexicographic.compare(imax, imax2) < 0);
		

		int numtests = 100;
		for (int sizea = 1; sizea <= 9; sizea++) {
			ByteBuffer bufa = ByteBuffer.allocate(sizea);
			for (int sizeb = sizea; sizeb <= 9; sizeb++) {
				ByteBuffer bufb = ByteBuffer.allocate(sizeb);
				for (int i = 0; i < numtests; i++) {
					long a = generate(sizea);
					long b = generate(sizeb);
					
					bufa.rewind();
					assertEquals(sizea, UnsignedLexVarint.writeLexVarUInt64(bufa, a));
					
					bufb.rewind();
					assertEquals(sizeb, UnsignedLexVarint.writeLexVarUInt64(bufb, b));

					boolean a_smaller = a >= 0 ? (b < 0 || a < b) : (b < 0 && a < b);
					
					assertEquals(a==b, Lexicographic.compare(bufa.array(), bufb.array()) == 0);
					assertEquals(a_smaller, Lexicographic.compare(bufa.array(), bufb.array()) < 0);
					assertEquals(!a_smaller, Lexicographic.compare(bufb.array(), bufa.array()) < 0);
				}
			}
		}
	}
}
