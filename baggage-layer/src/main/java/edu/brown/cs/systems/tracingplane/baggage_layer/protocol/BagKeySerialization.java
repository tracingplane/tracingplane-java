package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

import java.nio.ByteBuffer;
import edu.brown.cs.systems.tracingplane.atom_layer.types.AtomLayerException;
import edu.brown.cs.systems.tracingplane.atom_layer.types.ByteBuffers;
import edu.brown.cs.systems.tracingplane.atom_layer.types.Lexicographic;
import edu.brown.cs.systems.tracingplane.atom_layer.types.UnsignedLexVarint;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagOptions;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.HeaderPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.IndexedHeaderPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.KeyedHeaderPrefix;

// TODO: test for comparison and write
public class BagKeySerialization {

    private BagKeySerialization() {}

    public static void write(int level, BagKey.Indexed field, ByteBuffer buf) {
        buf.put(IndexedHeaderPrefix.prefixFor(level).prefix);
        UnsignedLexVarint.writeLexVarUInt32(buf, field.index);
        BagOptionsSerialization.write(field.options, buf);
    }

    public static void write(int level, BagKey.Keyed field, ByteBuffer buf) {
        buf.put(KeyedHeaderPrefix.prefixFor(level).prefix);
        UnsignedLexVarint.writeLexVarUInt32(buf, field.key.remaining());
        ByteBuffers.copyTo(field.key, buf);
        BagOptionsSerialization.write(field.options, buf);
    }

    public static int serializedSize(int index, BagOptions options) {
        return UnsignedLexVarint.encodedLength(index) + BagOptionsSerialization.serializedSize(options);
    }

    public static int serializedSize(ByteBuffer key, BagOptions options) {
        return UnsignedLexVarint.encodedLength(key.remaining()) + key.remaining() +
               BagOptionsSerialization.serializedSize(options);
    }

    public static ByteBuffer serialize(int childIndex, BagOptions childOptions) {
        ByteBuffer buf = ByteBuffer.allocate(serializedSize(childIndex, childOptions));
        UnsignedLexVarint.writeLexVarUInt32(buf, childIndex);
        BagOptionsSerialization.write(childOptions, buf);
        buf.limit(buf.position());
        buf.position(0);
        return buf;
    }

    public static ByteBuffer serialize(ByteBuffer childKey, BagOptions childOptions) {
        ByteBuffer buf = ByteBuffer.allocate(serializedSize(childKey, childOptions));
        ByteBuffers.copyTo(childKey, buf);
        BagOptionsSerialization.write(childOptions, buf);
        buf.limit(buf.position());
        buf.position(0);
        return buf;
    }

    public static BagKey parse(HeaderPrefix prefix, ByteBuffer buf) throws AtomLayerException {
        buf.mark();
        try {
            switch (prefix.headerType) {
            case Indexed:
                return parseIndexed(buf);
            case Keyed:
                return parseKeyed(buf);
            default:
                throw new AtomLayerException("Unknown header type " + prefix.headerType);
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
