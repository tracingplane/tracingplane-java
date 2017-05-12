package brown.tracingplane;

/**
 * It wouldn't be Java without some a camel-case caravan of nouns.
 * 
 * {@link BaggageProviderFactory} is expected to have a no-arg constructor so that it can be instantiated by reflection.
 */
public interface BaggageProviderFactory {

    /**
     * Instantiate the {@link BaggageProvider} that this factory provides.
     * 
     * @return a {@link BaggageProvider} instance
     */
    public BaggageProvider<? extends BaggageContext> provider();

}
