package brown.tracingplane;

/**
 * <p>
 * Factory for {@link TransitLayer} instances; primarily used to configure the {@link TransitLayer} used by the
 * {@link ActiveBaggage} static API.
 * </p>
 * 
 * <p>
 * {@link TransitLayerFactory} is expected to have a no-arg constructor so that it can be instantiated by reflection.
 * </p>
 */
public interface TransitLayerFactory {

    /**
     * Instantiate the {@link TransitLayer} implementation that this factory provides.
     * 
     * @return a {@link TransitLayer} instance
     */
    public TransitLayer transitlayer();

}
