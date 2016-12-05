package edu.brown.cs.systems.tracingplane.atom_layer;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.junit.Test;

import edu.brown.cs.systems.tracingplane.atom_layer.AtomLayerSerialization;
import junit.framework.TestCase;

public class TestSerialization extends TestCase {
	
	@Test
	public void testSerializeNulls() {
		
		assertNull(AtomLayerSerialization.serialize(null));
		assertNull(AtomLayerSerialization.serialize(new ArrayList<ByteBuffer>()));
		
	}

}
