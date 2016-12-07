package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

import java.nio.ByteBuffer;

import edu.brown.cs.systems.tracingplane.atom_layer.types.AtomLayerException;
import edu.brown.cs.systems.tracingplane.atom_layer.types.ByteBuffers;
import edu.brown.cs.systems.tracingplane.atom_layer.types.Lexicographic;
import edu.brown.cs.systems.tracingplane.atom_layer.types.UnsignedLexVarint;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagOptions;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.HeaderAtom;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.IndexedHeaderAtom;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.KeyedHeaderAtom;

// TODO: test for comparison and write
public class BagKeySerialization {

	private BagKeySerialization() {
	}

	public static void write(int level, BagKey.Indexed field, ByteBuffer buf) {
		buf.put(IndexedHeaderAtom.prefixFor(level).prefix);
		UnsignedLexVarint.writeLexVarUInt32(buf, field.index);
		BagOptionsSerialization.write(field.options, buf);
	}

	public static void write(int level, BagKey.Keyed field, ByteBuffer buf) {
		buf.put(KeyedHeaderAtom.prefixFor(level).prefix);
		UnsignedLexVarint.writeLexVarUInt32(buf, field.key.remaining());
		ByteBuffers.copyTo(field.key, buf);
		BagOptionsSerialization.write(field.options, buf);
	}
	
	public static BagKey parse(HeaderAtom prefix, ByteBuffer buf) throws AtomLayerException {
		buf.mark();
		try {
			switch(prefix.headerType) {
			case Indexed: return parseIndexed(buf);
			case Keyed: return parseKeyed(buf);
			default: throw new AtomLayerException("Unknown header type " + prefix.headerType);
			}
		} finally {
			buf.reset();
		}
	}
	
	public static BagKey parseIndexed(ByteBuffer buf) throws AtomLayerException {
		int index = UnsignedLexVarint.readLexVarUInt32(buf);
		return BagKey.indexed(index, BagOptionsSerialization.parse(buf));
	}
	
	public static BagKey parseKeyed(ByteBuffer buf) throws AtomLayerException {
		int keyLength = UnsignedLexVarint.readLexVarUInt32(buf);
		int keyStart = buf.position();
		int optionsStart = keyStart + keyLength;
		
		buf.position(optionsStart);
		BagOptions options = BagOptionsSerialization.parse(buf);
		
		buf.position(keyStart);
		buf.limit(optionsStart);
		return BagKey.named(buf, options);
	}

	public static int compare(BagKey.Indexed field, ByteBuffer buf) throws AtomLayerException {
		int bufIndex = UnsignedLexVarint.readLexVarUInt32(buf);
		if (bufIndex < field.index) {
			return 1;
		} else if (bufIndex == field.index) {
			return BagOptionsSerialization.compare(field.options, buf);
		} else {
			return -1;
		}
	}

	public static int compare(BagKey.Keyed field, ByteBuffer buf) throws AtomLayerException {
		int keyLength = UnsignedLexVarint.readLexVarUInt32(buf);
		int limit = buf.limit();
		buf.limit(buf.position() + keyLength);

		int keyComparison = Lexicographic.compare(field.key, buf);
		if (keyComparison != 0) {
			return keyComparison;
		} else {
			buf.position(buf.limit());
			buf.limit(limit);
			return BagOptionsSerialization.compare(field.options, buf);
		}
	}

}
