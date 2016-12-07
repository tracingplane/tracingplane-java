package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

/** Only the first two bits are used for the bag type */
public class AtomTypes {

	private AtomTypes() {
	}

	private static final AtomType[] bagTypes = new AtomType[4];
	private static final HeaderType[] headerTypes = new HeaderType[4]; 

	public enum AtomType {
		Data(0), Header(1);

		public final int id;
		public final byte byteValue;

		private AtomType(int id) {
			this.id = id;
			this.byteValue = (byte) ((id << 6) & 0xC0);
			bagTypes[id] = this;
		}

		public static AtomType fromByte(byte b) {
			int id = (b & 0xC0) >>> 6;
			return bagTypes[id];
		}

		public boolean match(byte b) {
			return (b & 0xC0) == byteValue;
		}
	}
	
	public enum HeaderType {
		Indexed(0), Keyed(1);
		
		public final int id;
		public final byte byteValue;
		
		private HeaderType(int id) {
			this.id = id;
			this.byteValue = (byte) (id & 0x03);
		}
		
		public static HeaderType fromByte(byte b) {
			int id = b & 0x03;
			return headerTypes[id];
		}
		
		public boolean match(byte b) {
			return (b & 0x03) == byteValue;
		}
		
	}
	
	public static class Level {
		
		public static final int LEVELS = 16;
		private static final Level[] levels = new Level[LEVELS];
		static {
			for (int i = 0; i < LEVELS; i++) {
				levels[i] = new Level(i);
			}
		}
		
		public final int level;
		public final byte byteValue;
		
		private Level(int level) {
			this.level = level;
			this.byteValue = (byte) (((15 - level) << 2) & 0x3C);
		}
		
		public static Level get(int level) {
			return levels[level];
		}
		
		public static int valueOf(byte b) {
			return 15 - ((b & 0x3C) >> 2);
		}
		
		public static Level fromByte(byte b) {
			return levels[valueOf(b)];
		}
		
		public boolean match(byte b) {
			return (b & 0x3C) == byteValue;
		}
		
		
	}
}