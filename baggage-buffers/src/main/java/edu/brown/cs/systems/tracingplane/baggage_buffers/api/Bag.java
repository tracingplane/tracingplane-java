package edu.brown.cs.systems.tracingplane.baggage_buffers.api;

/**
 * BaggageBuffers generates classes that implement this interface, Bag. They also generate BaggageHandler
 * implementations
 * 
 * Bags are propagated by BaggageBuffers across all execution boundaries. They define semantics for branching, joining,
 * serializing, and trimming.
 */
public interface Bag {

    BaggageHandler<?> handler();

}
