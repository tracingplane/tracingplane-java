package brown.tracingplane.bdl;

import brown.tracingplane.baggageprotocol.BaggageWriter;
import brown.tracingplane.baggageprotocol.ElementWriter;

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
