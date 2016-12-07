package edu.brown.cs.systems.tracingplane.baggage_layer;

import java.nio.ByteBuffer;

import edu.brown.cs.systems.tracingplane.atom_layer.BaggageAtoms;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.DataAtom;

//TODO: description and method documentation
public interface BaggageContents extends BaggageAtoms {
	
	public static final ByteBuffer TRIM_MARKER = ByteBuffer.wrap(new byte[] { DataAtom.prefix().prefix });

	public static final BaggageLayer<?> baggageLayer = BaggageLayerFactory.createDefaultBaggageLayer();
	
	
	// TODO: static methods here for accessing data in bags, plus methods for accessing thread local baggage contents

}
