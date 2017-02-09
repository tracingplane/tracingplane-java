package edu.brown.cs.systems.tracingplane.baggage_buffers;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.Bag;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.BaggageHandler;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;

/**
 * Global registration of baggage handlers to keys. Not currently implemented to support adding new handlers at runtime
 */
public class Registrations {
    
    private static final Logger log = LoggerFactory.getLogger(Registrations.class);

    static Registrations instance = Registrations.create();


    /* Implementation notes: Could be volatile or atomic reference, but not trying to optimize for adding handlers at
     * runtime (only at init). Could be treemap if the performance different is fine */
    final BagKey[] keys;
    final BaggageHandler<?>[] handlers;

    final Map<BaggageHandler<?>, BagKey> handlersToKeys = new HashMap<>();
    final Map<BagKey, BaggageHandler<?>> keysToHandlers = new TreeMap<>();

    public static Registrations create() {
        return create(new BaggageBuffersConfig());
    }

    public static Registrations create(BaggageBuffersConfig config) {
        Map<BagKey, BaggageHandler<?>> mapping = new TreeMap<>();
        for (Integer k : config.registeredBags.keySet()) {
            BagKey key = BagKey.indexed(k);
            BaggageHandler<?> handler = resolveHandler(config.registeredBags.get(k));
            mapping.put(key, handler);
        }

        return new Registrations(mapping);
    }

    private Registrations(Map<BagKey, BaggageHandler<?>> mapping) {
        keys = new BagKey[mapping.size()];
        handlers = new BaggageHandler<?>[mapping.size()];

        int i = 0;
        for (BagKey key : mapping.keySet()) {
            BaggageHandler<?> handler = mapping.get(key);

            keysToHandlers.put(key, handler);
            handlersToKeys.put(handler, key);
            keys[i] = key;
            handlers[i] = handler;
            i++;
        }
    }

    /**
     * Look up the global static registration of this handler
     * 
     * @param handler the handler to look up
     * @return the bag key that this handler is registered to, otherwise null
     */
    public static BagKey lookup(BaggageHandler<?> handler) {
        return instance.handlersToKeys.get(handler);
    }

    /**
     * Registers the provided key with the provided handler into the process's default registrations. HIGHLY recommended
     * NOT to do this -- instead you should statically configure the registration of keys to handlers. However, for
     * trialling code and testing, this is an easier way to avoid error messages.
     */
    public static void register(BagKey key, BaggageHandler<?> handler) {
        instance = instance.add(key, handler);
    }

    @SuppressWarnings("unchecked")
    private static BaggageHandler<?> resolveHandler(String bagClassName) {
        try {
            Class<?> cls = Class.forName(bagClassName);
            if (Bag.class.isAssignableFrom(cls)) {
                return resolveHandlerForBag((Class<? extends Bag>) cls);
            } else {
                log.error("Unable to get BaggageHandler for non-Bag class " + cls);
            }
        } catch (ClassNotFoundException e) {
            log.error("Cannot instantiate Bag handler " + bagClassName + ", class not found", e);
        }
        return null;
    }

    public Registrations add(BagKey key, BaggageHandler<?> handler) {
        Map<BagKey, BaggageHandler<?>> newMapping = new TreeMap<>(keysToHandlers);
        newMapping.put(key, handler);
        return new Registrations(newMapping);
    }

    public Registrations remove(BagKey key) {
        Map<BagKey, BaggageHandler<?>> newMapping = new TreeMap<>(keysToHandlers);
        if (newMapping.remove(key) != null) {
            return new Registrations(newMapping);
        } else {
            return this;
        }
    }

    public static BaggageHandler<?> resolveHandlerForBag(Class<? extends Bag> cls) {
        try {
            return cls.newInstance().handler();
        } catch (InstantiationException e) {
            log.error("Unable to instantiate Bag class " + cls, e);
        } catch (IllegalAccessException e) {
            log.error("Unable to instantiate Bag class " + cls, e);
        }
        return null;
    }

}
