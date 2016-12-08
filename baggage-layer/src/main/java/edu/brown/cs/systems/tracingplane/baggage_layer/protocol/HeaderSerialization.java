package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

import java.nio.ByteBuffer;
import edu.brown.cs.systems.tracingplane.atom_layer.types.AtomLayerException;
import edu.brown.cs.systems.tracingplane.atom_layer.types.ByteBuffers;
import edu.brown.cs.systems.tracingplane.atom_layer.types.UnsignedLexVarint;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagOptions;
import edu.brown.cs.systems.tracingplane.baggage_layer.BaggageLayerException;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.AtomPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.HeaderPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.IndexedHeaderPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.KeyedHeaderPrefix;

/**
 * Contains methods to serialize and deserialize header atoms (eg, {@link IndexedHeaderPrefix} and
 * {@link KeyedHeaderPrefix} atoms)
 */
public class HeaderSerialization {

    private HeaderSerialization() {}

    public static final int PREFIX_SIZE = 1;

    /**
     * Calculates the serialized size of an Indexed header atom including its prefix
     * 
     * @param bagKey an Indexed bagkey
     * @return the serialized size of the atom for this bagkey
     */
    public static int serializedSize(BagKey.Indexed bagKey) {
        return PREFIX_SIZE + UnsignedLexVarint.encodedLength(bagKey.index) + serializedSize(bagKey.options);
    }

    /**
     * Calculates the serialized size of a Keyed header atom including its prefix
     * 
     * @param bagKey a Keyed header bagKey
     * @return the serialized size of the atom for this bagKey
     */
    public static int serializedSize(BagKey.Keyed bagKey) {
        return PREFIX_SIZE + UnsignedLexVarint.encodedLength(bagKey.key.remaining()) + bagKey.key.remaining() +
               serializedSize(bagKey.options);
    }

    /**
     * Calculates the serialized size of bag options. If the bag options are the default options, they are omitted from
     * serialization
     */
    public static int serializedSize(BagOptions options) {
        if (options.isDefault()) {
            return 0;
        } else {
            return 1;
        }
    }

    /**
     * Write an atom to the provided buffer for the specified bagKey and level. This will write both the atom prefix and
     * its payload
     * 
     * @param buf the buffer to write to
     * @param bagKey the bagKey to write
     * @param level the level of the bagKey
     */
    public static void writeAtom(ByteBuffer buf, BagKey.Indexed bagKey, int level) {
        writeAtomPrefix(buf, bagKey, level);
        writeAtomPayload(buf, bagKey);
    }

    /**
     * Write an atom to the provided buffer for the specified bagKey and level. This will write both the atom prefix and
     * its payload
     * 
     * @param buf the buffer to write to
     * @param bagKey the bagKey to write
     * @param level the level of the bagKey
     */
    public static void writeAtom(ByteBuffer buf, BagKey.Keyed bagKey, int level) {
        writeAtomPrefix(buf, bagKey, level);
        writeAtomPayload(buf, bagKey);
    }

    /**
     * Write the prefix byte of an Indexed header atom to the provided buf
     * 
     * @param buf the buffer to write to
     * @param level the header level
     */
    public static void writeAtomPrefix(ByteBuffer buf, BagKey.Indexed bagKey, int level) {
        buf.put(IndexedHeaderPrefix.prefixFor(level).prefix);
    }

    /**
     * Write the prefix byte of a Keyed header atom to the provided buf
     * 
     * @param buf the buffer to write to
     * @param level the header level
     */
    public static void writeAtomPrefix(ByteBuffer buf, BagKey.Keyed bagKey, int level) {
        buf.put(KeyedHeaderPrefix.prefixFor(level).prefix);
    }

    /**
     * Write the payload of an Indexed header atom to the provided buf
     * 
     * @param buf the buffer to write to
     * @param bagKey the bagKey to write
     */
    public static void writeAtomPayload(ByteBuffer buf, BagKey.Indexed bagKey) {
        UnsignedLexVarint.writeLexVarUInt32(buf, bagKey.index);
        if (bagKey.options != BagOptions.defaultOptions) {
            writeBagOptions(buf, bagKey.options);
        }
    }

    /**
     * Write the payload of an Keyed header atom to the provided buf
     * 
     * @param buf the buffer to write to
     * @param bagKey the bagKey to write
     */
    public static void writeAtomPayload(ByteBuffer buf, BagKey.Keyed bagKey) {
        ByteBuffers.copyTo(bagKey.key, buf);
        writeBagOptions(buf, bagKey.options);
    }

    /**
     * Write the bag options to the provided buf. If the options are just the default options, this will not write
     * anything.
     * 
     * @param buf the buffer to write to
     * @param options the bag options
     */
    public static void writeBagOptions(ByteBuffer buf, BagOptions options) {
        if (options == null) {
            options = BagOptions.defaultOptions;
        }
        buf.put(options.byteValue);
    }

