package edu.brown.cs.systems.tracingplane.transit_layer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A TransitLayer provides implementations of methods that are called by users from the static methods in
 * {@link Baggage}.
 * 
 * @param <B> Some implementation of {@link Baggage} used by this transit layer.
 */
public interface TransitLayer<B extends Baggage> {

    /**
     * @param baggage a baggage instance
     * @return true if <code>baggage</code> is an instance of {@link B} or if <code>baggage</code> is null. This method
     *         only returns false if <code>baggage</code> is an instance of baggage but not an instance of {@link B}.
     */
    public boolean isInstance(Baggage baggage);

    /**
     * @return a new instance of {@link B}, which may be null to indicate an empty baggage
     */
    public B newInstance();

    /**
     * Discards <code>baggage</code>. If this TransitLayer performs anything like reference counting, then this method
     * is where decrementing reference counts would happen.
     * 
     * @param baggage a baggage instance
     */
    public void discard(B baggage);

    /**
     * Create and return a copy of <code>from</code>. If this TransitLayer performs anything like reference counting,
     * then this method might just return <code>from</code> while incrementing reference counts.
     * 
     * This method might create a new instance of {@link B} and might even modify the contents of <code>from</code>
     * 
     * @param from a baggage instance, possibly null
     * @return a baggage instance branched from <code>from</code>, possibly null
     */
    public B branch(B from);

    /**
     * Merge the contents of <code>left</code> and <code>right</code> and return as baggage. <code>left</code> and
     * <code>right</code> should be treated as discarded (or reused) after this method call.
     * 
     * @param left a baggage instance, possibly null
     * @param right a baggage instance, possibly null
     * @return a baggage instance with merged contents from <code>left</code> and <code>right</code>
     */
    public B join(B left, B right);

    /**
     * Deserialize the provided serialized baggage representation.
     * 
     * @param serialized a serialized baggage
     * @param offset offset into the array where the baggage bytes begin
     * @param length lenft of the baggage bytes
     * @return a deserialized baggage instance, possibly null
     */
    public B deserialize(byte[] serialized, int offset, int length);

    /**
     * Reads a serialized baggage instance from the provided input stream. The serialized baggage instance should be
     * length prefixed.
     * 
     * @param in the input stream to read from
     * @return a deserialized baggage instance, possibly null
     * @throws IOException propagated if any occurs while reading from <code>in</code>
     */
    public B readFrom(InputStream in) throws IOException;

    /**
     * Serialize the provided baggage instance to its byte representation. This method should behave as though
     * {@link #branch(Baggage)} is also being called prior to serialization.
     * 
     * @param baggage a baggage instance to serialize, possibly null
     * @return the serialized representation of <code>baggage</code>, which might be null or an empty byte array
     */
    public byte[] serialize(B baggage);

    /**
     * Serialize the provided baggage instance to its byte representation and trim the baggage so that its serialized
     * size is {@code <= maximumSerializedSize}. This method should behave as though {@link #branch(Baggage)} is also being
     * called prior to serialization.
     * 
     * @param baggage a baggage instance to serialize, possibly null
     * @param maximumSerializedSize the maximum size in bytes of the serialized baggage.
     * @return the serialized representation of <code>baggage</code>, which might be null or an empty byte array
     */
    public byte[] serialize(B baggage, int maximumSerializedSize);

    /**
     * Serializes <code>baggage</code> and writes it length-prefixed to <code>out</code>.
     * 
     * @param out the output stream to write to
     * @param baggage a baggage instance to serialize, possibly null
     * @throws IOException propagated if any occurs while writing to <code>out</code>
     */
    public void writeTo(OutputStream out, B baggage) throws IOException;

    /**
     * Serializes <code>baggage</code> and writes it length-prefixed to <code>out</code>. Also trims the baggage so that
     * its serialized size is {@code <= maximumSerializedSize}
     * 
     * @param out the output stream to write to
     * @param baggage a baggage instance to serialize, possibly null
     * @param maximumSerializedSize the maximum size in bytes of the serialized baggage.
     * @throws IOException propagated if any occurs while writing to <code>out</code>
     */
    public void writeTo(OutputStream out, B baggage, int maximumSerializedSize) throws IOException;

}
