package edu.brown.cs.systems.tracingplane.baggage_layer.impl;

import edu.brown.cs.systems.tracingplane.baggage_layer.impl.AtomTypes.AtomType;

/** Logic for byte prefixes of serialized bags */
public class AtomPrefixes {
	
	public static final int LEVELS = 32;

	/**
	 * NamedField and IndexedField use remaining five bits for field level.
	 * Lowest level (eg root) is lexicographically highest
	 */
	public static class IndexedHeaderPrefix {
		
		private IndexedHeaderPrefix() {}

		public static final AtomType bagType = AtomType.IndexedHeader;
		
		public static final byte[] prefixes = new byte[LEVELS];

		static {
			for (int i = 0; i < LEVELS; i++) {
				prefixes[i] = (byte) (bagType.byteValue | (LEVELS - 1 - i));
			}
		}
		
		public static boolean isIndexedFieldPrefix(byte b) {
			return bagType.match(b);
		}
		
		public static int level(byte b) {
			return 31 - (b & 0x1F);
		}
		
		public static boolean isValidLevel(int level) {
			return level >= 0 && level < LEVELS;
		}
		
		public static byte prefixFor(int level) {
			return prefixes[level];
		}

	}

	/**
	 * NamedField and IndexedField use remaining five bits for field level.
	 * Lowest level (eg root) is lexicographically highest
	 */
	public static class KeyedHeaderPrefix {
		
		private KeyedHeaderPrefix() {}

		public static final AtomType bagType = AtomType.KeyedHeader;
		
		public static final byte[] prefixes = new byte[LEVELS];

		static {
			for (int i = 0; i < LEVELS; i++) {
				prefixes[i] = (byte) (bagType.byteValue | (LEVELS - 1 - i));
			}
		}
		
		public static boolean isKeyedFieldPrefix(byte b) {
			return bagType.match(b);
		}
		
		public static int level(byte b) {
			return 31 - (b & 0x1F);
		}
		
		public static boolean isValidLevel(int level) {
			return level >= 0 && level < LEVELS;
		}
		
		public static byte prefixFor(int level) {
			return prefixes[level];
		}

	}
	
	/** Overflow has no use for additional bits */
	public static class OverflowPrefix {
		
		private OverflowPrefix() {}
		
		public static final AtomType bagType = AtomType.Overflow;
		
		public final static byte prefix = bagType.byteValue;
		
	}
	
	/** FieldData is data for the current bag and has the byte value of 0 */
	public static class DataPrefix {
		
		public static final AtomType bagType = AtomType.Data;
		
		public static final byte prefix = bagType.byteValue;
		
	}
	
	/** InlineFieldData is also data, but inlined for a sub-bag with id in the range [0, 31) (EXCLUDING 31) */
	public static class InlineFieldPrefix {
		
		public static final AtomType bagType = AtomType.Data;

		public static final byte[] prefixes = new byte[31];

		static {
			for (int i = 0; i < 31; i++) {
				prefixes[i] = (byte) (bagType.byteValue | (i + 1));
			}
		}
		
		public static boolean isInlineData(byte b) {
			return bagType.match(b);
		}
		
		public static int fieldId(byte b) {
			return (b & 0x1F) - 1;
		}
		
		public static boolean idCanBeInlined(int id) {
			return id >= 0 && id < 31;
		}
		
		public static byte prefixFor(int id) {
			return prefixes[id];
		}
		
	}

}
