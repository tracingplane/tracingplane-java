package edu.brown.cs.systems.tracingplane.transit_layer.impl;

import java.io.InputStream;
import java.io.OutputStream;

import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;
import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayer;

public class TransitLayerNullImpl {

	private TransitLayerNullImpl() {
	}

	public static final Baggage NULL_BAGGAGE = new NullBaggage();

	public static class NullBaggage implements Baggage {

	}

	public static class NullTransitLayer implements TransitLayer {

		public Baggage newInstance() {
			return NULL_BAGGAGE;
		}
		
		public void discard(Baggage baggage) {
		}

		public Baggage branch(Baggage from) {
			return NULL_BAGGAGE;
		}

		public Baggage join(Baggage left, Baggage right) {
			return NULL_BAGGAGE;
		}

		@Override
		public Baggage deserialize(byte[] serialized, int offset, int length) {
			return NULL_BAGGAGE;
		}

		@Override
		public Baggage readFrom(InputStream in) {
			return NULL_BAGGAGE;
		}

		@Override
		public byte[] serialize(Baggage instance) {
			return new byte[0];
		}

		@Override
		public void writeTo(OutputStream out, Baggage instance) {
		}
	}

}
