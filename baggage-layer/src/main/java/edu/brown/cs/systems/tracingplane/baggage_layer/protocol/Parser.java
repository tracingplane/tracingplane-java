package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

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
