package brown.tracingplane.bdl;

import brown.tracingplane.baggageprotocol.BaggageReader;
import brown.tracingplane.baggageprotocol.ElementReader;

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
