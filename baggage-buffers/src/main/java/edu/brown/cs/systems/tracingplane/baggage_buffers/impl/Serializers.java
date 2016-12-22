package edu.brown.cs.systems.tracingplane.baggage_buffers.impl;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import edu.brown.cs.systems.tracingplane.atom_layer.types.Lexicographic;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.Serializer;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.Serializer.ElementSerializer;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageWriter;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.ElementWriter;

/**
 * Serializers for built-in types used by compiled baggage buffers classes
 */
public class Serializers {

    private Serializers() {}

    public static ElementSerializer<Boolean> boolSerializer() {
        return new ElementSerializer<Boolean>() {
            public void serialize(ElementWriter writer, Boolean instance) {
                if (instance != null) {
                    WriterHelpers.writeBool(writer, instance);
                }
            }
        };
    }

    public static ElementSerializer<Integer> int32Serializer() {
        return new ElementSerializer<Integer>() {
            public void serialize(ElementWriter writer, Integer instance) {
                if (instance != null) {
                    WriterHelpers.writeUInt32(writer, instance);
                }
            }
        };
    }

    public static ElementSerializer<Integer> sint32Serializer() {
        return new ElementSerializer<Integer>() {
            public void serialize(ElementWriter writer, Integer instance) {
                if (instance != null) {
                    WriterHelpers.writeSInt32(writer, instance);
                }
            }
        };
    }

    public static ElementSerializer<Integer> fixed32Serializer() {
        return new ElementSerializer<Integer>() {
            public void serialize(ElementWriter writer, Integer instance) {
                if (instance != null) {
                    WriterHelpers.writeFixed32(writer, instance);
                }
            }
        };
    }

    public static ElementSerializer<Integer> sfixed32Serializer() {
        return new ElementSerializer<Integer>() {
            public void serialize(ElementWriter writer, Integer instance) {
                if (instance != null) {
                    WriterHelpers.writeFixed32(writer, instance);
                }
            }
        };
    }

    public static ElementSerializer<Long> int64Serializer() {
        return new ElementSerializer<Long>() {
            public void serialize(ElementWriter writer, Long instance) {
                if (instance != null) {
                    WriterHelpers.writeUInt64(writer, instance);
                }
            }
        };
    }

    public static ElementSerializer<Long> sint64Serializer() {
        return new ElementSerializer<Long>() {
            public void serialize(ElementWriter writer, Long instance) {
                if (instance != null) {
                    WriterHelpers.writeSInt64(writer, instance);
                }
            }
        };
    }

    public static ElementSerializer<Long> fixed64Serializer() {
        return new ElementSerializer<Long>() {
            public void serialize(ElementWriter writer, Long instance) {
                if (instance != null) {
                    WriterHelpers.writeFixed64(writer, instance);
                }
            }
        };
    }

    public static ElementSerializer<Long> sfixed64Serializer() {
        return new ElementSerializer<Long>() {
            public void serialize(ElementWriter writer, Long instance) {
                if (instance != null) {
                    WriterHelpers.writeFixed64(writer, instance);
                }
            }
        };
    }

    public static ElementSerializer<Float> floatSerializer() {
        return new ElementSerializer<Float>() {
            public void serialize(ElementWriter writer, Float instance) {
                if (instance != null) {
                    WriterHelpers.writeFloat(writer, instance);
                }
            }
        };
    }

    public static ElementSerializer<Double> doubleSerializer() {
        return new ElementSerializer<Double>() {
            public void serialize(ElementWriter writer, Double instance) {
                if (instance != null) {
                    WriterHelpers.writeDouble(writer, instance);
                }
            }
        };
    }

    public static ElementSerializer<String> stringSerializer() {
        return new ElementSerializer<String>() {
            public void serialize(ElementWriter writer, String instance) {
                if (instance != null) {
                    WriterHelpers.writeString(writer, instance);
                }
            }
        };
    }

    public static ElementSerializer<ByteBuffer> bytesSerializer() {
        return new ElementSerializer<ByteBuffer>() {
            public void serialize(ElementWriter writer, ByteBuffer instance) {
                if (instance != null) {
                    WriterHelpers.writeBytes(writer, instance);
                }
            }
        };
    }

    public static <T> ElementSerializer<Set<T>> setSerializer(ElementSerializer<T> elementSerializer) {
        return new ElementSerializer<Set<T>>() {
            public void serialize(ElementWriter writer, Set<T> instance) {
                if (instance == null) {
                    return;
                }
                for (T value : instance) {
                    elementSerializer.serialize(writer, value);
                }
                writer.sortData();
            }
        };
    }

    public static <K, V> Serializer<Map<K, V>> mapSerializer(Function<K, ByteBuffer> keySerializer,
                                                             Serializer<V> valueSerializer) {
        return new Serializer<Map<K, V>>() {
            public void serialize(BaggageWriter writer, Map<K, V> instance) {
                if (instance == null) {
                    return;
                }

                SortedMap<ByteBuffer, K> keys = new TreeMap<ByteBuffer, K>(Lexicographic.BYTE_BUFFER_COMPARATOR);
                for (K key : instance.keySet()) {
                    ByteBuffer serializedKey = keySerializer.apply(key);
                    if (serializedKey != null) {
                        keys.put(serializedKey, key);
                    }
                }

                for (ByteBuffer serializedKey : keys.keySet()) {
                    V value = instance.get(keys.get(serializedKey));
                    if (value != null) {
                        writer.enter(BagKey.keyed(serializedKey));
                        valueSerializer.serialize(writer, value);
                        writer.exit();
                    }
                }
            }
        };
    }

}
