package edu.brown.cs.systems.tracingplane.baggage_layer;

import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.Parser;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.Serializer;

public interface BaggageHandler<T> extends Parser<T>, Serializer<T> {

    public T join(T first, T second);

    public T branch(T from);

}
