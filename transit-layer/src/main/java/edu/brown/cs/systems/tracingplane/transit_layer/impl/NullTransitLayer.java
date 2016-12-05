package edu.brown.cs.systems.tracingplane.transit_layer.impl;

import java.io.InputStream;
import java.io.OutputStream;

import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;
import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayer;

public class NullTransitLayer implements TransitLayer<Baggage> {

	public boolean isInstance(Baggage baggage) {
		return baggage == null || baggage instanceof NullBaggage;
	}

	public Baggage newInstance() {
		return NullBaggage.INSTANCE;
	}

	public void discard(Baggage baggage) {
	}

	public Baggage branch(Baggage from) {
		return NullBaggage.INSTANCE;
	}

	public Baggage join(Baggage left, Baggage right) {
		return NullBaggage.INSTANCE;
	}

	@Override
	public Baggage deserialize(byte[] serialized, int offset, int length) {
		return NullBaggage.INSTANCE;
	}

	@Override
	public Baggage readFrom(InputStream in) {
		return NullBaggage.INSTANCE;
	}

	@Override
	public byte[] serialize(Baggage instance) {
		return new byte[0];
	}

	@Override
	public void writeTo(OutputStream out, Baggage instance) {
	}
}