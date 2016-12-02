package edu.brown.cs.systems.tracingplane.baggage_layer.impl;

import org.junit.Test;

import edu.brown.cs.systems.tracingplane.baggage_layer.impl.AtomPrefixes.BagType;
import edu.brown.cs.systems.tracingplane.baggage_layer.impl.AtomPrefixes.DataPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.impl.AtomPrefixes.IndexedBagHeaderPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.impl.AtomPrefixes.InlineFieldPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.impl.AtomPrefixes.KeyedBagHeaderPrefix;
import junit.framework.TestCase;

public class TestElementType extends TestCase {
	
	@Test
	public void testBagType() {
		assertEquals(0, AtomType.DataPrefix.id);
		assertEquals((byte) 0, AtomType.DataPrefix.byteValue);
		assertTrue(AtomType.DataPrefix.match((byte) 0));
		
		assertEquals(1, AtomType.OverflowPrefix.id);
		assertEquals((byte) 32, AtomType.OverflowPrefix.byteValue);
		assertTrue(AtomType.OverflowPrefix.match((byte) 32));
		
		assertEquals(2, AtomType.IndexedBagHeader.id);
		assertEquals((byte) 64, AtomType.IndexedBagHeader.byteValue);
		assertTrue(AtomType.IndexedBagHeader.match((byte) 64));
		
		assertEquals(3, AtomType.NamedField.id);
		assertEquals((byte) 96, AtomType.NamedField.byteValue);
		assertTrue(AtomType.NamedField.match((byte) 96));
	}
	
	@Test
	public void testIndexedField() {
		for (int i = 0; i < 32; i++) {
			IndexedBagHeaderPrefix f = IndexedBagHeaderPrefix.levels[i];
			assertEquals(i, f.level);
			assertEquals((byte) (95-i), f.byteValue);
			
			assertEquals(i, IndexedBagHeaderPrefix.level((byte) (95-i)));
			
			IndexedBagHeaderPrefix f2 = IndexedBagHeaderPrefix.fromByte((byte) (95-i));
			assertEquals(f, f2);
		}
	}
	
	@Test
	public void testNamedField() {
		for (int i = 0; i < 32; i++) {
			KeyedBagHeaderPrefix f = KeyedBagHeaderPrefix.levels[i];
			assertEquals(i, f.level);
			assertEquals((byte) (127-i), f.byteValue);
			
			assertEquals(i, KeyedBagHeaderPrefix.level((byte) (127-i)));
			
			KeyedBagHeaderPrefix f2 = KeyedBagHeaderPrefix.fromByte((byte) (127-i));
			assertEquals(f, f2);
		}
	}
	
	@Test
	public void testFieldData() {
		assertEquals(0, DataPrefix.byteValue);
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