    /**
     * Serialize the full atom for the provided bag at the specified level
     */
    public static ByteBuffer serialize(BagKey.Indexed bagKey, int level) {
        ByteBuffer buf = ByteBuffer.allocate(serializedSize(bagKey));
        writeAtomPrefix(buf, bagKey, level);
        writeAtomPayload(buf, bagKey);
        buf.flip();
        return buf;
    }

    /**
     * Serialize the full atom for the provided bag at the specified level
     */
    public static ByteBuffer serialize(BagKey.Keyed bagKey, int level) {
        ByteBuffer buf = ByteBuffer.allocate(serializedSize(bagKey));
        writeAtomPrefix(buf, bagKey, level);
        writeAtomPayload(buf, bagKey);
        buf.flip();
        return buf;
    }

    /**
     * Serialize the atom payload for the provided bagkey, excluding atom prefix. The returned buffer will have
     * allocated room for a prefix byte, but not filled it. The position of the returned buffer will be the start of the
     * payload.
     */
    public static ByteBuffer serializePayload(BagKey.Indexed bagKey) {
        ByteBuffer buf = serialize(bagKey, 0);
        buf.position(1);
        return buf;
    }

    /**
     * Serialize the atom payload for the provided bagkey, excluding atom prefix. The returned buffer will have
     * allocated room for a prefix byte, but not filled it. The position of the returned buffer will be the start of the
     * payload.
     */
    public static ByteBuffer serializePayload(BagKey.Keyed bagKey) {
        ByteBuffer buf = serialize(bagKey, 0);
        buf.position(1);
        return buf;
    }

    /**
     * Parses a bag key from the provided atom
     * 
     * @param buf a buffer containing both the prefix of the atom and its payload
     * @return a bag key
     * @throws AtomLayerException if the header payload could not be parsed
     * @throws BaggageLayerException if the atom is not a header atom
     */
    public static BagKey parse(ByteBuffer atom) throws AtomLayerException, BaggageLayerException {
        if (atom.remaining() == 0) {
            throw new BaggageLayerException("Unable to parse zero-length atom " + atom);
        }
        byte firstByte = atom.get();
        AtomPrefix prefix = AtomPrefixes.get(firstByte);
        if (prefix instanceof HeaderPrefix) {
            return parse((HeaderPrefix) prefix, atom);
        } else {
            throw new BaggageLayerException("Invalid prefix for header " + prefix);
        }
    }

    /**
     * Parses a bag key
     * 
     * @param prefix the prefix of the atom
     * @param buf the payload of the atom
     * @return a bag key
     * @throws AtomLayerException if the atom is not a header atom or if the header payload could not be parsed
     */
    public static BagKey parse(HeaderPrefix prefix, ByteBuffer buf) throws BaggageLayerException {
        buf.mark();
        try {
            switch (prefix.headerType) {
            case Indexed:
                return parseIndexedHeaderPayload(buf);
            case Keyed:
                return parseKeyedHeaderPayload(buf);
            default:
                throw new BaggageLayerException("Unknown header type " + prefix.headerType);
            }
        } finally {
            buf.reset();
        }
    }

    /**
     * Parse the payload of an indexed header
     * 
     * @param buf the payload of the atom
     * @return the parsed bagkey
     * @throws BaggageLayerException if the atom payload could not be parsed
     */
    public static BagKey parseIndexedHeaderPayload(ByteBuffer buf) throws BaggageLayerException {
        try {
            int index = UnsignedLexVarint.readLexVarUInt32(buf);
            if (buf.hasRemaining()) {
                return BagKey.indexed(index, parseBagOptions(buf));
            } else {
                return BagKey.indexed(index);
            }
        } catch (AtomLayerException e) {
            throw new BaggageLayerException("Unable to parse IndexedHeader payload", e);
        }
    }

    /**
     * Parse the payload of a keyed header
     * 
     * @param buf the payload of the atom
     * @return the parsed bagkey
     * @throws BaggageLayerException if the atom payload could not be parsed
     */
    public static BagKey parseKeyedHeaderPayload(ByteBuffer buf) throws BaggageLayerException {
        int keySize = buf.remaining() - 1;
        ByteBuffer key = buf.slice();
        key.limit(keySize);
        buf.position(buf.position() + keySize);
        BagOptions options = parseBagOptions(buf);
        return BagKey.named(key, options);
    }

    /**
     * Parse bag options. If the options are invalid, throws a BaggageLayerException
     */
    public static BagOptions parseBagOptions(ByteBuffer buf) throws BaggageLayerException {
        byte optionsValue = buf.get();
        BagOptions options = BagOptions.valueOf(optionsValue);
        if (options == null) {
            throw new BaggageLayerException(String.format("Got invalid bag options %d", optionsValue));
        }
        return options;
    }

}
