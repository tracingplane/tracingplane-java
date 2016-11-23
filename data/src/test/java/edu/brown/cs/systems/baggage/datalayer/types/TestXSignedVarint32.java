package edu.brown.cs.systems.baggage.datalayer.types;

import java.nio.ByteBuffer;
import java.util.Random;

import org.junit.Test;

import edu.brown.cs.systems.baggage.datalayer.DataLayerException;
import edu.brown.cs.systems.baggage.datalayer.DataUtils;
import edu.brown.cs.systems.baggage.datalayer.types.SignedLexVarInt;
import junit.framework.TestCase;

public class TestXSignedVarint32 extends TestCase {
	
	private static ByteBuffer make(String... ss) {
		ByteBuffer buf = ByteBuffer.allocate(ss.length);
		for (String s : ss) {
			buf.put(DataUtils.makeByte(s));
		}
		buf.rewind();
		return buf;
	}
	
	private static byte[] write(int value) {
		ByteBuffer buf = ByteBuffer.allocate(5);
		SignedLexVarInt.writeLexVarInt32(buf, value);
		buf.flip();
		byte[] bytes = new byte[buf.remaining()];
		buf.get(bytes);
		return bytes;
	}
	
	private static String writeString(int value) {
		byte[] bytes = write(value);
		String s = "";
		for (byte b : bytes) {
			s = s + DataUtils.toBinaryString(b);
		}
		return s;
	}
	
	@Test
	public void testXSignedVarint32Simple() throws DataLayerException {
		assertEquals(Integer.MIN_VALUE, SignedLexVarInt.readLexVarInt32(make("00000111", "10000000", "00000000", "00000000", "00000000")));
		assertEquals(-65, SignedLexVarInt.readLexVarInt32(make("0011 1111", "1011 1111")));
		assertEquals(-64, SignedLexVarInt.readLexVarInt32(make("0100 0000")));
		assertEquals(-19, SignedLexVarInt.readLexVarInt32(make("0110 1101")));
		assertEquals(-4, SignedLexVarInt.readLexVarInt32(make("0111 1100")));
		assertEquals(-1, SignedLexVarInt.readLexVarInt32(make("0111 1111")));
		assertEquals(0, SignedLexVarInt.readLexVarInt32(make("1000 0000")));
		assertEquals(1, SignedLexVarInt.readLexVarInt32(make("1000 0001")));
		assertEquals(19, SignedLexVarInt.readLexVarInt32(make("1001 0011")));
		assertEquals(63, SignedLexVarInt.readLexVarInt32(make("1011 1111")));
		assertEquals(64, SignedLexVarInt.readLexVarInt32(make("1100 0000", "0100 0000")));
		assertEquals(Integer.MAX_VALUE, SignedLexVarInt.readLexVarInt32(make("11111000", "01111111", "11111111", "11111111", "11111111")));
	}
	
	@Test
	public void testXSignedVarint32Simple2() throws DataLayerException {
		assertEquals("0000011110000000000000000000000000000000", writeString(Integer.MIN_VALUE));
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
		assertEquals("1111100001111111111111111111111111111111", writeString(Integer.MAX_VALUE));
	}
	
	@Test
	public void testEstimatedSize() {
		assertEquals(1, SignedLexVarInt.encodedLength(-64));
		assertEquals(1, SignedLexVarInt.encodedLength(-1));
		assertEquals(1, SignedLexVarInt.encodedLength(0));
		assertEquals(1, SignedLexVarInt.encodedLength(63));

		assertEquals(2, SignedLexVarInt.encodedLength(-64*128));
		assertEquals(2, SignedLexVarInt.encodedLength(-64-1));
		assertEquals(2, SignedLexVarInt.encodedLength(64));
		assertEquals(2, SignedLexVarInt.encodedLength(64*128-1));

		assertEquals(3, SignedLexVarInt.encodedLength(-64*128*128));
		assertEquals(3, SignedLexVarInt.encodedLength(-64*128-1));
		assertEquals(3, SignedLexVarInt.encodedLength(64*128));
		assertEquals(3, SignedLexVarInt.encodedLength(64*128*128-1));

		assertEquals(4, SignedLexVarInt.encodedLength(-64*128*128*128));
		assertEquals(4, SignedLexVarInt.encodedLength(-64*128*128-1));
		assertEquals(4, SignedLexVarInt.encodedLength(64*128*128));
		assertEquals(4, SignedLexVarInt.encodedLength(64*128*128*128-1));

		assertEquals(5, SignedLexVarInt.encodedLength(-64*128*128*128-1));
		assertEquals(5, SignedLexVarInt.encodedLength(64*128*128*128));
		assertEquals(5, SignedLexVarInt.encodedLength(Integer.MAX_VALUE));
		assertEquals(5, SignedLexVarInt.encodedLength(Integer.MIN_VALUE));
	}
	
