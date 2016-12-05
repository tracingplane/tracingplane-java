package edu.brown.cs.systems.tracingplane.atom_layer;

import org.junit.Test;

import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;
import junit.framework.TestCase;

public class TestAtomLayer extends TestCase {
	
	@Test
	public void testNewInstance() {
		assertNull(Baggage.transit.newInstance());
	}

}
