package edu.brown.cs.systems.tracingplane.baggage_layer.impl;

import org.junit.Test;

import edu.brown.cs.systems.tracingplane.baggage_layer.impl.ElementType.BagType;
import edu.brown.cs.systems.tracingplane.baggage_layer.impl.ElementType.FieldData;
import edu.brown.cs.systems.tracingplane.baggage_layer.impl.ElementType.IndexedField;
import edu.brown.cs.systems.tracingplane.baggage_layer.impl.ElementType.InlineFieldData;
import edu.brown.cs.systems.tracingplane.baggage_layer.impl.ElementType.NamedField;
import junit.framework.TestCase;

public class TestElementType extends TestCase {
	
	@Test
	public void testBagType() {
		assertEquals(0, BagType.Data.id);
		assertEquals((byte) 0, BagType.Data.byteValue);
		assertTrue(BagType.Data.match((byte) 0));
		
		assertEquals(1, BagType.Overflow.id);
		assertEquals((byte) 32, BagType.Overflow.byteValue);
		assertTrue(BagType.Overflow.match((byte) 32));
		
		assertEquals(2, BagType.IndexedField.id);
		assertEquals((byte) 64, BagType.IndexedField.byteValue);
		assertTrue(BagType.IndexedField.match((byte) 64));
		
		assertEquals(3, BagType.NamedField.id);
		assertEquals((byte) 96, BagType.NamedField.byteValue);
		assertTrue(BagType.NamedField.match((byte) 96));
	}
	
	@Test
	public void testIndexedField() {
		for (int i = 0; i < 32; i++) {
			IndexedField f = IndexedField.levels[i];
			assertEquals(i, f.level);
			assertEquals((byte) (95-i), f.byteValue);
			
			assertEquals(i, IndexedField.level((byte) (95-i)));
			
			IndexedField f2 = IndexedField.fromByte((byte) (95-i));
			assertEquals(f, f2);
		}
	}
	
	@Test
	public void testNamedField() {
		for (int i = 0; i < 32; i++) {
			NamedField f = NamedField.levels[i];
			assertEquals(i, f.level);
			assertEquals((byte) (127-i), f.byteValue);
			
			assertEquals(i, NamedField.level((byte) (127-i)));
			
			NamedField f2 = NamedField.fromByte((byte) (127-i));
			assertEquals(f, f2);
		}
	}
	
	@Test
	public void testFieldData() {
		assertEquals(0, FieldData.byteValue);
	}
	
	@Test
	public void testInlineFieldData() {
		for (int i = 0; i < 31; i++) {
			assertTrue(InlineFieldData.idCanBeInlined(i));
			assertEquals(i+1, InlineFieldData.inlined(i));
		}
		for (byte i = 32; i < 0 || i > 32; i++) {
			assertFalse(InlineFieldData.idCanBeInlined(i));
			assertEquals(-1, InlineFieldData.inlined(i));
		}
	}

}
