package edu.brown.cs.systems.tracingplane.baggage_buffers.impl;

import java.nio.ByteBuffer;
import edu.brown.cs.systems.tracingplane.atom_layer.types.SignedLexVarint;
import edu.brown.cs.systems.tracingplane.atom_layer.types.UnsignedLexVarint;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageWriter;

public class WriterHelpers {

    private WriterHelpers() {}

    /** Write a boolean data atom */
    public static void writeBool(BaggageWriter writer, boolean value) {
        byte toWrite = (byte) (value ? 1 : 0);
        writer.newDataAtom(1).put(toWrite);
    }

    /** Write an unsigned integer, encoded lexicographically */
    public static void writeUInt32(BaggageWriter writer, int value) {
        UnsignedLexVarint.writeLexVarUInt32(writer.newDataAtom(5), value);
    }

    /** Write a signed integer, encoded lexicographically */
    public static void writeSInt32(BaggageWriter writer, int value) {
        SignedLexVarint.writeLexVarInt32(writer.newDataAtom(5), value);
    }

    /** Write a fixed 4-byte integer data atom */
    public static void writeFixed32(BaggageWriter writer, int value) {
        writer.newDataAtom(Integer.BYTES).putInt(value);
    }

    /** Write an unsigned long, encoded lexicographically */
    public static void writeUInt64(BaggageWriter writer, int value) {
        UnsignedLexVarint.writeLexVarUInt64(writer.newDataAtom(9), value);
    }

    /** Write a signed long, encoded lexicographically */
    public static void writeSInt64(BaggageWriter writer, int value) {
        SignedLexVarint.writeLexVarInt64(writer.newDataAtom(9), value);
    }

    /** Write a fixed 8-byte long data atom */
    public static void writeFixed64(BaggageWriter writer, long value) {
        writer.newDataAtom(Long.BYTES).putLong(value);
    }

    /** Write a float data atom */
    public static void writeFloat(BaggageWriter writer, float value) {
        writer.newDataAtom(Float.BYTES).putFloat(value);
    }

    /** Write a double data atom */
    public static void writeDouble(BaggageWriter writer, double value) {
        writer.newDataAtom(Double.BYTES).putDouble(value);
    }

    /** Write a string data atom */
    public static void writeString(BaggageWriter writer, String value) {
        byte[] bytes = value.getBytes();
        writer.newDataAtom(bytes.length).put(bytes);
    }

    /** Write a bytes data atom */
    public static void writeBytes(BaggageWriter writer, ByteBuffer bytes) {
        writer.writeBytes(bytes);
    }

}
