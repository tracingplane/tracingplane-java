package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.systems.tracingplane.atom_layer.types.AtomLayerException;
import edu.brown.cs.systems.tracingplane.atom_layer.types.Lexicographic;
import edu.brown.cs.systems.tracingplane.atom_layer.types.TypeUtils;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixTypes.AtomType;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixTypes.HeaderType;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixTypes.Level;

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
			prefixes[i] = new UnsupportedPrefix((byte) i);
		}

		// Construct all valid prefixes
		List<AtomPrefix> allowedAtoms = new ArrayList<>(256);
		allowedAtoms.add(DataPrefix.prefix());
		for (IndexedHeaderPrefix atom : IndexedHeaderPrefix.prefixes) {
			allowedAtoms.add(atom);
		}
		for (KeyedHeaderPrefix atom : KeyedHeaderPrefix.prefixes) {
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

	private AtomPrefixes() {
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

	public static abstract class HeaderPrefix extends AtomPrefix {

		public static final AtomType atomType = AtomType.Header;

		protected final Level level;
		protected final HeaderType headerType;

		public HeaderPrefix(Level level, HeaderType headerType) {
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

	public static class IndexedHeaderPrefix extends HeaderPrefix {

		public static final HeaderType headerType = HeaderType.Indexed;

		private static final IndexedHeaderPrefix[] prefixes;

		static {
			prefixes = new IndexedHeaderPrefix[Level.LEVELS];
			for (int level = 0; level < Level.LEVELS; level++) {
				prefixes[level] = new IndexedHeaderPrefix(Level.get(level));
			}
		}

		public static IndexedHeaderPrefix prefixFor(int level) {
			return prefixes[level];
		}

		private IndexedHeaderPrefix(Level level) {
			super(level, headerType);
		}

		@Override
		public String toString() {
			return String.format("[IndexedHeaderPrefix prefix=%s level=%d]", TypeUtils.toBinaryString(prefix),
					level.level);
		}

		@Override
		BagKey parse(ByteBuffer buf) throws AtomLayerException {
			return BagKeySerialization.parseIndexed(buf);
		}

	}

	public static class KeyedHeaderPrefix extends HeaderPrefix {

		public static final HeaderType headerType = HeaderType.Keyed;

		private static final KeyedHeaderPrefix[] prefixes;

		static {
			prefixes = new KeyedHeaderPrefix[Level.LEVELS];
			for (int level = 0; level < Level.LEVELS; level++) {
				prefixes[level] = new KeyedHeaderPrefix(Level.get(level));
			}
		}

		public static KeyedHeaderPrefix prefixFor(int level) {
			return prefixes[level];
		}

		private KeyedHeaderPrefix(Level level) {
			super(level, headerType);
		}

		@Override
		public String toString() {
			return String.format("[KeyedHeaderPrefix   prefix=%s level=%d]", TypeUtils.toBinaryString(prefix),
					level.level);
		}

		@Override
		BagKey parse(ByteBuffer buf) throws AtomLayerException {
			return BagKeySerialization.parseKeyed(buf);
		}

	}

	public static class DataPrefix extends AtomPrefix {

		public static final AtomType atomType = AtomType.Data;
		public static final byte prefix = atomType.byteValue;
		static final DataPrefix instance = new DataPrefix();

		private DataPrefix() {
			super(atomType, DataPrefix.prefix);
		}

		public static DataPrefix prefix() {
			return instance;
		}

		@Override
		public boolean isData() {
			return true;
		}

		@Override
		public String toString() {
			return String.format("[DataPrefix prefix=%s]", TypeUtils.toBinaryString(super.prefix));
		}

	}

	public static class UnsupportedPrefix extends AtomPrefix {

		private UnsupportedPrefix(byte prefix) {
			super(null, prefix);
		}

		@Override
		boolean isValid() {
			return false;
		}

		@Override
		public String toString() {
			return String.format("[UnsupportedPrefix   prefix=%s]", TypeUtils.toBinaryString(prefix));
		}

	}

	public static void main(String[] args) {
		for (int i = 0; i < prefixes.length; i++) {
			System.out.printf("%d: %s\n", i, prefixes[i]);
		}
	}

}
