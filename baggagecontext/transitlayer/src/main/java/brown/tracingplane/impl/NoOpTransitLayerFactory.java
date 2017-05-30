package brown.tracingplane.impl;

import brown.tracingplane.ActiveBaggage;
import brown.tracingplane.TransitLayer;
import brown.tracingplane.TransitLayerFactory;

/**
 * <p>
 * A {@link TransitLayer} that does nothing. This is the default {@link TransitLayer} implementation that will be used
 * if none is configured. If this is the {@link TransitLayer} implementation used, the effect is that any calls to the
 * {@link ActiveBaggage} interface will find that there is no active baggage.
 * </p>
 */
public class NoOpTransitLayerFactory implements TransitLayerFactory {

    @Override
    public TransitLayer transitlayer() {
        return new NoOpTransitLayer();
    }
}
