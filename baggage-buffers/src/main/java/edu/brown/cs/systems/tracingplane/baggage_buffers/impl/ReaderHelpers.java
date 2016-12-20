package edu.brown.cs.systems.tracingplane.baggage_buffers.impl;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.ElementReader;

/**
 * Helper methods used by compiled baggagebuffers classes
 */
public class ReaderHelpers {

    private ReaderHelpers() {}

    public static final Function<ByteBuffer, Boolean> to_bool = buf -> Cast.to_bool(buf);
    public static final Function<ByteBuffer, Integer> to_int32 = buf -> Cast.to_int32(buf);
    public static final Function<ByteBuffer, Integer> to_sint32 = buf -> Cast.to_sint32(buf);
    public static final Function<ByteBuffer, Integer> to_fixed32 = buf -> Cast.to_fixed32(buf);
    public static final Function<ByteBuffer, Integer> to_sfixed32 = buf -> Cast.to_sfixed32(buf);
    public static final Function<ByteBuffer, Long> to_int64 = buf -> Cast.to_int64(buf);
    public static final Function<ByteBuffer, Long> to_sint64 = buf -> Cast.to_sint64(buf);
    public static final Function<ByteBuffer, Long> to_fixed64 = buf -> Cast.to_fixed64(buf);
    public static final Function<ByteBuffer, Long> to_sfixed64 = buf -> Cast.to_sfixed64(buf);
    public static final Function<ByteBuffer, Float> to_float = buf -> Cast.to_float(buf);
    public static final Function<ByteBuffer, Double> to_double = buf -> Cast.to_double(buf);
    public static final Function<ByteBuffer, String> to_string = buf -> Cast.to_string(buf);
    public static final Function<ByteBuffer, ByteBuffer> to_bytes = buf -> Cast.to_bytes(buf);

    /** Find the next data item that matches the provided predicate */
    public static ByteBuffer filterNext(ElementReader reader, Predicate<ByteBuffer> filter) {
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
    public static <T> T castNext(ElementReader reader, Function<ByteBuffer, T> cast) {
        ByteBuffer buf = null;
        T out = null;
        while ((buf = reader.nextData()) != null) {
            if ((out = cast.apply(buf)) != null) {
                return out;
            }
        }
        return null;
    }

    public static <In, Out> Out combine(ElementReader reader, Function<ByteBuffer, In> cast,
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

    public static <In, Out extends Collection<In>> Out collect(ElementReader reader, Function<ByteBuffer, In> cast,
                                                               Supplier<Out> defaultValueGenerator) {
        return combine(reader, cast, defaultValueGenerator, (collection, value) -> {
            collection.add(value);
            return collection;
        });
    }

}
