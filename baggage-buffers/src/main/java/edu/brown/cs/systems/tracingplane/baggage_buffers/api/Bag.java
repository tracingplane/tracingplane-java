package edu.brown.cs.systems.tracingplane.baggage_buffers.api;

/**
 * BaggageBuffers generates classes that implement this interface, Bag.  They also generate BaggageHandler implementations
 */
public interface Bag {
    
    BaggageHandler<?> handler();

}
