package edu.brown.cs.systems.tracingplane.context_layer;

import org.junit.Test;

import edu.brown.cs.systems.tracingplane.transit_layer.Baggage2;
import junit.framework.TestCase;

public class TestContextLayer extends TestCase {
	
	@Test
	public void testNewInstance() {
		assertNull(Baggage2.transit.newInstance());
	}

}
