package edu.brown.cs.systems.tracingplane.context_layer;

import org.junit.Test;

import edu.brown.cs.systems.tracingplane.context_layer.ContextLayerListener.ContextLayerListenerContainer;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;
import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayerNullImpl;
import junit.framework.TestCase;

public class TestDefaultTransitLayer extends TestCase {
	
	@Test
	public void testNotNullTransitLayerImpl() {
		// The context layer is configured to be the default transit layer -- test that this is the case
		
		assertNotNull(Baggage.transit);
		assertFalse(Baggage.transit instanceof TransitLayerNullImpl.NullTransitLayer);
		assertTrue(Baggage.transit instanceof ContextLayer);
		
		Baggage baggage = Baggage.newInstance();
		assertNotNull(baggage);
		assertTrue(baggage instanceof BaggageImpl);
	}
	
	@Test
	public void testNullContextLayerListeners() {
		// In this package, no context layer listeners should be configured, so here we make sure that's true
		
		assertTrue(Baggage.transit instanceof ContextLayer);
		ContextLayer ctxLayer = (ContextLayer) Baggage.transit;
		
		assertNotNull(ctxLayer.listener);
		assertTrue(ctxLayer.listener instanceof ContextLayerListenerContainer);
		ContextLayerListenerContainer listener = (ContextLayerListenerContainer) ctxLayer.listener;
		
		assertEquals(0, listener.size);
		assertNotNull(listener.listeners);
		assertEquals(0, listener.listeners.length);
	}
	
}
