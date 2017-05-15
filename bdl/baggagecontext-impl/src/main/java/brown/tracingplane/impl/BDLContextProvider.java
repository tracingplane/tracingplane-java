package brown.tracingplane.impl;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import brown.tracingplane.BaggageContext;
import brown.tracingplane.BaggageProvider;
import brown.tracingplane.atomlayer.AtomLayerSerialization;
import brown.tracingplane.baggageprotocol.BagKey;
import brown.tracingplane.baggageprotocol.BaggageReader;
import brown.tracingplane.baggageprotocol.BaggageWriter;
import brown.tracingplane.bdl.BaggageHandler;
import brown.tracingplane.impl.BaggageListener.BranchListener;
import brown.tracingplane.impl.BaggageListener.JoinListener;

public class BDLContextProvider implements BaggageProvider<BDLContext> {

    static final Logger log = LoggerFactory.getLogger(BDLContextProvider.class);

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
     * The mapping of baggage handlers to bag numbers
     */
    BaggageHandlerRegistry registry = BaggageHandlerRegistry.create();

    /**
     * Register a {@link BaggageHandler} for the specified key. In general, this mapping should be configured statically
     * in config, rather than using this method.
     */
    public void register(BagKey key, BaggageHandler<?> handler) {
        if (key == null) {
            log.error("Unable to register handler for null key: " + String.valueOf(handler));
            return;
        }
        if (handler == null) {
            log.error("Cannot register null handler for key " + key);
            return;
        }
        registry = registry.add(key, handler);
    }

}
