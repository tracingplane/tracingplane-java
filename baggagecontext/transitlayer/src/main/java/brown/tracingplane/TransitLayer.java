package brown.tracingplane;

import java.nio.ByteBuffer;

/**
 * <p>
 * Maintains {@link BaggageContext} instances for requests. Most {@link TransitLayer} implementations will store
 * {@link BaggageContext} instances using thread-local storage; this interface primarily exists to support wrappers to
 * other implementations (e.g., OpenTracing).
 * </p>
 * 
 * <p>
 * The static methods in the {@link ActiveBaggage} interface proxy to a {@link TransitLayer} implementation. Typically
 * this implementation will be {@link ThreadLocalTransitLayer}, which maintains an active {@link BaggageContext}
 * instance using thread-local storage.
 * </p>
 * 
 * <p>
 * In general, {@link TransitLayer} implementations are responsible for maintaining {@link BaggageContext} instances
 * while executions run. They use the notion of an "active" context which represents the {@link BaggageContext} for the
 * current execution. Implicitly, if the current execution has no {@link BaggageContext}, it is the same as having an
 * empty context.
 * </p>
 * 
 * <p>
 * A {@link TransitLayer} implementation typically maintains the active {@link BaggageContext} using thread-local
 * storage. The purpose of this interface is to enable other implementations to plug in to existing instrumentation.
 * </p>
 * 
 * <p>
 * {@link TransitLayer} also provides methods that mirror those of {@link BaggageProvider}, such as {@link #branch()}
 * which corresponds to {@link BaggageProvider#branch(BaggageContext)}. These methods simply proxy the
 * {@link BaggageProvider} method, passing the currently active {@link BaggageContext}.
 * </p>
 * 
 * <p>
 * All {@link TransitLayer} implementations must have a one-argument constructor that receives the
 * {@link BaggageProvider} to use.
 * </p>
 */
public interface TransitLayer {

    /**
     * Discard the currently active {@link BaggageContext}.
     */
    public void discard();

    /**
     * Create and return a branched copy of the currently active baggage context. Typically, this will just duplicate
     * the active context or increment a reference count. Sometimes it will create a new instance, or even modify the
     * contents of the branched context.
     * 
     * @return a baggage instance branched from the currently active context, possibly null
     */
    public BaggageContext branch();

    /**
     * Create and return a branched, serialized copy of the currently active baggage context. Typically, this will just
     * duplicate the active context or increment a reference count. Sometimes it will create a new instance, or even
     * modify the contents of the branched context.
     * 
     * @return a baggage instance branched from the currently active context and serialized, possibly null
     */
    public byte[] branchBytes();

    /**
     * Merges the contents of <code>otherContext</code> into the currently active context. <code>otherContext</code>
     * should not be reused after calling this method, and should be treated as discarded.
     * 
     * @param otherContext another baggage context, possibly null
     */
    public void join(BaggageContext otherContext);

    /**
     * Deserializes the provided context and merges it into the currently active context.
     * 
     * @param serializedContext a serialized baggage context, possibly null
     */
    public void join(ByteBuffer serializedContext);

    /**
     * Deserializes the provided context and merges it into the currently active context.
     * 
     * @param serializedContext a serialized baggage context, possibly null
     */
    public void join(byte[] serialized, int offset, int length);

    /**
     * Discards the currently active {@link BaggageContext}, then activates the provided <code>baggage</code>. If
     * <code>baggage</code> is just a modified version of the currently active BaggageContext, then it is better to use
     * the {@link #update(BaggageContext)} method instead.
     * 
     * @param baggage The new baggage context to activate.
     */
    public void set(BaggageContext baggage);

    /**
     * Deserializes the provided context, discards any currently active context, and replaces it with the deserialized
     * context.
     * 
     * @param serializedContext a serialized baggage context, possibly null
     */
    public void set(ByteBuffer serializedContext);

    /**
     * Deserializes the provided context and merges it into the currently active context.
     * 
     * @param serialized a serialized baggage context, possibly null
     * @param offset offset into the byte array
     * @param length length of serialized bytes
     */
    public void set(byte[] serialized, int offset, int length);

    /**
     * Gets and removes the currently active {@link BaggageContext}. After calling this method, there will be no active
     * {@link BaggageContext}.
     * 
     * @return the current {@link BaggageContext}.
     */
    public BaggageContext take();

    /**
     * Gets, removes, and serializes the currently active {@link BaggageContext}. After calling this method, there will
     * be no active {@link BaggageContext}.
     * 
     * @return the current {@link BaggageContext}.
     */
    public byte[] takeBytes();

    /**
     * Gets the currently active {@link BaggageContext}. The {@link BaggageContext} instance remains active after
     * calling this method. Use {@link #take()} to if you wish to get and remove the currently active context.
     * 
     * @return the active {@link BaggageContext}
     */
    public BaggageContext peek();

    /**
     * Sets the currently active {@link BaggageContext}. A call to this method implies that the provided
     * <code>context</code> argument is an updated version of the active context. Conversely, if you intend to replace
     * the currently active context (e.g., because a different execution is beginning), use the
     * {@link #set(BaggageContext)} method.
     * 
     * @param context an updated version of the currently active baggage context.
     */
    public void update(BaggageContext baggage);

}
