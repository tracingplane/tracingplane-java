package edu.brown.cs.systems.tracingplane.context_layer;

import org.junit.Test;

import edu.brown.cs.systems.tracingplane.context_layer.impl.BlindContextLayer;
import edu.brown.cs.systems.tracingplane.context_layer.listener.ContextLayerNullListener;
import edu.brown.cs.systems.tracingplane.context_layer.protocol.TransitLayerImpl;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;
import edu.brown.cs.systems.tracingplane.transit_layer.impl.TransitLayerNullImpl;
import junit.framework.TestCase;

public class TestDefaultTransitLayer extends TestCase {
	
	@Test
	public void testDefaultTransitLayer() {
		// The context layer is configured to be the default transit layer -- test that this is the case
		
		assertNotNull(Baggage.transit);
		assertFalse(Baggage.transit instanceof TransitLayerNullImpl.NullTransitLayer);
		assertTrue(Baggage.transit instanceof TransitLayerImpl);
		
		TransitLayerImpl transit = (TransitLayerImpl) Baggage.transit;
		assertNotNull(transit.context);
		assertTrue(transit.context instanceof BlindContextLayer);
		assertNotNull(transit.listener);
		assertTrue(transit.listener instanceof ContextLayerNullListener);
		
		Baggage baggage = Baggage.newInstance();
		assertNull(baggage);
	}
	
}
