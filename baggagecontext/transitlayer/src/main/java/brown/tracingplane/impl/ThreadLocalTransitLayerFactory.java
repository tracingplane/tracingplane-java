package brown.tracingplane.impl;

import brown.tracingplane.DefaultBaggageProvider;
import brown.tracingplane.TransitLayer;
import brown.tracingplane.TransitLayerFactory;

/**
 * <p>
 * The {@link TransitLayerFactory} that creates {@link ThreadLocalTransitLayer} instances. If you need to manually
 * configure the {@link TransitLayer}, you would set <code>baggage.transit</code> to be this class, e.g.:
 * 
 * <pre>
 * -Dbaggage.transit=brown.tracingplane.impl.ThreadLocalTransitLayerFactory
 * </pre>
 * 
 * or in the typesafe config <code>application.conf</code>:
 * 
 * <pre>
 * baggage.transit = "brown.tracingplane.impl.ThreadLocalTransitLayerFactory"
 * </pre>
 * </p>
 */
public class ThreadLocalTransitLayerFactory implements TransitLayerFactory {

    @Override
    public TransitLayer transitlayer() {
        return new ThreadLocalTransitLayer(DefaultBaggageProvider.getWrapped());
    }

}
