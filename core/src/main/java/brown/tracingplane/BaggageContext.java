package brown.tracingplane;

import java.util.concurrent.Callable;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;

/**
 * <p>
 * A {@link BaggageContext} is an opaque context object belonging to an execution. Baggage contexts are propagated
 * alongside requests as they execute -- they should be passed to new threads, included with work items (e.g.,
 * {@link Runnable}, {@link Callable}, etc.), serialized in network headers, and so on.
 * </p>
 * 
 * <p>
 * This interface provides no methods for accessing the baggage contents. When instrumenting the system to propagate
 * Baggage contexts, you should not need to access or manipulate baggage contents. Instead, use the static propagation
 * methods in the {@link Baggage} class.
 * </p>
 * 
 * <p>
 * The static methods in this interface represent the principle propagation operations for {@link BaggageContext}
 * instances, specifically, duplicating contexts, merging contexts, and serialization.
 * </p>
 *
 */
public interface BaggageContext {

}
