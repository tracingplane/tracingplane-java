package edu.brown.cs.systems.tracingplane.baggage_buffers.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageReader;

public class ReaderHelpers {

    public static final Function<ByteBuffer, Integer> ToInt =
            buf -> (buf != null && buf.remaining() == 4) ? buf.getInt(buf.position()) : null;
    public static final Function<ByteBuffer, Long> ToLong =
            buf -> (buf != null && buf.remaining() == 8) ? buf.getLong(buf.position()) : null;
    public static final Function<ByteBuffer, Double> ToDouble =
            buf -> (buf != null && buf.remaining() == 8) ? buf.getDouble(buf.position()) : null;

    /** Find the next data item that matches the provided predicate */
    public static ByteBuffer filterNext(BaggageReader reader, Predicate<ByteBuffer> filter) {
        ByteBuffer buf;
        while ((buf = reader.nextData()) != null) {
            if (filter.test(buf)) {
                return buf;
            }
        }
        return null;
    }

    /**
     * Get the next data item and transform bytebuffer to type using the provided function. If the cast function returns
     * null, it will move on to the next data item.
     */
    public static <T> T castNext(BaggageReader reader, Function<ByteBuffer, T> cast) {
        ByteBuffer buf = null;
        T out = null;
        while ((buf = reader.nextData()) != null) {
            if ((out = cast.apply(buf)) != null) {
                return out;
            }
        }
        return null;
    }

    public static <In, Out> Out combine(BaggageReader reader, Function<ByteBuffer, In> cast,
                                        Supplier<Out> defaultValueGenerator, BiFunction<Out, In, Out> combiner) {
        Out combined = null;
        In next = null;
        while ((next = castNext(reader, cast)) != null) {
            if (combined == null) {
                combined = defaultValueGenerator.get();
            }
            combined = combiner.apply(combined, next);
        }
        return combined;
    }

    public static <In, Out extends Collection<In>> Out collect(BaggageReader reader, Function<ByteBuffer, In> cast,
                                                               Supplier<Out> defaultValueGenerator) {
        return combine(reader, cast, defaultValueGenerator, (collection, value) -> {
            collection.add(value);
            return collection;
        });
    }

    public static <T> List<T> list(BaggageReader reader, Function<ByteBuffer, T> cast) {
        return collect(reader, cast, () -> new ArrayList<T>());
    }

    public static Long firstLong(BaggageReader reader) {
        return castNext(reader, ToLong);
    }

    public static List<Long> longs(BaggageReader reader) {
        return list(reader, ToLong);
    }

}
