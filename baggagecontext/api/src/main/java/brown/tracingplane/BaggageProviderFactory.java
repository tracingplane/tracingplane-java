package brown.tracingplane;

/**
 * <p>
 * Factory for {@link BaggageProvider} instances; primarily used to configure the {@link BaggageProvider} used by the
 * {@link Baggage} static API.
 * </p>
 * 
 * <p>
 * {@link BaggageProviderFactory} is expected to have a no-arg constructor so that it can be instantiated by reflection.
 * </p>
 */
public interface BaggageProviderFactory {

    /**
     * Instantiate the {@link BaggageProvider} that this factory provides.
     * 
     * @return a {@link BaggageProvider} instance
     */
    public BaggageProvider<? extends BaggageContext> provider();

}
