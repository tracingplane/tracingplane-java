package edu.brown.cs.systems.tracingplane.baggage_buffers.api;

import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageWriter;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.ElementWriter;

public interface Serializer<T> {

    public void serialize(BaggageWriter writer, T instance);

    /** A serializer that only serializes data elements and not bags */
    public static interface ElementSerializer<T> extends Serializer<T> {

        @Override
        public default void serialize(BaggageWriter writer, T instance) {
            serialize((ElementWriter) writer, instance);
        }

        public void serialize(ElementWriter writer, T instance);

    }

}
