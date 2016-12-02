package edu.brown.cs.systems.tracingplane.baggage_layer.impl_old3;

public interface BagSerialization<T> {
	
	/** Get a parser that can parse elements of type {@link T} */
	public BagParser<T> parser();

	/** Get a serializer that can serialize elements of type {@link T} */
	public BagSerializer<T> serializer();

}
