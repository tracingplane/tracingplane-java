package edu.brown.cs.systems.baggage.datatypes;

import java.nio.ByteBuffer;
import java.util.Random;

import org.junit.Test;

import edu.brown.cs.systems.baggage.data.DataLayerException;
import edu.brown.cs.systems.baggage.data.DataUtils;
import junit.framework.TestCase;

public class TestXSignedVarint extends TestCase {
	
	private static ByteBuffer make(String... ss) {
		ByteBuffer buf = ByteBuffer.allocate(ss.length);
		for (String s : ss) {
			buf.put(DataUtils.makeByte(s));
		}
		buf.rewind();
		return buf;
	}
	
	private static byte[] write(long value) {
		ByteBuffer buf = ByteBuffer.allocate(9);
		XSignedVarint.writeLexVarInt64(buf, value);
		buf.flip();
		byte[] bytes = new byte[buf.remaining()];
		buf.get(bytes);
		return bytes;
	}
	
	private static String writeString(long value) {
		byte[] bytes = write(value);
		String s = "";
		for (byte b : bytes) {
			s = s + DataUtils.toBinaryString(b);
		}
		return s;
	}
	
	@Test
	public void testXSignedVarintSimple() throws DataLayerException {
		assertEquals(Long.MIN_VALUE, XSignedVarint.readLexVarInt64(make("00000000", "00000000", "00000000", "00000000", "00000000", "00000000", "00000000", "00000000", "00000000")));
		assertEquals(-65, XSignedVarint.readLexVarInt64(make("0011 1111", "1011 1111")));
		assertEquals(-64, XSignedVarint.readLexVarInt64(make("0100 0000")));
		assertEquals(-19, XSignedVarint.readLexVarInt64(make("0110 1101")));
		assertEquals(-4, XSignedVarint.readLexVarInt64(make("0111 1100")));
		assertEquals(-1, XSignedVarint.readLexVarInt64(make("0111 1111")));
		assertEquals(0, XSignedVarint.readLexVarInt64(make("1000 0000")));
		assertEquals(1, XSignedVarint.readLexVarInt64(make("1000 0001")));
		assertEquals(19, XSignedVarint.readLexVarInt64(make("1001 0011")));
		assertEquals(63, XSignedVarint.readLexVarInt64(make("1011 1111")));
		assertEquals(64, XSignedVarint.readLexVarInt64(make("1100 0000", "0100 0000")));
		assertEquals(Long.MAX_VALUE, XSignedVarint.readLexVarInt64(make("11111111", "11111111", "11111111", "11111111", "11111111", "11111111", "11111111", "11111111", "11111111")));
	}
	
	@Test
	public void testXSignedVarintSimple2() throws DataLayerException {
		assertEquals("000000000000000000000000000000000000000000000000000000000000000000000000", writeString(Long.MIN_VALUE));
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
		assertEquals("111111111111111111111111111111111111111111111111111111111111111111111111", writeString(Long.MAX_VALUE));
	}
	
	@Test
	public void testXSignedVarIntEncodeDecode() throws DataLayerException {
		int numtests = 1000;
		Random r = new Random(0);
		
		long min = 0;
		long max = 64;
		
		for (int size = 1; size < 3; size++) {
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
				int sizeWritten = XSignedVarint.writeLexVarInt64(b, value);
				assertEquals(size, sizeWritten);
	
				b.rewind();
				long valueRead = XSignedVarint.readLexVarInt64(b);
				assertEquals(value, valueRead);
				
				b.rewind();
				sizeWritten = XSignedVarint.writeLexVarInt64(b, -value-1);
				assertEquals(size, sizeWritten);
	
				b.rewind();
				valueRead = XSignedVarint.readLexVarInt64(b);
				assertEquals(-value-1, valueRead);
			}
			
			min = max;
			max *= 64;
		}

		ByteBuffer b = ByteBuffer.allocate(9);
		for (int i = 0; i < numtests; i++) {
			long value;
			do {
				value = r.nextLong();
			} while (value < Long.MAX_VALUE/2);

			assertTrue(value >= Long.MAX_VALUE/2);
			assertTrue(value < Long.MAX_VALUE);
			
			b.rewind();
			int sizeWritten = XSignedVarint.writeLexVarInt64(b, value);
			assertEquals(9, sizeWritten);

			b.rewind();
			long valueRead = XSignedVarint.readLexVarInt64(b);
			assertEquals(value, valueRead);
			
			b.rewind();
			sizeWritten = XSignedVarint.writeLexVarInt64(b, -value-1);
			assertEquals(9, sizeWritten);

			b.rewind();
			valueRead = XSignedVarint.readLexVarInt64(b);
			assertEquals(-value-1, valueRead);
		}
	}
}