	@Test
	public void testXSignedVarInt32EncodeDecode() throws DataLayerException {
		int numtests = 1000;
		Random r = new Random(0);
		
		int min = 0;
		int max = 64;
		
		for (int size = 1; size < 5; size++) {
			ByteBuffer b = ByteBuffer.allocate(size);
			for (int i = 0; i < numtests; i++) {
				int value = r.nextInt(max-min) + min;
				assertTrue(value >= min);
				assertTrue(value < max);
				
				b.rewind();
				int sizeWritten = SignedLexVarInt.writeLexVarInt32(b, value);
				assertEquals(size, sizeWritten);
	
				b.rewind();
				int valueRead = SignedLexVarInt.readLexVarInt32(b);
				assertEquals(value, valueRead);
				
				b.rewind();
				sizeWritten = SignedLexVarInt.writeLexVarInt32(b, -value-1);
				assertEquals(size, sizeWritten);
	
				b.rewind();
				valueRead = SignedLexVarInt.readLexVarInt32(b);
				assertEquals(-value-1, valueRead);
			}
			
			min = max;
			max *= 128;
		}

		ByteBuffer b = ByteBuffer.allocate(5);
		for (int i = 0; i < numtests; i++) {
			int value = r.nextInt(1+Integer.MAX_VALUE/2) + Integer.MAX_VALUE/2;
			assertTrue(value > Integer.MAX_VALUE/2);
			assertTrue(value <= Integer.MAX_VALUE);
			
			b.rewind();
			int sizeWritten = SignedLexVarInt.writeLexVarInt32(b, value);
			assertEquals(5, sizeWritten);

			b.rewind();
			int valueRead = SignedLexVarInt.readLexVarInt32(b);
			assertEquals(value, valueRead);
			
			b.rewind();
			sizeWritten = SignedLexVarInt.writeLexVarInt32(b, -value-1);
			assertEquals(5, sizeWritten);

			b.rewind();
			valueRead = SignedLexVarInt.readLexVarInt32(b);
			assertEquals(-value-1, valueRead);
		}
	}
	
	private static final Random r = new Random(7);
	private static int generate(int size) {
		if (size == 5) {
			return r.nextInt(Integer.MAX_VALUE) + Integer.MAX_VALUE/2 + 1;
		}
		int min = 0;
		int max = 64;
		for (; size > 1; size--) {
			min = max;
			max *= 128;
		}
		return r.nextInt(max-min) + min;
	}
	
	@Test
	public void testSignedVarint32Comparison() {
		int numtests = 100;
		for (int sizea = 1; sizea <= 5; sizea++) {
			ByteBuffer bufa = ByteBuffer.allocate(sizea);
			for (int sizeb = sizea; sizeb <= 5; sizeb++) {
				ByteBuffer bufb = ByteBuffer.allocate(sizeb);
				for (int i = 0; i < numtests; i++) {
					int valuea = generate(sizea);
					int valueb = generate(sizeb);
					
					for (int a : new int[] { valuea, -valuea-1 }) {
						for (int b : new int[] { valueb, -valueb-1 }) {
							
							bufa.rewind();
							assertEquals(sizea, SignedLexVarInt.writeLexVarInt32(bufa, a));
							
							bufb.rewind();
							assertEquals(sizeb, SignedLexVarInt.writeLexVarInt32(bufb, b));

							assertEquals(Integer.compare(a, b) == 0, DataUtils.compare(bufa.array(), bufb.array()) == 0);
							assertEquals(Integer.compare(a, b) < 0, DataUtils.compare(bufa.array(), bufb.array()) < 0);
							assertEquals(Integer.compare(b, a) < 0, DataUtils.compare(bufb.array(), bufa.array()) < 0);
						}
					}
				}
			}
		}
	}
}
