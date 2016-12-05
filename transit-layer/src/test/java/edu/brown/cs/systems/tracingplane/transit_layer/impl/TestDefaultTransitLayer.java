package edu.brown.cs.systems.tracingplane.transit_layer.impl;

import org.junit.Test;

import edu.brown.cs.systems.tracingplane.transit_layer.Baggage2;
import junit.framework.TestCase;

public class TestDefaultTransitLayer extends TestCase {

	@Test
	public void testNullTransitLayerImpl() {
		assertNotNull(Baggage2.transit);
		assertTrue(Baggage2.transit instanceof TransitLayerNullImpl2.NullTransitLayer);

		Baggage2 baggage = Baggage2.newInstance();
		assertNotNull(baggage);
		assertEquals(baggage, TransitLayerNullImpl2.NULL_BAGGAGE);
	}

}
