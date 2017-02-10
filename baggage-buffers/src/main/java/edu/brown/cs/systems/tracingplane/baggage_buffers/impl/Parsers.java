package edu.brown.cs.systems.tracingplane.baggage_buffers.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.Parser;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.Parser.ElementParser;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.Struct;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.Struct.StructHandler;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.Struct.StructReader;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageReader;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.ElementReader;

/**
 * Parsers for built-in types used by compiled baggage buffers classes
 */
public class Parsers {

    private Parsers() {}
    
    public static ElementParser<Boolean> taintParser() {
        return combine(boolParser(), () -> false, (l, r) -> l || r);
    }

    public static ElementParser<Boolean> boolParser() {
        return castNext(ReaderHelpers.to_bool);
    }

    public static ElementParser<Integer> int32Parser() {
        return castNext(ReaderHelpers.to_int32);
    }

    public static ElementParser<Integer> sint32Parser() {
        return castNext(ReaderHelpers.to_sint32);
    }

    public static ElementParser<Integer> fixed32Parser() {
        return castNext(ReaderHelpers.to_fixed32);
    }

    public static ElementParser<Integer> sfixed32Parser() {
        return castNext(ReaderHelpers.to_sfixed32);
    }

    public static ElementParser<Long> int64Parser() {
        return castNext(ReaderHelpers.to_int64);
    }

    public static ElementParser<Long> sint64Parser() {
        return castNext(ReaderHelpers.to_sint64);
    }

    public static ElementParser<Long> fixed64Parser() {
        return castNext(ReaderHelpers.to_fixed64);
    }

    public static ElementParser<Long> sfixed64Parser() {
        return castNext(ReaderHelpers.to_sfixed64);
    }

    public static ElementParser<Float> floatParser() {
        return castNext(ReaderHelpers.to_float);
    }

    public static ElementParser<Double> doubleParser() {
        return castNext(ReaderHelpers.to_double);
    }

    public static ElementParser<String> stringParser() {
        return castNext(ReaderHelpers.to_string);
    }

    public static ElementParser<ByteBuffer> bytesParser() {
        return castNext(ReaderHelpers.to_bytes);
    }

    public static <T> ElementParser<Set<T>> setParser(ElementParser<T> elementParser) {
        return collect(elementParser, () -> new HashSet<T>());
    }

    public static <T> ElementParser<List<T>> listParser(ElementParser<T> elementParser) {
        return collect(elementParser, () -> new ArrayList<T>());
    }

    public static <K, V> Parser<Map<K, V>> mapParser(Function<ByteBuffer, K> keyCast, Parser<V> valueParser) {
        return new Parser<Map<K, V>>() {
            public Map<K, V> parse(BaggageReader reader) {
                BagKey currentKey = null;
                Map<K, V> out = null;
                while ((currentKey = reader.enter()) != null) {
                    try {
                        if (!(currentKey instanceof BagKey.Keyed)) {
                            continue;
                        }
                        K key = keyCast.apply(((BagKey.Keyed) currentKey).key);
                        if (key == null) {
                            continue;
                        }

                        V value = valueParser.parse(reader);
                        if (value == null) {
                            continue;
                        }

                        if (out == null) {
                            out = new HashMap<K, V>();
                        }
                        out.put(key, value);
                    } finally {
                        reader.exit();
                    }
                }
                return out;
            }
        };
    }

    static <T> ElementParser<T> castNext(Function<ByteBuffer, T> cast) {
        return new ElementParser<T>() {
            public T parse(ElementReader reader) {
                ByteBuffer buf = null;
                T out = null;
                while ((buf = reader.nextData()) != null) {
                    if ((out = cast.apply(buf)) != null) {
                        return out;
                    }
                }
                return null;
            }
        };
    }

    static <In, Out> ElementParser<Out> combine(ElementParser<In> elementParser, Supplier<Out> defaultValueGenerator,
                                                BiFunction<Out, In, Out> combiner) {
        return new ElementParser<Out>() {
            public Out parse(ElementReader reader) {
                Out combined = null;
                In next = null;
                while ((next = elementParser.parse(reader)) != null) {
                    if (combined == null) {
                        combined = defaultValueGenerator.get();
                    }
                    combined = combiner.apply(combined, next);
                }
                return combined;
            }
        };
    }

    static <In, Out extends Collection<In>> ElementParser<Out> collect(ElementParser<In> elementParser,
                                                                       Supplier<Out> defaultValueGenerator) {
        BiFunction<Out, In, Out> combiner = (c, v) -> {
            c.add(v);
            return c;
        };
        return combine(elementParser, defaultValueGenerator, combiner);
    }

}
