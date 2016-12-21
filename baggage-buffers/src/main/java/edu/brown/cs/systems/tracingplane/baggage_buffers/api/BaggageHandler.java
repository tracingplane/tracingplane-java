package edu.brown.cs.systems.tracingplane.baggage_buffers.api;

import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageWriter;

public interface BaggageHandler<T extends Bag> extends Parser<T>, Serializer<T>, Joiner<T>, Brancher<T> {

    public boolean isInstance(Bag bag);

    @SuppressWarnings("unchecked")
    public default void serialize(BaggageWriter writer, Bag bag) {
        if (isInstance(bag)) {
            serialize(writer, (T) bag);
        }
    }

    @SuppressWarnings("unchecked")
    public default T join(Bag first, Bag second) {
        if (isInstance(first)) {
            if (isInstance(second)) {
                return join((T) first, (T) second);
            } else {
                return (T) first;
            }
        } else {
            if (isInstance(second)) {
                return (T) second;
            } else {
                return null;
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public default T branch(Bag from) {
        if (isInstance(from)) {
            return branch((T) from);
        } else {
            return null;
        }
    }

}
