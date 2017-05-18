package brown.tracingplane.impl;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import brown.tracingplane.BaggageContext;
import brown.tracingplane.BaggageListener;
import brown.tracingplane.BaggageListener.BranchListener;
import brown.tracingplane.BaggageListener.JoinListener;
import brown.tracingplane.BaggageProvider;
import brown.tracingplane.atomlayer.AtomLayerSerialization;
import brown.tracingplane.baggageprotocol.BagKey;
import brown.tracingplane.baggageprotocol.BaggageReader;
import brown.tracingplane.baggageprotocol.BaggageWriter;
import brown.tracingplane.bdl.BDLUtils;
import brown.tracingplane.bdl.Bag;

/**
 * <p>
 * {@link BDLContextProvider} is the primary {@link BaggageProvider} for the tracing plane. It provides a
 * {@link BaggageContext} implementation that is used by BDL-generated classes.
 * </p>
 * 
 * <p>
 * {@link BDLContextProvider} has several static API methods for getting and setting objects within
 * {@link BaggageContext} instances. In general, you should not need to use these methods directly; instead, they are
 * used by the BDL-generated accessors.
 * </p>
 */
public class BDLContextProvider implements BaggageProvider<BDLContext> {

    static final Logger log = LoggerFactory.getLogger(BDLContextProvider.class);

    /**
     * The mapping of baggage handlers to bag numbers. This is intended to be a global singleton
     */
    private BaggageHandlerRegistry registry;

    BDLContextProvider() {
        this(BaggageHandlerRegistry.instance);
    }

    BDLContextProvider(BaggageHandlerRegistry registry) {
        this.registry = registry;
    }

    @Override
    public boolean isValid(BaggageContext baggage) {
        return baggage == null || baggage instanceof BDLContext;
    }

    @Override
    public BDLContext newInstance() {
        return null;
    }

    @Override
    public void discard(BDLContext baggage) {}

    /**
     * Implementation of branch, possibly wrapped with {@link BaggageListener} instances
     */
    Function<BDLContext, BDLContext> branchFunction = ctx -> ctx == null ? null : ctx.branch();

    @Override
    public BDLContext branch(BDLContext from) {
        return branchFunction.apply(from);
    }

    /**
     * Registers a {@link BranchListener} that will be invoked any time {@link BDLContextProvider#branch(BDLContext)} is
     * invoked.
     * 
     * @param listener a {@link BranchListener}
     */
    public void addListener(final BranchListener<BDLContext> listener) {
        final Function<BDLContext, BDLContext> wrappedBranchFunction = branchFunction;
        branchFunction = ctx -> listener.branch(ctx, wrappedBranchFunction);
    }

    /**
     * Implementation of join, possibly wrapped with {@link BaggageListener} instances
     */
    BiFunction<BDLContext, BDLContext, BDLContext> joinFunction = (l, r) -> l == null ? r : l.mergeWith(r);

    @Override
    public BDLContext join(BDLContext left, BDLContext right) {
        return joinFunction.apply(left, right);
    }

    /**
     * Additional operation provided by {@link BDLContext} to compact a context. This is a prototype, its behavior is
     * datatype-dependent and generally should not be used. Its implementation is kinda hacky since I'm not sure if this
     * should be incorporated into the main API yet. Currently only {@link CounterImpl} actually implements compaction.
     */
    public BDLContext compact(BDLContext left, BDLContext right) {
        BDLUtils.is_compaction.set(true);
        try {
            return joinFunction.apply(left, right);
        } finally {
            BDLUtils.is_compaction.set(false);
        }
    }

    /**
     * Registers a {@link JoinListener} that will be invoked any time
     * {@link BDLContextProvider#join(BDLContext, BDLContext)} is invoked.
     * 
     * @param listener a {@link JoinListener}
     */
    public void addListener(final JoinListener<BDLContext> listener) {
        final BiFunction<BDLContext, BDLContext, BDLContext> wrappedJoinFunction = joinFunction;
        joinFunction = (l, r) -> listener.join(l, r, wrappedJoinFunction);
    }

    @Override
    public BDLContext deserialize(byte[] serialized, int offset, int length) {
        List<ByteBuffer> atoms = AtomLayerSerialization.deserialize(serialized, offset, length);
        BaggageReader reader = BaggageReader.create(atoms);
        return BDLContext.parseFrom(registry, reader);
    }

    @Override
    public BDLContext deserialize(ByteBuffer buf) {
        List<ByteBuffer> atoms = AtomLayerSerialization.deserialize(buf);
        BaggageReader reader = BaggageReader.create(atoms);
        return BDLContext.parseFrom(registry, reader);
    }

    @Override
    public byte[] serialize(BDLContext baggage) {
        if (baggage == null) return null;
        BaggageWriter writer = baggage.serialize();
        return AtomLayerSerialization.serialize(writer.atoms());
    }

    @Override
    public byte[] serialize(BDLContext baggage, int maximumSerializedSize) {
        if (baggage == null) return null;
        BaggageWriter writer = baggage.serialize();
        return AtomLayerSerialization.serialize(writer.atoms(), maximumSerializedSize);
    }

    /**
     * <p>
     * This method fetches the object stored in the provided {@link BaggageContext} under the specified key, or returns
     * null if there is no object present.
     * </p>
     * 
     * <p>
     * This method is typically invoked by BDL-generated classes when calling the <code>getFrom(BaggageContext)</code>
     * method. For example, if we generated a bag called <code>XTraceBaggage</code> and registered it to
     * <code>BagKey.indexed(7)</code>, then a call to <code>get(BaggageContext, BagKey.indexed(7))</code> would return
     * either null (if there is no <code>XTraceBaggage</code> instance present), or an instance of
     * <code>XTraceBaggage</code>.
     * </p>
     * 
     * @param baggage a {@link BaggageContext} instance, expected to be an instance of {@link BDLContext}
     * @param key the key to look up
     * @return if <code>baggage</code> is an instance of {@link BDLContext}, returns the object mapped to
     *         <code>key</code> if there is one. Otherwise, returns null.
     */
    public static Bag get(BaggageContext baggage, BagKey key) {
        if (!(baggage instanceof BDLContext)) {
            return null;
        }
        return ((BDLContext) baggage).get(key);
    }

    /**
     * <p>
     * Maps the specified key to the provided bag value. This method possibly modifies the provided <code>baggage</code>
     * instance, or might return a new instance depending on whether <code>baggage</code> is modifiable.
     * </p>
     * 
     * <p>
     * To construct a new {@link BaggageContext} with the specified mapping, simply pass <code>null</code> for
     * <code>baggage</code>.
     * </p>
     * 
     * @param baggage the baggage to modify, which may be null, indicating the empty baggage.
     * @param key the key to update
     * @param bag the new bag to map to this key
     * @return a possibly new baggage instance with the updated mapping.
     */
    public static BaggageContext set(BaggageContext baggage, BagKey key, Bag value) {
        if (key != null) {
            BDLContext contents;
            if (baggage instanceof BDLContext) {
                contents = (BDLContext) baggage;
            } else {
                contents = new BDLContext();
            }
            contents.put(key, value);
            return contents;
        } else {
            return baggage;
        }
    }

}
