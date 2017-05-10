package brown.tracingplane;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;

/**
 * <p>
 * Loads the default configured {@link TransitLayer} using reflection. Checks the <code>baggage.transit</code> property.
 * If <code>baggage.transit</code> is not set, then the default transit layer will be {@link ThreadLocalTransitLayer}.
 * </p>
 */
public class DefaultTransitLayer {

    private static final Logger log = LoggerFactory.getLogger(DefaultTransitLayer.class);

    /** Not instantiable */
    private DefaultTransitLayer() {}

    private static boolean initialized = false;
    private static TransitLayer instance = null;

    private static synchronized void initialize() {
        if (initialized) {
            return;
        }
        BaggageProvider<BaggageContext> provider = DefaultBaggageProvider.getWrapped();
        try {
            String providerClass = ConfigFactory.load().getString("baggage.transit");
            try {
                Constructor<?> constructor = Class.forName(providerClass).getDeclaredConstructor(BaggageProvider.class);
                instance = (TransitLayer) constructor.newInstance(provider);
            } catch (NoSuchMethodException | SecurityException | ClassNotFoundException | InstantiationException
                     | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                log.error("Unable to instantiate baggage.transit " + providerClass, e);
            }
        } catch (ConfigException.Missing e) {
            log.error("No baggage.transit has been configured");
        } catch (ConfigException.WrongType e) {
            log.error("Invalid value (expected a string) for baggage.transit " +
                      ConfigFactory.load().getAnyRef("baggage.transit"));
        } finally {
            initialized = true;
            if (instance == null) {
                instance = new ThreadLocalTransitLayer(provider);
            }
            log.info("Transit Layer initialied to " + instance.getClass().getName());
        }
    }

    /**
     * @return the configured {@link TransitLayer} instance. The default transit layer can be set using
     *         -Dbaggage.transit. If no instance has been configured, this method will return a
     *         {@link ThreadLocalTransitLayer} that uses simple thread-local storage to store baggage contexts.
     */
    public static TransitLayer get() {
        if (!initialized) {
            initialize();
        }
        return instance;
    }

}
