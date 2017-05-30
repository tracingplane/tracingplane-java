package brown.tracingplane.bdl;

/**
 * The BDL compiler generates classes that implement this interface, Bag. They also generate BaggageHandler
 * implementations
 * 
 * Bags are propagated by the Tracing Plane across all execution boundaries. They define semantics for branching,
 * joining, serializing, and trimming.
 */
public interface Bag {

    BaggageHandler<?> handler();

}
