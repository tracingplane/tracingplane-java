package edu.brown.cs.systems.tracingplane.baggage_layer.impl;

/** Only the first three bits are used for the bag type */
public class AtomTypes {

	private AtomTypes() {
	}

	private static final AtomType[] bagTypes = new AtomType[8];

	public enum AtomType {
		Data(0), Overflow(1), IndexedHeader(2), KeyedHeader(3);

		public final int id;
		public final byte byteValue;

		private AtomType(int id) {
			this.id = id;
			this.byteValue = (byte) ((id << 5) & 0xE0);
			bagTypes[id] = this;
		}

		public static AtomType fromByte(byte b) {
			int id = (b & 0xE0) >>> 5;
			return bagTypes[id];
		}

		public boolean match(byte b) {
			return (b & 0xE0) == byteValue;
		}

	}
}