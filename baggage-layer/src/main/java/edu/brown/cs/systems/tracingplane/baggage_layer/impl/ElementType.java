package edu.brown.cs.systems.tracingplane.baggage_layer.impl;

public class ElementType {

	private static final BagType[] bagTypes = new BagType[8];

	/** Only the first three bits are used for the bag type */
	public static enum BagType {
		Data(0), Overflow(1), IndexedField(2), NamedField(3);

		public final int id;
		public final byte byteValue;

		private BagType(int id) {
			this.id = id;
			this.byteValue = (byte) ((id << 5) & 0xE0);
			bagTypes[id] = this;
		}

		public static BagType fromByte(byte b) {
			int id = (b & 0xE0) >>> 5;
			return bagTypes[id];
		}

		public boolean match(byte b) {
			return (b & 0xE0) == byteValue;
		}

	}

	/**
	 * NamedField and IndexedField use remaining five bits for field level.
	 * Lowest level (eg root) is lexicographically highest
	 */
	public static class IndexedField {

		public static final int MinLevel = 0;
		public static final int MaxLevel = 31;

		public static final BagType bagType = BagType.IndexedField;

		public static final IndexedField[] levels = new IndexedField[32];

		static {
			for (int i = 0; i < 32; i++) {
				levels[i] = new IndexedField(i);
			}
		}

		public final int level;
		public final byte byteValue;

		private IndexedField(int level) {
			this.level = level;
			this.byteValue = (byte) (bagType.byteValue | (31 - level));
		}
		
		public static int level(byte b) {
			if (!bagType.match(b)) {
				return -1;
			}
			return 31 - (b & 0x1F);
		}

		public static IndexedField fromByte(byte b) {
			int level = level(b);
			if (level < 0 || level >= levels.length) {
				return null;
			} else {
				return levels[level];
			}
		}

	}

	/**
	 * NamedField and IndexedField use remaining five bits for field level.
	 * Lowest level (eg root) is lexicographically highest
	 */
	public static class NamedField {
		
		public static final int MinLevel = 0;
		public static final int MaxLevel = 31;

		public static final BagType bagType = BagType.NamedField;

		public static final NamedField[] levels = new NamedField[32];

		static {
			for (int i = 0; i < 32; i++) {
				levels[i] = new NamedField(i);
			}
		}

		public final int level;
		public final byte byteValue;

		private NamedField(int level) {
			this.level = level;
			this.byteValue = (byte) (bagType.byteValue | (31 - level));
		}
		
		public static int level(byte b) {
			if (!bagType.match(b)) {
				return -1;
			}
			return 31 - (b & 0x1F);
		}

		public static NamedField fromByte(byte b) {
			int level = level(b);
			if (level < 0 || level >= levels.length) {
				return null;
			} else {
				return levels[level];
			}
		}

	}
	
	/** Overflow has no use for additional bits */
	public static class Overflow {
		
		public static final BagType bagType = BagType.Overflow;
		
		public final static byte byteValue;
		
		static {
			byteValue = bagType.byteValue;
		}
		
	}
	
	/** FieldData is data for the current bag and has the byte value of 0 */
	public static class FieldData {
		
		public static final BagType bagType = BagType.Data;
		
		public static final byte byteValue = bagType.byteValue;
		
	}
	
	/** InlineFieldData is also data, but inlined for a sub-bag with id in the range [0, 31) (EXCLUDING 31) */
	public static class InlineFieldData {
		
		public static final BagType bagType = BagType.Data;

		public static final byte[] inlineField = new byte[31];

		static {
			for (int i = 0; i < 31; i++) {
				inlineField[i] = (byte) (bagType.byteValue | (i + 1));
			}
		}
		
		public static boolean isInlineData(byte b) {
			return bagType.match(b);
		}
		
		public static boolean idCanBeInlined(int id) {
			return id >= 0 && id < 31;
		}
		
		public static byte inlined(int id) {
			if (!idCanBeInlined(id)) {
				return -1;
			}
			return inlineField[id];
		}
		
		public static int fieldId(byte b) {
			return (b & 0x1F) - 1;
		}
		
	}

}
