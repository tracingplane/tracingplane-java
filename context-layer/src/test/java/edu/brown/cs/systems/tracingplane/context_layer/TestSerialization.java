package edu.brown.cs.systems.tracingplane.context_layer;

import org.junit.Test;

import junit.framework.TestCase;

public class TestSerialization extends TestCase {
	
	@Test
	public void testSerializeNulls() {
		
		assertNull(BaggageImplSerialization.serialize(null));
		
		BaggageImpl baggage = new BaggageImpl();
		
		assertNull(BaggageImplSerialization.serialize(baggage));
		
	}

}
