package edu.brown.cs.systems.tracingplane.transit_layer.impl;

import org.junit.Test;

import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;
import junit.framework.TestCase;

public class TestDefaultTransitLayer extends TestCase {

	@Test
	public void testNullTransitLayerImpl() {
		assertNotNull(Baggage.transit);
		assertTrue(Baggage.transit instanceof NullTransitLayer);

		Baggage baggage = Baggage.newInstance();
		assertNotNull(baggage);
		assertEquals(baggage, NullBaggage.INSTANCE);
	}

}
