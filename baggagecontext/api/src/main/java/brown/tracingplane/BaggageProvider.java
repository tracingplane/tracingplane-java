package brown.tracingplane;

import java.nio.ByteBuffer;

/**
 * <p>
 * A {@link BaggageProvider} provides an implementation of {@link BaggageContext} objects. Manipulation of
 * {@link BaggageContext} objects is primarily done through {@link BaggageProvider} methods.
 * </p>
 */
public interface BaggageProvider<B extends BaggageContext> {

    /**
     * @param baggage a BaggageContext to test
     * @return true if <code>baggage</code> is an instance of {@link B} or if baggage is null.
     */
    boolean isValid(BaggageContext baggage);

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
     * Deserialize the provided serialized baggage representation.
     * 
     * @param buf a serialized baggage
     * @return a deserialized baggage instance, possibly null
     */
    public B deserialize(ByteBuffer buf);

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
     * size is {@code <= maximumSerializedSize}. This method should behave as though {@link #branch(Baggage)} is also
     * being called prior to serialization.
     * 
     * @param baggage a baggage instance to serialize, possibly null
     * @param maximumSerializedSize the maximum size in bytes of the serialized baggage.
     * @return the serialized representation of <code>baggage</code>, which might be null or an empty byte array
     */
    public byte[] serialize(B baggage, int maximumSerializedSize);
}
