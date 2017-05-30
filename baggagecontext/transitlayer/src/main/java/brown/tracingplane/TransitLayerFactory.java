package brown.tracingplane;

/**
 * It wouldn't be Java without a camel-case caravan of nouns.
 * 
 * {@link TransitLayerFactory} is expected to have a no-arg constructor so that it can be instantiated by reflection.
 */
public interface TransitLayerFactory {

    /**
     * Instantiate the {@link TransitLayer} implementation that this factory provides.
     * 
     * @return a {@link TransitLayer} instance
     */
    public TransitLayer transitlayer();

}
