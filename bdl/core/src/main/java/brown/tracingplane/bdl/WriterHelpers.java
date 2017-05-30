package brown.tracingplane.bdl;

import java.nio.ByteBuffer;
import java.util.function.BiConsumer;
import java.util.function.Function;
import brown.tracingplane.atomlayer.SignedLexVarint;
import brown.tracingplane.atomlayer.UnsignedLexVarint;
import brown.tracingplane.baggageprotocol.ElementWriter;

/**
 * Helper methods used by compiled baggagebuffers classes
 */
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
    
    
    public static final BiConsumer<Boolean, ByteBuffer> write_bool = (v, buf) -> buf.put((byte) (v ? 1 : 0));
    public static final BiConsumer<Integer, ByteBuffer> write_int32 = (v, buf) -> UnsignedLexVarint.writeLexVarUInt32(buf, v);
    public static final BiConsumer<Integer, ByteBuffer> write_sint32 = (v, buf) -> SignedLexVarint.writeLexVarInt32(buf, v);
    public static final BiConsumer<Integer, ByteBuffer> write_fixed32 = (v, buf) -> buf.putInt(v);
    public static final BiConsumer<Long, ByteBuffer> write_int64 = (v, buf) -> UnsignedLexVarint.writeLexVarUInt64(buf, v);
    public static final BiConsumer<Long, ByteBuffer> write_sint64 = (v, buf) -> SignedLexVarint.writeLexVarInt64(buf, v);
    public static final BiConsumer<Long, ByteBuffer> write_fixed64 = (v, buf) -> buf.putLong(v);
    public static final BiConsumer<Float, ByteBuffer> write_float = (v, buf) -> buf.putFloat(v);
    public static final BiConsumer<Double, ByteBuffer> write_double = (v, buf) -> buf.putDouble(v);
    public static final BiConsumer<String, ByteBuffer> write_string = (v, buf) -> buf.put(v.getBytes());
    public static final BiConsumer<ByteBuffer, ByteBuffer> write_bytes = (v, buf) -> buf.put(v);
    
    
    
    
    /** Write an empty data atom */
    public static void writeEmpty(ElementWriter writer) {
        writer.newDataAtom(0);
    }

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
