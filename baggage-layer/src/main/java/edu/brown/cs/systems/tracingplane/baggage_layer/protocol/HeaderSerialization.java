package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

import java.nio.ByteBuffer;
import edu.brown.cs.systems.tracingplane.atom_layer.types.AtomLayerException;
import edu.brown.cs.systems.tracingplane.atom_layer.types.ByteBuffers;
import edu.brown.cs.systems.tracingplane.atom_layer.types.UnsignedLexVarint;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.baggage_layer.BaggageLayerException;
import edu.brown.cs.systems.tracingplane.baggage_layer.BaggageLayerException.BaggageLayerRuntimeException;
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

    /** Atom prefixes are 1 byte long */
    public static final int PREFIX_SIZE = 1;

    /**
     * Calculates the serialized size of an Indexed header atom including its prefix
     * 
     * @param bagKey an Indexed bagkey
     * @return the serialized size of the atom for this bagkey
     */
    public static int serializedSize(BagKey.Indexed bagKey) {
        return PREFIX_SIZE + UnsignedLexVarint.encodedLength(bagKey.index);
    }

    /**
     * Calculates the serialized size of a Keyed header atom including its prefix
     * 
     * @param bagKey a Keyed header bagKey
     * @return the serialized size of the atom for this bagKey
     */
    public static int serializedSize(BagKey.Keyed bagKey) {
        return PREFIX_SIZE + bagKey.key.remaining();
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
     * @param bagKey the key of this header
     * @param level the header level
     */
    public static void writeAtomPrefix(ByteBuffer buf, BagKey.Indexed bagKey, int level) {
        buf.put(IndexedHeaderPrefix.prefixFor(level, bagKey.options).prefix);
    }

    /**
     * Write the prefix byte of a Keyed header atom to the provided buf
     * 
     * @param buf the buffer to write to
     * @param bagKey the key of this header
     * @param level the header level
     */
    public static void writeAtomPrefix(ByteBuffer buf, BagKey.Keyed bagKey, int level) {
        buf.put(KeyedHeaderPrefix.prefixFor(level, bagKey.options).prefix);
    }

    /**
     * Write the payload of an Indexed header atom to the provided buf
     * 
     * @param buf the buffer to write to
     * @param bagKey the key of this header
     * @param bagKey the bagKey to write
     */
    public static void writeAtomPayload(ByteBuffer buf, BagKey.Indexed bagKey) {
        UnsignedLexVarint.writeLexVarUInt32(buf, bagKey.index);
    }

    /**
     * Write the payload of an Keyed header atom to the provided buf
     * 
     * @param buf the buffer to write to
     * @param bagKey the bagKey to write
     */
    public static void writeAtomPayload(ByteBuffer buf, BagKey.Keyed bagKey) {
        ByteBuffers.copyTo(bagKey.key, buf);
    }

    /**
     * Serialize the header atom, including prefix byte, for the provided bagKey and level
     * 
     * @param bagKey the key to write a header for
     * @param level the level of this key
     * @return the header atom representing the provided key and level
     */
    public static ByteBuffer serialize(BagKey bagKey, int level) {
        if (bagKey instanceof BagKey.Indexed) {
            return serialize((BagKey.Indexed) bagKey, level);
        } else if (bagKey instanceof BagKey.Keyed) {
            return serialize((BagKey.Keyed) bagKey, level);
        } else {
            throw new BaggageLayerRuntimeException("Cannot serialize unsupported bagKey " + bagKey);
        }
    }

    /**
     * Serialize the header atom, including prefix byte, for the provided bagKey and level
     * 
     * @param bagKey the key to write a header for
     * @param level the level of this key
     * @return the header atom representing the provided key and level
     */
    public static ByteBuffer serialize(BagKey.Indexed bagKey, int level) {
        ByteBuffer buf = ByteBuffer.allocate(serializedSize(bagKey));
        writeAtomPrefix(buf, bagKey, level);
        writeAtomPayload(buf, bagKey);
        buf.flip();
        return buf;
    }

    /**
     * Serialize the header atom, including prefix byte, for the provided bagKey and level
     * 
     * @param bagKey the key to write a header for
     * @param level the level of this key
     * @return the header atom representing the provided key and level
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
     * 
     * @param bagKey the key to write a header for
     * @return a buffer containing the payload of the header atom
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
     * 
     * @param bagKey the key to write a header for
     * @return a buffer containing the payload of the header atom
     */
    public static ByteBuffer serializePayload(BagKey.Keyed bagKey) {
        ByteBuffer buf = serialize(bagKey, 0);
        buf.position(1);
        return buf;
    }

    /**
     * Parses a bag key from the provided atom
     * 
     * @param atom a buffer containing both the prefix of the atom and its payload
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
     * @throws BaggageLayerException if the atom is not a header atom or if the header payload could not be parsed
     */
    public static BagKey parse(HeaderPrefix prefix, ByteBuffer buf) throws BaggageLayerException {
        buf.mark();
        try {
            switch (prefix.headerType) {
            case Indexed:
                return parseIndexedHeaderPayload(prefix, buf);
            case Keyed:
                return parseKeyedHeaderPayload(prefix, buf);
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
     * @param prefix the prefix of this atom
     * @param buf the payload of the atom
     * @return the parsed bagkey
     * @throws BaggageLayerException if the atom payload could not be parsed
     */
    public static BagKey parseIndexedHeaderPayload(HeaderPrefix prefix, ByteBuffer buf) throws BaggageLayerException {
        try {
            return BagKey.indexed(UnsignedLexVarint.readLexVarUInt32(buf), prefix.options());
        } catch (AtomLayerException e) {
            throw new BaggageLayerException("Unable to parse IndexedHeader payload", e);
        }
    }

    /**
     * Parse the payload of a keyed header
     * 
     * @param prefix the prefix of this atom
     * @param buf the payload of the atom
     * @return the parsed bagkey
     * @throws BaggageLayerException if the atom payload could not be parsed
     */
    public static BagKey parseKeyedHeaderPayload(HeaderPrefix prefix, ByteBuffer buf) throws BaggageLayerException {
        return BagKey.keyed(buf.slice(), prefix.options());
    }

}
