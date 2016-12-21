package edu.brown.cs.systems.tracingplane.baggage_buffers;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.Bag;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.BaggageHandler;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;

public class BaggageBuffersConfig {

    private static final Logger log = LoggerFactory.getLogger(BaggageBuffersConfig.class);

    private static final String BAGS_KEY = "tracingplane.baggage-buffers.bags";

    public String baggageLayerFactory;
    public final Map<BagKey, BaggageHandler<?>> handlers = new HashMap<>();

    public BaggageBuffersConfig() {
        Config conf = ConfigFactory.load();

        for (Entry<String, ConfigValue> x : conf.getConfig(BAGS_KEY).entrySet()) {
            String bagClassName = x.getValue().unwrapped().toString();

            Integer key = parseBagKey(x.getKey(), bagClassName);
            if (key == null) {
                continue;
            }

            BaggageHandler<?> handler = getHandlerForClass(key, bagClassName);
            if (handler == null) {
                continue;
            }

            handlers.put(BagKey.indexed(key), handler);

            log.info("Registered " + bagClassName + " to baggage key " + key);
        }
    }

    private Integer parseBagKey(String key, String className) {
        try {
            if (key.startsWith("\"") && key.endsWith("\"")) {
                key = key.substring(1, key.length() - 1);
            }
            return Integer.parseUnsignedInt(key);
        } catch (NumberFormatException e) {
            log.error("Cannot configure handler \"" + key + " = " + className +
                      "\" due to unparsable unsigned integer " + key);
        }
        return null;
    }

    private BaggageHandler<?> getHandlerForClass(int key, String className) {
        try {
            Class<?> cls = Class.forName(className);
            if (Bag.class.isAssignableFrom(cls)) {
                return ((Bag) cls.newInstance()).handler();
            } else {
                log.error("The configured bag class \"" + className + "\" for bag " + key +
                          " does not implement the Bag interface");
            }
        } catch (ClassNotFoundException e) {
            log.error("The configured bag class \"" + className + "\" for bag " + key + " could not be found");
        } catch (InstantiationException e) {
            log.error("The configured bag class \"" + className + "\" for bag " + key + " could not be instantiated",
                      e);
        } catch (IllegalAccessException e) {
            log.error("The configured bag class \"" + className + "\" for bag " + key + " could not be instantiated",
                      e);
        }
        return null;
    }

    public static void main(String[] args) {
        BasicConfigurator.configure();
        new BaggageBuffersConfig();
    }

}
