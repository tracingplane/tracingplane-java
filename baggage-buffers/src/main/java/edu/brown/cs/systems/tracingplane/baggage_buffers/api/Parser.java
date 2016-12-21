package edu.brown.cs.systems.tracingplane.baggage_buffers.api;

import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageReader;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.ElementReader;

public interface Parser<T> {

    public T parse(BaggageReader reader);

    public static interface ElementParser<T> extends Parser<T> {

        @Override
        public default T parse(BaggageReader reader) {
            return parse((ElementReader) reader);
        }

        public T parse(ElementReader reader);

    }

}
