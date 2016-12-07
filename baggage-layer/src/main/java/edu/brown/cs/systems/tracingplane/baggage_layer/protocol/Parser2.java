package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

public interface Parser2<T> {
	
	public T parse(BaggageReader reader);

}
