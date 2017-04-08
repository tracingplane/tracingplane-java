package edu.brown.cs.systems.tracingplane.baggage_buffers;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import edu.brown.cs.systems.tracingplane.baggage_buffers.BaggageBuffersUtils.BaggageAccessListener;
import edu.brown.cs.systems.tracingplane.baggage_buffers.BaggageBuffersUtils.NullBaggageListener;

public class BaggageBuffersConfig {

    private static final Logger log = LoggerFactory.getLogger(BaggageBuffersConfig.class);

    private static final String BAGS_KEY = "baggage-buffers.bags";

    public String baggageLayerFactory;
    public final Map<Integer, String> registeredBags = new HashMap<>();
    private final String baggageAccessListenerClassName;

    public BaggageBuffersConfig() {
        Config conf = ConfigFactory.load();
        
        baggageAccessListenerClassName = conf.getString("baggage-buffers.access-listener");

        for (Entry<String, ConfigValue> x : conf.getConfig(BAGS_KEY).entrySet()) {
            String bagClassName = x.getValue().unwrapped().toString();

            Integer key = parseBagKey(x.getKey(), bagClassName);
            if (key != null) {
                registeredBags.put(key, bagClassName);
            }

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
    
    BaggageAccessListener getBaggageAccessListenerInstance() {
        try {
            return (BaggageAccessListener) Class.forName(baggageAccessListenerClassName).newInstance();
        } catch (InstantiationException e) {
            log.error("Cannot instantiate baggage listener class " + baggageAccessListenerClassName, e);
        } catch (IllegalAccessException e) {
            log.error("Cannot instantiate baggage listener class " + baggageAccessListenerClassName, e);
        } catch (ClassNotFoundException e) {
            log.error("Unknown baggage listener class " + baggageAccessListenerClassName);
        }
        return new NullBaggageListener();
    }

}
