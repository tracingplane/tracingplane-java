package edu.brown.cs.systems.tracingplane.baggage_layer.impl;

import org.junit.Test;

import edu.brown.cs.systems.tracingplane.baggage_layer.impl.AtomPrefixes.DataPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.impl.AtomPrefixes.IndexedHeaderPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.impl.AtomPrefixes.InlineFieldPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.impl.AtomPrefixes.KeyedHeaderPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.impl.AtomTypes.AtomType;
import junit.framework.TestCase;

public class TestElementType extends TestCase {
	
	@Test
	public void testBagType() {
		assertEquals(0, AtomType.Data.id);
		assertEquals((byte) 0, AtomType.Data.byteValue);
		assertTrue(AtomType.Data.match((byte) 0));
		
		assertEquals(1, AtomType.Overflow.id);
		assertEquals((byte) 32, AtomType.Overflow.byteValue);
		assertTrue(AtomType.Overflow.match((byte) 32));
		
		assertEquals(2, AtomType.IndexedHeader.id);
		assertEquals((byte) 64, AtomType.IndexedHeader.byteValue);
		assertTrue(AtomType.IndexedHeader.match((byte) 64));
		
		assertEquals(3, AtomType.KeyedHeader.id);
		assertEquals((byte) 96, AtomType.KeyedHeader.byteValue);
		assertTrue(AtomType.KeyedHeader.match((byte) 96));
	}
	
	@Test
	public void testIndexedField() {
		for (int i = 0; i < 32; i++) {
			assertTrue(IndexedHeaderPrefix.isValidLevel(i));
			assertEquals((byte) (95 - i), IndexedHeaderPrefix.prefixes[i]);
			
			assertEquals(i, IndexedHeaderPrefix.level((byte) (95 - i)));
		}
		for (int i = -10; i < 0; i++) {
			assertFalse(IndexedHeaderPrefix.isValidLevel(i));
		}
		for (int i = 32; i < 100; i++) {
			assertFalse(IndexedHeaderPrefix.isValidLevel(i));
		}
	}
	
	@Test
	public void testNamedField() {
		for (int i = 0; i < 32; i++) {
			assertTrue(KeyedHeaderPrefix.isValidLevel(i));
			assertEquals((byte) (127 - i), KeyedHeaderPrefix.prefixes[i]);
			
			assertEquals(i, KeyedHeaderPrefix.level((byte) (127 - i)));
		}
		for (int i = -10; i < 0; i++) {
			assertFalse(KeyedHeaderPrefix.isValidLevel(i));
		}
		for (int i = 32; i < 100; i++) {
			assertFalse(KeyedHeaderPrefix.isValidLevel(i));
		}
	}
	
	@Test
	public void testFieldData() {
		assertEquals(0, DataPrefix.prefix);
	}
	
	@Test
	public void testInlineFieldData() {
		for (int i = 0; i < 31; i++) {
			assertTrue(InlineFieldPrefix.idCanBeInlined(i));
			assertEquals(i+1, InlineFieldPrefix.prefixFor(i));
		}
		for (byte i = 32; i < 0 || i > 32; i++) {
			assertFalse(InlineFieldPrefix.idCanBeInlined(i));
			assertEquals(-1, InlineFieldPrefix.prefixFor(i));
		}
	}

}
