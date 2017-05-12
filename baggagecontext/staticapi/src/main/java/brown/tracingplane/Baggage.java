package brown.tracingplane;

import java.nio.ByteBuffer;

/**
 * <p>
 * The static methods in the {@link Baggage} class are the main entry point for manipulating {@link BaggageContext}
 * instances.  The methods here mirror those provided by the {@link BaggageProvider} interface.
 * <p>
 * 
 * <p>
 * Use of the {@link Baggage} API depends on a {@link BaggageProvider} being configured. Typically, this is done
 * automatically by the Tracing Plane distribution that you use. However, if this is not the case, or if you wish to
 * override the {@link BaggageProvider} implementation in use, then set the <code>baggage.provider</code> property to
 * the {@link BaggageProviderFactory} of your choice.
 * </p>
 *
 */
public class Baggage {

    static BaggageProvider<BaggageContext> provider = DefaultBaggageProvider.getWrapped();

    /** Not instantiable */
    private Baggage() {}

    /**
     * @return a new instance of {@link B}, which may be null to indicate an empty baggage
     */
    public static BaggageContext newInstance() {
        return provider.newInstance();
    }

    /**
     * Discards <code>baggage</code>, indicating that the instance will not be used again. This method is useful, but
     * not required. Its purpose is to enable {@link BaggageContext} implementations to do things like reference
     * counting, which is an optimization. Failing to discard baggage instances will have no bad side effects.
     * 
     * @param baggage a baggage context
     */
    public static void discard(BaggageContext baggage) {
        provider.discard(baggage);
    }

    /**
     * Create and return a branched copy <code>from</code>. Typically, this will just duplicate <code>from</code> or
     * increment a reference count. Sometimes it will create a new instance, or even modify the contents of
     * <code>from</code>.
     * 
     * @param from a baggage instance, possibly null
     * @return a baggage instance branched from <code>from</code>, possibly null
     */
    public static BaggageContext branch(BaggageContext from) {
        return provider.branch(from);
    }

    /**
     * Merge the contents of <code>left</code> and <code>right</code> and return as baggage. <code>left</code> and
     * <code>right</code> should be treated as discarded (or reused) after this method call.
     * 
     * @param left a baggage instance, possibly null
     * @param right a baggage instance, possibly null
     * @return a baggage instance with merged contents from <code>left</code> and <code>right</code>
     */
    public static BaggageContext join(BaggageContext left, BaggageContext right) {
        return provider.join(left, right);
    }

    /**
     * Deserialize the provided serialized baggage representation.
     * 
     * @param serialized a serialized baggage
     * @param offset offset into the array where the baggage bytes begin
     * @param length lenft of the baggage bytes
     * @return a deserialized baggage instance, possibly null
     */
    public static BaggageContext deserialize(byte[] serialized, int offset, int length) {
        return provider.deserialize(serialized, offset, length);
    }

    /**
     * Deserialize the provided serialized baggage representation.
     * 
     * @param buf a serialized baggage
     * @return a deserialized baggage instance, possibly null
     */
    public static BaggageContext deserialize(ByteBuffer buf) {
        return provider.deserialize(buf);
    }

    /**
     * Serialize the provided baggage instance to its byte representation. This method should behave as though
     * {@link #branch(Baggage)} is also being called prior to serialization.
     * 
     * @param baggage a baggage instance to serialize, possibly null
     * @return the serialized representation of <code>baggage</code>, which might be null or an empty byte array
     */
    public static byte[] serialize(BaggageContext baggage) {
        return provider.serialize(baggage);
    }

    /**
     * Serialize the provided baggage instance to its byte representation and trim the baggage so that its serialized
     * size is {@code <= maximumSerializedSize}. This method should behave as though {@link #branch(Baggage)} is also
     * being called prior to serialization.
     * 
     * @param baggage a baggage instance to serialize, possibly null
     * @param maximumSerializedSize the maximum size in bytes of the serialized baggage.
     * @return the serialized representation of <code>baggage</code>, which might be null or an empty byte array
     */
    public static byte[] serialize(BaggageContext baggage, int maximumSerializedSize) {
        return provider.serialize(baggage, maximumSerializedSize);
    }

}
