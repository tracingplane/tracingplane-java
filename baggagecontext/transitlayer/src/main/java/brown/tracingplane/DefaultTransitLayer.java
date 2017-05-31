package brown.tracingplane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import brown.tracingplane.impl.NoOpTransitLayerFactory;
import brown.tracingplane.impl.ThreadLocalTransitLayer;

/**
 * <p>
 * Loads the {@link TransitLayerFactory} specified by <code>baggage.transit</code> using reflection.
 * </p>
 */
public class DefaultTransitLayer {

    private static final Logger log = LoggerFactory.getLogger(DefaultTransitLayer.class);

    private final TransitLayerFactory factory;
    private final TransitLayer transitlayer;

    /** Not instantiable */
    private DefaultTransitLayer() {
        Config config = ConfigFactory.load();

        TransitLayerFactory factory = null;
        if (!config.hasPath("baggage.transit")) {
            log.warn("No TransitLayerFactory has been configured using baggage.transit -- baggage propagation using the ActiveBaggage interface will be disabled");
        } else {
            try {
                String transitLayerClass = config.getString("baggage.transit");

                try {
                    Object instantiated = Class.forName(transitLayerClass).newInstance();

                    try {
                        factory = (TransitLayerFactory) instantiated;
                    } catch (ClassCastException e) {
                        log.error("The configured baggage.transit should be an instance of TransitLayerFactory; found " +
                                  instantiated.getClass().getName());
                    }
                } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                    log.error("Unable to instantiate TransitLayerFactory specified by baggage.transit " +
                              transitLayerClass, e);
                }

            } catch (ConfigException.WrongType e) {
                Object v = config.getAnyRef("baggage.transit");
                log.error("Invalid baggage.transit has been configured -- baggage propagation using the ActiveBaggage interface will be disabled.  Expected a string for baggage.provider, found " +
                          v.getClass().getName() + ": " + v);
            }
        }

        if (factory == null) {
            this.factory = new NoOpTransitLayerFactory();
            this.transitlayer = this.factory.transitlayer();
        } else {
            this.factory = factory;
            this.transitlayer = this.factory.transitlayer();
        }
    }

    private static DefaultTransitLayer instance = null;

    private static DefaultTransitLayer instance() {
        if (instance == null) {
            synchronized (DefaultTransitLayer.class) {
                if (instance == null) {
                    instance = new DefaultTransitLayer();
                }
            }
        }
        return instance;
    }

    /**
     * @return the configured {@link TransitLayer} instance. The default transit layer can be set using
     *         -Dbaggage.transit. If no instance has been configured, this method will return a
     *         {@link ThreadLocalTransitLayer} that uses simple thread-local storage to store baggage contexts.
     */
    public static TransitLayer get() {
        return instance().transitlayer;
    }

}
