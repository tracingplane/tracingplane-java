package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.systems.tracingplane.atom_layer.types.AtomLayerException;
import edu.brown.cs.systems.tracingplane.atom_layer.types.Lexicographic;
import edu.brown.cs.systems.tracingplane.atom_layer.types.TypeUtils;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomTypes.AtomType;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomTypes.HeaderType;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomTypes.Level;

/**
 * Logic for byte prefixes of serialized bags
 * 
 * TODO: documentation
 */
public class AtomPrefixes {

	private static final AtomPrefix[] prefixes;

	static {
		// Initialize all prefixes as unsupported
		prefixes = new AtomPrefix[256];
		for (int i = 0; i < prefixes.length; i++) {
			prefixes[i] = new UnsupportedPrefixAtom((byte) i);
		}

		// Construct all valid prefixes
		List<AtomPrefix> allowedAtoms = new ArrayList<>(256);
		allowedAtoms.add(DataAtom.prefix());
		for (IndexedHeaderAtom atom : IndexedHeaderAtom.prefixes) {
			allowedAtoms.add(atom);
		}
		for (KeyedHeaderAtom atom : KeyedHeaderAtom.prefixes) {
			allowedAtoms.add(atom);
		}

		// Insert into array
		for (AtomPrefix atom : allowedAtoms) {
			if (atom.prefix >= 0) {
				prefixes[atom.prefix] = atom;
			} else {
				prefixes[256 + atom.prefix] = atom;
			}
		}
	}

	public static AtomPrefix get(byte prefix) {
		if (prefix >= 0) {
			return prefixes[prefix];
		} else {
			return prefixes[256 + prefix];
		}
	}

	public static abstract class AtomPrefix implements Comparable<AtomPrefix> {

		protected final AtomType atomType;
		public final byte prefix;

		public AtomPrefix(AtomType atomType, byte prefix) {
			this.atomType = atomType;
			this.prefix = prefix;
		}

		boolean isValid() {
			return true;
		}

		boolean isHeader() {
			return false;
		}

		boolean isData() {
			return false;
		}

		int level(int currentLevel) {
			return currentLevel + 1;
		}

		@Override
		public int compareTo(AtomPrefix o) {
			return Lexicographic.compare(prefix, o.prefix);
		}
	}

	public static abstract class HeaderAtom extends AtomPrefix {

		public static final AtomType atomType = AtomType.Header;

		protected final Level level;
		protected final HeaderType headerType;

		public HeaderAtom(Level level, HeaderType headerType) {
			super(AtomType.Header, (byte) (AtomType.Header.byteValue | level.byteValue | headerType.byteValue));
			this.level = level;
			this.headerType = headerType;
		}

		@Override
		boolean isHeader() {
			return true;
		}
		
		int level() {
			return this.level.level;
		}

		@Override
		int level(int currentLevel) {
			return this.level.level;
		}

		abstract BagKey parse(ByteBuffer buf) throws AtomLayerException;

	}

	public static class IndexedHeaderAtom extends HeaderAtom {

		public static final HeaderType headerType = HeaderType.Indexed;

		private static final IndexedHeaderAtom[] prefixes;

		static {
			prefixes = new IndexedHeaderAtom[Level.LEVELS];
			for (int level = 0; level < Level.LEVELS; level++) {
				prefixes[level] = new IndexedHeaderAtom(Level.get(level));
			}
		}

		public static IndexedHeaderAtom prefixFor(int level) {
			return prefixes[level];
		}

		private IndexedHeaderAtom(Level level) {
			super(level, headerType);
		}

		@Override
		public String toString() {
			return String.format("[IndexedHeaderAtom prefix=%s level=%d]", TypeUtils.toBinaryString(prefix),
					level.level);
		}

		@Override
		BagKey parse(ByteBuffer buf) throws AtomLayerException {
			return BagKeySerialization.parseIndexed(buf);
		}

	}

	public static class KeyedHeaderAtom extends HeaderAtom {

		public static final HeaderType headerType = HeaderType.Keyed;

		private static final KeyedHeaderAtom[] prefixes;

		static {
			prefixes = new KeyedHeaderAtom[Level.LEVELS];
			for (int level = 0; level < Level.LEVELS; level++) {
				prefixes[level] = new KeyedHeaderAtom(Level.get(level));
			}
		}

		public static KeyedHeaderAtom prefixFor(int level) {
			return prefixes[level];
		}

		private KeyedHeaderAtom(Level level) {
			super(level, headerType);
		}

		@Override
		public String toString() {
			return String.format("[KeyedHeaderAtom   prefix=%s level=%d]", TypeUtils.toBinaryString(prefix),
					level.level);
		}

		@Override
		BagKey parse(ByteBuffer buf) throws AtomLayerException {
			return BagKeySerialization.parseKeyed(buf);
		}

	}

	public static class DataAtom extends AtomPrefix {

		public static final AtomType atomType = AtomType.Data;
		public static final byte prefix = atomType.byteValue;
		private static final DataAtom instance = new DataAtom();

		private DataAtom() {
			super(atomType, DataAtom.prefix);
		}

		public static DataAtom prefix() {
			return instance;
		}

		@Override
		public boolean isData() {
			return true;
		}

		@Override
		public String toString() {
			return String.format("[DataAtom prefix=%s]", TypeUtils.toBinaryString(super.prefix));
		}

	}

	public static class UnsupportedPrefixAtom extends AtomPrefix {

		private UnsupportedPrefixAtom(byte prefix) {
			super(null, prefix);
		}

		@Override
		boolean isValid() {
			return false;
		}

		@Override
		public String toString() {
			return String.format("[UnsupportedAtom   prefix=%s]", TypeUtils.toBinaryString(prefix));
		}

	}

	public static void main(String[] args) {
		for (int i = 0; i < prefixes.length; i++) {
			System.out.printf("%d: %s\n", i, prefixes[i]);
		}
	}

}
