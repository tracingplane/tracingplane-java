package brown.tracingplane;

/**
 * <p>
 * The {@link TransitLayer} links the {@link Baggage} static method calls to specific {@link BaggageContext} instances
 * for the configured {@link BaggageProvider}.
 * 
 * 
 * <p>
 * {@link TransitLayer} provides static methods for propagating {@link BaggageContext} instances and using thread-local
 * storage to maintain baggage for the current thread.
 * </p>
 * 
 * <p>
 * Some of the methods in this class mirror static methods in {@link Baggage}, but with different arguments -- these
 * methods operate directly on the thread-local baggage instance.
 * </p>
 *
 */
public final class TransitLayer {

    /** {@link TransitLayer} is not instantiable */
    private TransitLayer() {}

}
