package edu.brown.cs.systems.tracingplane.baggage_buffers;

import java.io.Closeable;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.Bag;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.baggage_layer.BaggageLayer;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageReader;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageWriter;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;
import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayerCallbacks.TransitHandler;
import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayerCallbacks.TransitLayerCallbackRegistry;

/**
 * <p>
 * The final layer in the baggage stack. BaggageBuffers is an implementation of the BaggageLayer. It enables you to
 * register baggage-buffers compiled classes then access them through the generated accessor methods.
 * </p>
 */
public class BaggageBuffers implements BaggageLayer<BaggageBuffersContents> {

    private static final TransitLayerCallbackRegistry<BaggageBuffersContents> callbacks =
            new TransitLayerCallbackRegistry<>(BaggageBuffers::branchImpl, BaggageBuffers::joinImpl);

    /**
     * Register a callback handler that will be invoked when branching and joining
     */
    public Closeable registerCallbackHandler(TransitHandler transitHandler) {
        return callbacks.add(transitHandler);
    }

    @Override
    public boolean isInstance(Baggage baggage) {
        return baggage == null || baggage instanceof BaggageBuffersContents;
    }

    @Override
    public BaggageBuffersContents newInstance() {
        return null;
    }

    @Override
    public void discard(BaggageBuffersContents baggage) {}

    @Override
    public BaggageBuffersContents branch(BaggageBuffersContents from) {
        return callbacks.branch(from);
    }

    private static BaggageBuffersContents branchImpl(BaggageBuffersContents from) {
        return from == null ? null : from.branch();
    }

    @Override
    public BaggageBuffersContents join(BaggageBuffersContents left, BaggageBuffersContents right) {
        return callbacks.join(left, right);
    }

    private static BaggageBuffersContents joinImpl(BaggageBuffersContents left, BaggageBuffersContents right) {
        if (left == null) {
            return right;
        } else if (right == null) {
            return left;
        } else {
            return left.mergeWith(right);
        }
    }

    @Override
    public BaggageBuffersContents read(BaggageReader reader) {
        return BaggageBuffersContents.parseFrom(reader);
    }

    @Override
    public BaggageWriter write(BaggageBuffersContents instance) {
        return instance == null ? null : instance.serialize();
    }

    /**
     * Access the current thread's baggage and retrieve the object stored in the specified bag
     * 
     * @param key the key to look up
     * @return the object if the current thread's baggage is an instance of {@link BaggageBuffersContents} and has an
     *         object mapped to the specified key; else null
     */
    public static Bag get(BagKey key) {
        return get(Baggage.get(), key);
    }

    /**
     * Attempts to retrieve the object stores in the specified bagkey from the provided baggage.
     * 
     * @param baggage a Baggage object that should be an instance of {@link BaggageBuffersContents}
     * @param key the key to look up
     * @return the object if the provided baggage is an instance of {@link BaggageBuffersContents} and has an object
     *         mapped to the specified key; else null
     */
    public static Bag get(Baggage baggage, BagKey key) {
        if (baggage instanceof BaggageBuffersContents) {
            return ((BaggageBuffersContents) baggage).get(key);
        }
        return null;
    }

    /**
     * Map the specified key to the provided bag. Updates the baggage being stored for the current thread.
     * 
     * @param key the key to access
     * @param bag the new bag to map to this key
     */
    public static void set(BagKey key, Bag value) {
        if (key != null) {
            Baggage original = Baggage.get();
            Baggage updated = set(original, key, value);
            if (original != updated) {
                Baggage.set(updated);
            }
        }
    }

    /**
     * Map the specified key to the provided bag. Returns a possibly new baggage instance with the updated mapping.
     * 
     * @param baggage the baggage to modify
     * @param key the key to update
     * @param bag the new bag to map to this key
     * @return a possibly new baggage instance with the updated mapping.
     */
    public static Baggage set(Baggage baggage, BagKey key, Bag value) {
        if (key != null) {
            if (baggage instanceof BaggageBuffersContents) {
                return ((BaggageBuffersContents) baggage).put(key, value);
            } else {
                return new BaggageBuffersContents().put(key, value);
            }
        } else {
            return baggage;
        }
    }

}
