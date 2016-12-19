package edu.brown.cs.systems.tracingplane.baggage_buffers.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import edu.brown.cs.systems.tracingplane.atom_layer.types.AtomLayerException;
import edu.brown.cs.systems.tracingplane.atom_layer.types.ByteBuffers;
import edu.brown.cs.systems.tracingplane.atom_layer.types.SignedLexVarint;
import edu.brown.cs.systems.tracingplane.atom_layer.types.UnsignedLexVarint;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageReader;

public class ReaderHelpers {

    private ReaderHelpers() {}

    public static final Function<ByteBuffer, Boolean> ToBool =
            buf -> (buf != null && buf.remaining() == 1) ? buf.get(buf.position()) != 0 : null;
    public static final Function<ByteBuffer, Integer> ToInt =
            buf -> (buf != null && buf.remaining() == 4) ? buf.getInt(buf.position()) : null;
    public static final Function<ByteBuffer, Integer> ToSignedLexVarInt32 = buf -> {
        if (buf == null) return null;
        try {
            return SignedLexVarint.readLexVarInt32(buf);
        } catch (AtomLayerException e) {
            return null;
        }
    };
    public static final Function<ByteBuffer, Integer> ToUnsignedLexVarInt32 = buf -> {
        if (buf == null) return null;
        try {
            return UnsignedLexVarint.readLexVarUInt32(buf);
        } catch (AtomLayerException e) {
            return null;
        }
    };
    public static final Function<ByteBuffer, Long> ToLong =
            buf -> (buf != null && buf.remaining() == 8) ? buf.getLong(buf.position()) : null;
    public static final Function<ByteBuffer, Long> ToSignedLexVarInt64 = buf -> {
        if (buf == null) return null;
        try {
            return SignedLexVarint.readLexVarInt64(buf);
        } catch (AtomLayerException e) {
            return null;
        }
    };
    public static final Function<ByteBuffer, Long> ToUnsignedLexVarInt64 = buf -> {
        if (buf == null) return null;
        try {
            return UnsignedLexVarint.readLexVarUInt64(buf);
        } catch (AtomLayerException e) {
            return null;
        }
    };
    public static final Function<ByteBuffer, Float> ToFloat =
            buf -> (buf != null && buf.remaining() == 4) ? buf.getFloat(buf.position()) : null;
    public static final Function<ByteBuffer, Double> ToDouble =
            buf -> (buf != null && buf.remaining() == 8) ? buf.getDouble(buf.position()) : null;
    public static final Function<ByteBuffer, String> ToString = buf -> ByteBuffers.getString(buf);
    public static final Function<ByteBuffer, ByteBuffer> ToBytes = buf -> buf;
    
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

    public static <T> Set<T> set(BaggageReader reader, Function<ByteBuffer, T> cast) {
        return collect(reader, cast, () -> new HashSet<T>());
    }

    public static List<Long> longs(BaggageReader reader) {
        return list(reader, ToLong);
    }

}
