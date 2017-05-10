package brown.tracingplane;

/**
 * <p>
 * A {@link BaggageContext} is an opaque context object belonging to an execution. Baggage contexts are propagated
 * alongside requests as they execute -- they should be passed to new threads, included with work items (e.g.,
 * {@link java.lang.Runnable}, {@link java.util.concurrent.Callable}, etc.), serialized in network headers, and so on.
 * </p>
 * 
 * <p>
 * {@link BaggageContext} objects have no direct methods for manipulating contexts or accessing data that they carry.
 * This is because, in the common case, a {@link BaggageContext} is typically empty, and an empty context is typically
 * implemented using null values.
 * </p>
 * 
 * <p>
 * To propagate {@link BaggageContext} objects, call {@link BaggageProvider} methods such as
 * {@link BaggageProvider#branch(BaggageContext)}, {@link BaggageProvider#serialize(BaggageContext)}, etc.
 * </p>
 */
public interface BaggageContext {

}
