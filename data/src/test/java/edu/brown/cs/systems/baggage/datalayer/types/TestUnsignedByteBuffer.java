package edu.brown.cs.systems.baggage.datalayer.types;

import java.nio.ByteBuffer;
import java.util.Comparator;

import org.junit.Test;

import edu.brown.cs.systems.baggage.datalayer.impl.Utils;
import edu.brown.cs.systems.baggage.datalayer.types.UnsignedByteBuffer;
import junit.framework.TestCase;

public class TestUnsignedByteBuffer extends TestCase {
	
	private static ByteBuffer make(String... ss) {
		ByteBuffer buf = ByteBuffer.allocate(ss.length);
		for (String s : ss) {
			buf.put(Utils.makeByte(s));
		}
		buf.rewind();
		return buf;
	}
	
	@Test
	public void testUnsafeComparatorExists() {
		Comparator<ByteBuffer> comparator = UnsignedByteBuffer.lexicographicalComparator();
		
		assertTrue(comparator instanceof UnsignedByteBuffer.LexicographicalComparatorHolder.UnsafeComparator);
	}
	
	@Test
	public void testJavaComparatorExists() {
		Comparator<ByteBuffer> comparator = UnsignedByteBuffer.lexicographicalComparatorJavaImpl();
		
		assertTrue(comparator instanceof UnsignedByteBuffer.LexicographicalComparatorHolder.PureJavaComparator);
	}
	
	@Test
	public void testUnsafeComparator() {
		Comparator<ByteBuffer> comparator = UnsignedByteBuffer.lexicographicalComparator();
		
		ByteBuffer a = make("0000 0000");
		ByteBuffer b = make("1000 0000");

		assertTrue(comparator.compare(a, a) == 0);
		assertTrue(comparator.compare(b, b) == 0);
		assertTrue(comparator.compare(a, b) < 0);
		assertTrue(comparator.compare(b, a) > 0);
	}
	
	@Test
	public void testUnsafeComparatorWithLong() {
		Comparator<ByteBuffer> comparator = UnsignedByteBuffer.lexicographicalComparator();
		
		ByteBuffer a = make("0000 0000", "0000 0000", "0000 0000", "0000 0000", "0000 0000", "0000 0000", "0000 0000", "0000 0000", "0000 0000");
		ByteBuffer b = make("1000 0000", "0000 0000", "0000 0000", "0000 0000", "0000 0000", "0000 0000", "0000 0000", "0000 0000", "0000 0000");

		assertTrue(comparator.compare(a, a) == 0);
		assertTrue(comparator.compare(b, b) == 0);
		assertTrue(comparator.compare(a, b) < 0);
		assertTrue(comparator.compare(b, a) > 0);
	}
	
	@Test
	public void testUnsafeComparatorWithLongAndRemaining() {
		Comparator<ByteBuffer> comparator = UnsignedByteBuffer.lexicographicalComparator();
		
		ByteBuffer a = make("0000 0000", "0000 0000", "0000 0000", "0000 0000", "0000 0000", "0000 0000", "0000 0000", "0000 0000", "0000 0000");
		ByteBuffer b = make("0000 0000", "0000 0000", "0000 0000", "0000 0000", "0000 0000", "0000 0000", "0000 0000", "0000 0000", "1000 0000");

		assertTrue(comparator.compare(a, a) == 0);
		assertTrue(comparator.compare(b, b) == 0);
		assertTrue(comparator.compare(a, b) < 0);
		assertTrue(comparator.compare(b, a) > 0);
	}

}
