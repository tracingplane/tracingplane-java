package brown.tracingplane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import brown.tracingplane.noopprovider.NoOpBaggageContextProvider;

/**
 * <p>
 * Loads the default configured {@link BaggageProvider} using reflection. Checks the <code>baggage.provider</code>
 * property.
 * </p>
 */
public class DefaultBaggageProvider {

    private static final Logger log = LoggerFactory.getLogger(DefaultBaggageProvider.class);

    /** Not instantiable */
    private DefaultBaggageProvider() {}

    private static boolean initialized = false;
    private static BaggageProvider<? extends BaggageContext> instance = null;

    @SuppressWarnings("unchecked")
    private static synchronized void initialize() {
        if (initialized) {
            return;
        }
        try {
            String providerClass = ConfigFactory.load().getString("baggage.provider");
            try {
                instance = (BaggageProvider<? extends BaggageContext>) Class.forName(providerClass).newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                log.error("Unable to instantiate baggage.provider " + providerClass, e);
            }
        } catch (ConfigException.Missing e) {
            log.error("No baggage.provider has been configured");
        } catch (ConfigException.WrongType e) {
            log.error("Invalid value (expected a string) for baggage.provider " +
                      ConfigFactory.load().getAnyRef("baggage.provider"));
        } finally {
            initialized = true;
            if (instance == null) {
                instance = new NoOpBaggageContextProvider();
            }
            log.info("Baggage provider initialied to " + instance.getClass().getName());
        }
    }

    /**
     * @return the configured {@link BaggageProvider} instance. The default baggage provider can be set using
     *         -Dbaggage.provider. If no instance has been configured, this method will return a
     *         {@link NoOpBaggageContextProvider}.
     */
    public static BaggageProvider<? extends BaggageContext> get() {
        if (!initialized) {
            initialize();
        }
        return instance;
    }

    /**
     * @return the configured {@link BaggageProvider} instance, wrapped with a {@link BaggageProviderProxy}. The default
     *         baggage provider can be set using -Dbaggage.provider. If no instance has been configured, this method
     *         will return a {@link NoOpBaggageContextProvider}.
     */
    public static BaggageProvider<BaggageContext> getWrapped() {
        BaggageProvider<? extends BaggageContext> provider = get();
        if (provider instanceof NoOpBaggageContextProvider) {
            return (NoOpBaggageContextProvider) provider;
        } else {
            return BaggageProviderProxy.wrap(provider);
        }
    }

}
