package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

public interface Serializer2<T> {
	
	public void serialize(BaggageWriter builder, int level, T instance);

}
