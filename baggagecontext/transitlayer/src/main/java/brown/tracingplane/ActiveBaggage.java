package brown.tracingplane;

import java.nio.ByteBuffer;

/**
 * <p>
 * {@link ActiveBaggage} provides static methods that mirror the methods implemented by {@link BaggageProvider} and
 * {@link TransitLayer}. Unlike the {@link brown.tracingplane.Baggage} interface, {@link ActiveBaggage} implicitly
 * accesses the currently-active {@link BaggageContext} that is being managed by the {@link TransitLayer}. Unless it has
 * been configured otherwise, this entails looking up the {@link BaggageContext} in thread-local storage.
 * </p>
 * 
 * <p>
 * This class also provides static methods to get and set the currently active baggage. These methods proxy to the
 * configured {@link TransitLayer}, which is responsible for maintaining active baggage (e.g., in thread-local storage).
 * </p>
 * 
 * <p>
 * If you wish to manipulate {@link BaggageContext} instances without affecting the currently active baggage context,
 * use the static methods on the {@link brown.tracingplane.Baggage} class.
 * </p>
 * 
 * <p>
 * Using this class requires that a {@link BaggageProvider} has been registered (e.g., using the
 * <code>baggage.provider</code> property). By default, the {@link TransitLayer} used will be
 * {@link ThreadLocalTransitLayer}; this can be overridden using <code>baggage.transit</code>.
 * </p>
 *
 */
public class ActiveBaggage {

    static TransitLayer transit = DefaultTransitLayer.get();

    /** Not instantiable */
    private ActiveBaggage() {}

    /**
     * Discard the currently active {@link BaggageContext}.
     */
    public static void discard() {
        transit.discard();
    }

    /**
     * Create and return a branched copy of the currently active baggage context. Typically, this will just duplicate
     * the active context or increment a reference count. Sometimes it will create a new instance, or even modify the
     * contents of the branched context.
     * 
     * @return a baggage instance branched from the currently active context, possibly null
     */
    public static BaggageContext branch() {
        return transit.branch();
    }

    /**
     * Create and return a branched, serialized copy of the currently active baggage context. Typically, this will just
     * duplicate the active context or increment a reference count. Sometimes it will create a new instance, or even
     * modify the contents of the branched context.
     * 
     * @return a baggage instance branched from the currently active context and serialized, possibly null
     */
    public static byte[] branchBytes() {
        return transit.branchBytes();
    }

    /**
     * Merges the contents of <code>otherContext</code> into the currently active context. <code>otherContext</code>
     * should not be reused after calling this method, and should be treated as discarded.
     * 
     * @param otherContext another baggage context, possibly null
     */
    public static void join(BaggageContext otherContext) {
        transit.join(otherContext);
    }

    /**
     * Deserializes the provided context and merges it into the currently active context.
     * 
     * @param serializedContext a serialized baggage context, possibly null
     */
    public static void join(ByteBuffer serializedContext) {
        transit.join(serializedContext);
    }

    /**
     * Deserializes the provided context and merges it into the currently active context.
     * 
     * @param serialized a serialized baggage context, possibly null
     * @param offset offset into byte array
     * @param length length of bytes to use
     */
    public static void join(byte[] serialized, int offset, int length) {
        transit.join(serialized, offset, length);
    }

    /**
     * Discards the currently active {@link BaggageContext}, then activates the provided <code>baggage</code>. If
     * <code>baggage</code> is just a modified version of the currently active BaggageContext, then it is better to use
     * the {@link #update(BaggageContext)} method instead.
     * 
     * @param baggage The new baggage context to activate.
     */
    public static void set(BaggageContext baggage) {
        transit.set(baggage);
    }

    /**
     * Deserializes the provided context, discards any currently active context, and replaces it with the deserialized
     * context.
     * 
     * @param serializedContext a serialized baggage context, possibly null
     */
    public static void set(ByteBuffer serializedContext) {
        transit.set(serializedContext);
    }

    /**
     * Deserializes the provided context and merges it into the currently active context.
     * 
     * @param serialized a serialized baggage context, possibly null
     * @param offset offset into the byte array
     * @param length length of serialized bytes
     */
    public static void set(byte[] serialized, int offset, int length) {
        transit.set(serialized, offset, length);
    }

    /**
     * Gets and removes the currently active {@link BaggageContext}. After calling this method, there will be no active
     * {@link BaggageContext}.
     * 
     * @return the current {@link BaggageContext}.
     */
    public static BaggageContext take() {
        return transit.take();
    }

    /**
     * Gets, removes, and serializes the currently active {@link BaggageContext}. After calling this method, there will
     * be no active {@link BaggageContext}.
     * 
     * @return the current {@link BaggageContext}.
     */
    public static byte[] takeBytes() {
        return transit.takeBytes();
    }

    /**
     * Gets the currently active {@link BaggageContext}. The {@link BaggageContext} instance remains active after
     * calling this method. Use {@link #take()} to if you wish to get and remove the currently active context.
     * 
     * @return the active {@link BaggageContext}
     */
    public static BaggageContext peek() {
        return transit.peek();
    }

    /**
     * Sets the currently active {@link BaggageContext}. A call to this method implies that the provided
     * <code>context</code> argument is an updated version of the active context. Conversely, if you intend to replace
     * the currently active context (e.g., because a different execution is beginning), use the
     * {@link #set(BaggageContext)} method.
     * 
     * @param context an updated version of the currently active baggage context.
     */
    public static void update(BaggageContext baggage) {
        transit.update(baggage);
    }

}
