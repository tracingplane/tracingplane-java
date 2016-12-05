package edu.brown.cs.systems.tracingplane.context_layer;

import org.junit.Test;

import edu.brown.cs.systems.tracingplane.context_layer.impl.RawAtomsContextLayer;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage2;
import edu.brown.cs.systems.tracingplane.transit_layer.impl.TransitLayerNullImpl2;
import junit.framework.TestCase;

public class TestDefaultTransitLayer extends TestCase {

	@Test
	public void testDefaultTransitLayer() {
		// The context layer is configured to be the default transit layer --
		// test that this is the case

		assertNotNull(Baggage2.transit);
		assertFalse(Baggage2.transit instanceof TransitLayerNullImpl2.NullTransitLayer);
		assertTrue(Baggage2.transit instanceof TransitLayerImpl);

		TransitLayerImpl<?> transit = (TransitLayerImpl<?>) Baggage2.transit;
		assertNotNull(transit.contextLayer);
		assertTrue(transit.contextLayer instanceof RawAtomsContextLayer);

		Baggage2 baggage = Baggage2.newInstance();
		assertNull(baggage);
	}

}
