package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

public interface Parser<T> {

    public T parse(BaggageReader reader);

}
