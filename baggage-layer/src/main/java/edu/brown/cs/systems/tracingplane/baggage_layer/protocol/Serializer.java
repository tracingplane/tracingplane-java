package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

public interface Serializer<T> {

    public void serialize(BaggageWriter builder, T instance);

}
