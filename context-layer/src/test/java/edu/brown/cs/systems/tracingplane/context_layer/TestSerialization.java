package edu.brown.cs.systems.tracingplane.context_layer;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.junit.Test;

import junit.framework.TestCase;

public class TestSerialization extends TestCase {
	
	@Test
	public void testSerializeNulls() {
		
		assertNull(ContextLayerSerialization.serialize(null));
		assertNull(ContextLayerSerialization.serialize(new ArrayList<ByteBuffer>()));
		
	}

}
