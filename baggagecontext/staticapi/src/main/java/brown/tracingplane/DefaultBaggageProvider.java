package brown.tracingplane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import brown.tracingplane.impl.NoOpBaggageContextProvider;
import brown.tracingplane.impl.NoOpBaggageContextProviderFactory;

/**
 * <p>
 * Loads the default configured {@link BaggageProvider} using reflection. Checks the <code>baggage.provider</code>
 * property.
 * </p>
 */
public class DefaultBaggageProvider {

    private static final Logger log = LoggerFactory.getLogger(DefaultBaggageProvider.class);

    private final BaggageProviderFactory factory;
    private final BaggageProvider<? extends BaggageContext> provider;
    private final BaggageProvider<BaggageContext> wrappedProvider;

    private DefaultBaggageProvider() {
        Config config = ConfigFactory.load();

        BaggageProviderFactory factory = null;
        if (!config.hasPath("baggage.provider")) {
            log.warn("No BaggageProviderFactory has been configured using baggage.provider -- baggage propagation will be disabled");
        } else {
            try {
                String providerClass = config.getString("baggage.provider");

                try {
                    Object instantiated = Class.forName(providerClass).newInstance();

                    try {
                        factory = (BaggageProviderFactory) instantiated;
                    } catch (ClassCastException e) {
                        log.error("The configured baggage.provider should be an instance of BaggageProviderFactory; found " +
                                  instantiated.getClass().getName());
                    }
                } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                    log.error("Unable to instantiate BaggageProviderFactory specified by baggage.provider " +
                              providerClass, e);
                }

            } catch (ConfigException.WrongType e) {
                Object v = config.getAnyRef("baggage.provider");
                log.error("Invalid baggage.provider has been configured -- baggage propagation will be disabled.  Expected a string for baggage.provider, found " +
                          v.getClass().getName() + ": " + v);
            }
        }

        if (factory == null) {
            this.factory = new NoOpBaggageContextProviderFactory();
            this.wrappedProvider = new NoOpBaggageContextProvider();
            this.provider = this.wrappedProvider;
        } else {
            this.factory = factory;
            this.provider = factory.provider();
            this.wrappedProvider = BaggageProviderProxy.wrap(this.provider);
        }
    }

    private static DefaultBaggageProvider instance = null;

    private static DefaultBaggageProvider instance() {
        if (instance == null) {
            synchronized (DefaultBaggageProvider.class) {
                if (instance == null) {
                    instance = new DefaultBaggageProvider();
                }
            }
        }
        return instance;
    }

    /**
     * @return the configured {@link BaggageProvider} instance. The default baggage provider can be set using
     *         -Dbaggage.provider. If no instance has been configured, this method will return a
     *         {@link NoOpBaggageContextProvider}.
     */
    public static BaggageProvider<? extends BaggageContext> get() {
        return instance().provider;
    }

    /**
     * @return the configured {@link BaggageProvider} instance, wrapped with a {@link BaggageProviderProxy}. The default
     *         baggage provider can be set using -Dbaggage.provider. If no instance has been configured, this method
     *         will return a {@link NoOpBaggageContextProvider}.
     */
    public static BaggageProvider<BaggageContext> getWrapped() {
        return instance().wrappedProvider;
    }

}
