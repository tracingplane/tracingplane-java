package edu.brown.cs.systems.tracingplane.transit_layer;

import java.io.InputStream;
import java.io.OutputStream;

public class TransitLayerNullImpl {
	
	static final Baggage NULL_BAGGAGE = new NullBaggage();
	
	static class NullBaggage implements Baggage {
		
	}
	
	static class NullTransitLayer implements TransitLayer {

		public Baggage newInstance() {
			return NULL_BAGGAGE;
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
