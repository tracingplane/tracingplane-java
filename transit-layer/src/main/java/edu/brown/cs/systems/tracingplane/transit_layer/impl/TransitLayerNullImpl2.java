package edu.brown.cs.systems.tracingplane.transit_layer.impl;

import java.io.InputStream;
import java.io.OutputStream;

import edu.brown.cs.systems.tracingplane.transit_layer.Baggage2;
import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayer2;
import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayerFactory;

public class TransitLayerNullImpl2 {

	private TransitLayerNullImpl2() {
	}

	public static final Baggage2 NULL_BAGGAGE = new NullBaggage();

	public static class NullTransitLayerFactory implements TransitLayerFactory {

		public TransitLayer2<?> newTransitLayer() {
			return new NullTransitLayer();
		}

	}

	public static class NullBaggage implements Baggage2 {

	}

	public static class NullTransitLayer implements TransitLayer2<Baggage2> {

		public boolean isInstance(Baggage2 baggage) {
			return baggage == null || baggage instanceof NullBaggage;
		}

		public Baggage2 newInstance() {
			return NULL_BAGGAGE;
		}

		public void discard(Baggage2 baggage) {
		}

		public Baggage2 branch(Baggage2 from) {
			return NULL_BAGGAGE;
		}

		public Baggage2 join(Baggage2 left, Baggage2 right) {
			return NULL_BAGGAGE;
		}

		@Override
		public Baggage2 deserialize(byte[] serialized, int offset, int length) {
			return NULL_BAGGAGE;
		}

		@Override
		public Baggage2 readFrom(InputStream in) {
			return NULL_BAGGAGE;
		}

		@Override
		public byte[] serialize(Baggage2 instance) {
			return new byte[0];
		}

		@Override
		public void writeTo(OutputStream out, Baggage2 instance) {
		}
	}

}
