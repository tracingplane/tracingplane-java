package brown.tracingplane.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import brown.tracingplane.baggageprotocol.BagKey;
import brown.tracingplane.bdl.Bag;
import brown.tracingplane.bdl.BaggageHandler;

/**
 * <p>
 * {@link BaggageHandlerRegistry} manages the mapping of baggage handlers to bag numbers. The globally configured mapping of
 * baggage handlers to bag numbers is maintained in {@link BDLContext#registry}.
 * </p>
 * 
 * <p>
 * BDL-compiled objects must be registered with a static bag number in order to be used. In concept, this is similar to
 * how protocols get mapped to ports (e.g., SSH maps to port 22, etc.). To register a bag, either specify it as a
 * command line configuration value:
 * 
 * <pre>
 * -Dbag.22=my.compiled.object.MyObject
 * </pre>
 * 
 * or in your <code>application.conf</code>:
 * 
 * <pre>
 * bag.22 = "my.compiled.object.MyObject"
 * </pre>
 * 
 * or statically in code:
 * 
 * <pre>
 * BDLContext.register(22, my.compiled.object.MyObject.class);
 * </pre>
 * </p>
 */
public class BaggageHandlerRegistry {

    private static final Logger log = LoggerFactory.getLogger(BaggageHandlerRegistry.class);

    private static final String BAGS_CONFIGURATION_KEY = "bag";

    /* Implementation notes: Could be volatile or atomic reference, but not trying to optimize for adding handlers at
     * runtime (only at init). Could be treemap if the performance different is fine */
    final BagKey[] keys;
    final BaggageHandler<?>[] handlers;

    final Map<BaggageHandler<?>, BagKey> handlersToKeys = new HashMap<>();
    final Map<BagKey, BaggageHandler<?>> keysToHandlers = new TreeMap<>();

    /**
     * Create a {@link BaggageHandlerRegistry} instance by parsing configured values from the default config
     */
    public static BaggageHandlerRegistry create() {
        return create(ConfigFactory.load());
    }

    /**
     * Create a {@link BaggageHandlerRegistry} instance by parsing the mappings configured in the provided {@link Config}
     * 
     * @param config a typesafe config
     * @return a {@link BaggageHandlerRegistry} instance with handlers loaded for the configured bag keys
     */
    public static BaggageHandlerRegistry create(Config config) {
        Map<BagKey, BaggageHandler<?>> mapping = new TreeMap<>();
        for (Entry<String, ConfigValue> x : config.getConfig(BAGS_CONFIGURATION_KEY).entrySet()) {
            String bagHandlerClassName = x.getValue().unwrapped().toString();
            Integer bagNumber = parseBagKey(x.getKey(), bagHandlerClassName);

            if (bagNumber == null) continue;

            BagKey key = BagKey.indexed(bagNumber);
            BaggageHandler<?> handler = resolveHandler(bagHandlerClassName);
            if (handler == null) continue;

            mapping.put(key, handler);
        }
        return new BaggageHandlerRegistry(mapping);
    }

    private BaggageHandlerRegistry(Map<BagKey, BaggageHandler<?>> mapping) {
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

        if (mapping.size() == 0) {
            log.warn("No baggage handlers are registered -- if this is unexpected, ensure `bag` is correctly configured");
        } else {
            String handlersString = mapping.entrySet().stream()
                                           .map(e -> "\t" + e.getKey().toString() + ": " +
                                                     e.getValue().getClass().getName().toString())
                                           .collect(Collectors.joining("\n"));
            log.info(mapping.size() + " baggage handlers registered:\n" + handlersString);
        }
    }

    /**
     * Look up the {@link BagKey} that the specified {@link BaggageHandler} is registered to
     */
    public BagKey get(BaggageHandler<?> handler) {
        return handlersToKeys.get(handler);
    }

    /**
     * Registers the provided key with the provided handler into the process's default registrations. Recommended NOT to
     * do this -- instead you should statically configure the registration of keys to handlers.
     * 
     * {@link BaggageHandlerRegistry} instances are immutable, so adding a new registration will create and return a new
     * {@link BaggageHandlerRegistry} instance with the additional mapping.
     */
    public BaggageHandlerRegistry add(BagKey key, BaggageHandler<?> handler) {
        Map<BagKey, BaggageHandler<?>> newMapping = new TreeMap<>(keysToHandlers);
        newMapping.put(key, handler);
        return new BaggageHandlerRegistry(newMapping);
    }

    /**
     * Unregisters the handler for the specified bag key.
     * 
     * {@link BaggageHandlerRegistry} instances are immutable, so removing a registration will return a new
     * {@link BaggageHandlerRegistry} instance with the mapping removed.
     */
    public BaggageHandlerRegistry remove(BagKey key) {
        Map<BagKey, BaggageHandler<?>> newMapping = new TreeMap<>(keysToHandlers);
        if (newMapping.remove(key) != null) {
            return new BaggageHandlerRegistry(newMapping);
        } else {
            return this;
        }
    }

    static Integer parseBagKey(String key, String className) {
        if (key.startsWith("\"") && key.endsWith("\"")) {
            key = key.substring(1, key.length() - 1);
        }
        try {
            return Integer.parseUnsignedInt(key);
        } catch (NumberFormatException e) {
            log.error("Cannot configure handler \"" + key + " = " + className +
                      "\" due to unparsable unsigned integer " + key);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    static BaggageHandler<?> resolveHandler(String bagClassName) {
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

    static BaggageHandler<?> resolveHandlerForBag(Class<? extends Bag> cls) {
        try {
            Constructor<? extends Bag> constructor = cls.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance().handler();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                 | NoSuchMethodException | SecurityException e) {
            log.error("Unable to instantiate Bag class " + cls, e);
        }
        return null;
    }

}
