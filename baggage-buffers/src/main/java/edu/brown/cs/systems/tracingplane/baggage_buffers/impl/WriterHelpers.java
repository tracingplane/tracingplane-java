package edu.brown.cs.systems.tracingplane.baggage_buffers.impl;

import java.nio.ByteBuffer;
import java.util.function.Function;
import edu.brown.cs.systems.tracingplane.atom_layer.types.SignedLexVarint;
import edu.brown.cs.systems.tracingplane.atom_layer.types.UnsignedLexVarint;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.ElementWriter;

public class WriterHelpers {

    private WriterHelpers() {}

    public static final Function<Boolean, ByteBuffer> from_bool = v -> Cast.from_bool(v);
    public static final Function<Integer, ByteBuffer> from_int32 = v -> Cast.from_int32(v);
    public static final Function<Integer, ByteBuffer> from_sint32 = v -> Cast.from_sint32(v);
    public static final Function<Integer, ByteBuffer> from_fixed32 = v -> Cast.from_fixed32(v);
    public static final Function<Integer, ByteBuffer> from_sfixed32 = v -> Cast.from_sfixed32(v);
    public static final Function<Long, ByteBuffer> from_int64 = v -> Cast.from_int64(v);
    public static final Function<Long, ByteBuffer> from_sint64 = v -> Cast.from_sint64(v);
    public static final Function<Long, ByteBuffer> from_fixed64 = v -> Cast.from_fixed64(v);
    public static final Function<Long, ByteBuffer> from_sfixed64 = v -> Cast.from_sfixed64(v);
    public static final Function<Float, ByteBuffer> from_float = v -> Cast.from_float(v);
    public static final Function<Double, ByteBuffer> from_double = v -> Cast.from_double(v);
    public static final Function<String, ByteBuffer> from_string = v -> Cast.from_string(v);
    public static final Function<ByteBuffer, ByteBuffer> from_bytes = v -> Cast.from_bytes(v);


    /** Write a boolean data atom */
    public static void writeBool(ElementWriter writer, boolean value) {
        byte toWrite = (byte) (value ? 1 : 0);
        writer.newDataAtom(1).put(toWrite);
    }

    /** Write an unsigned integer, encoded lexicographically */
    public static void writeUInt32(ElementWriter writer, int value) {
        UnsignedLexVarint.writeLexVarUInt32(writer.newDataAtom(5), value);
    }

    /** Write a signed integer, encoded lexicographically */
    public static void writeSInt32(ElementWriter writer, int value) {
        SignedLexVarint.writeLexVarInt32(writer.newDataAtom(5), value);
    }

    /** Write a fixed 4-byte integer data atom */
    public static void writeFixed32(ElementWriter writer, int value) {
        writer.newDataAtom(Integer.BYTES).putInt(value);
    }

    /** Write an unsigned long, encoded lexicographically */
    public static void writeUInt64(ElementWriter writer, long value) {
        UnsignedLexVarint.writeLexVarUInt64(writer.newDataAtom(9), value);
    }

    /** Write a signed long, encoded lexicographically */
    public static void writeSInt64(ElementWriter writer, long value) {
        SignedLexVarint.writeLexVarInt64(writer.newDataAtom(9), value);
    }

    /** Write a fixed 8-byte long data atom */
    public static void writeFixed64(ElementWriter writer, long value) {
        writer.newDataAtom(Long.BYTES).putLong(value);
    }

    /** Write a float data atom */
    public static void writeFloat(ElementWriter writer, float value) {
        writer.newDataAtom(Float.BYTES).putFloat(value);
    }

    /** Write a double data atom */
    public static void writeDouble(ElementWriter writer, double value) {
        writer.newDataAtom(Double.BYTES).putDouble(value);
    }

    /** Write a string data atom */
    public static void writeString(ElementWriter writer, String value) {
        byte[] bytes = value.getBytes();
        writer.newDataAtom(bytes.length).put(bytes);
    }

    /** Write a bytes data atom */
    public static void writeBytes(ElementWriter writer, ByteBuffer bytes) {
        writer.writeBytes(bytes);
    }

}
