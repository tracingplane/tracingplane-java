package edu.brown.cs.systems.tracingplane.baggage_buffers.impl;

import java.nio.ByteBuffer;
import edu.brown.cs.systems.tracingplane.atom_layer.types.ByteBuffers;
import edu.brown.cs.systems.tracingplane.atom_layer.types.SignedLexVarint;
import edu.brown.cs.systems.tracingplane.atom_layer.types.UnsignedLexVarint;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.Struct.StructReader;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.Struct.StructSizer;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.Struct.StructWriter;

/**
 * Helpers to read and write values from structs
 */
public class StructHelpers {
    
    public static final ByteBuffer EMPTY_BYTE_BUFFER = ByteBuffer.allocate(0);
    
    private StructHelpers() {}
    
    public static StructReader<Boolean> boolReader = buf -> buf.get() == 0;
    public static StructReader<Integer> int32Reader = buf -> UnsignedLexVarint.readLexVarUInt32(buf);
    public static StructReader<Integer> sint32Reader = buf -> SignedLexVarint.readLexVarInt32(buf);
    public static StructReader<Integer> fixed32Reader = buf -> buf.getInt();
    public static StructReader<Long> int64Reader = buf -> UnsignedLexVarint.readLexVarUInt64(buf);
    public static StructReader<Long> sint64Reader = buf -> SignedLexVarint.readLexVarInt64(buf);
    public static StructReader<Long> fixed64Reader = buf -> buf.getLong();
    public static StructReader<Float> floatReader = buf -> buf.getFloat();
    public static StructReader<Double> doubleReader = buf -> buf.getDouble();
    public static StructReader<String> stringReader = buf -> {
        final byte[] array;
        final int offset;
        final int length = Math.min(buf.remaining(), UnsignedLexVarint.readLexVarUInt32(buf));
        if (buf.hasArray()) {
            array = buf.array();
            offset = buf.arrayOffset() + buf.position();
            buf.position(buf.position() + length);
        } else {
            array = new byte[length];
            buf.get(array);
            offset = 0;
        }
        return new String(array, offset, length);
    };
    public static StructReader<ByteBuffer> bytesReader = buf -> {
        int length = UnsignedLexVarint.readLexVarUInt32(buf);
        ByteBuffer copy = buf.slice();
        copy.limit(length);
        return copy;
    };
    
    public static StructWriter<Boolean> boolWriter = (buf, v) -> buf.put((byte) (v ? 1 : 0));
    public static StructWriter<Integer> int32Writer = (buf, v) -> UnsignedLexVarint.writeLexVarUInt32(buf, v);
    public static StructWriter<Integer> sint32Writer = (buf, v) -> SignedLexVarint.writeLexVarInt32(buf, v);
    public static StructWriter<Integer> fixed32Writer = (buf, v) -> buf.putInt(v);
    public static StructWriter<Long> int64Writer = (buf, v) -> UnsignedLexVarint.writeLexVarUInt64(buf, v);
    public static StructWriter<Long> sint64Writer = (buf, v) -> SignedLexVarint.writeLexVarInt64(buf, v);
    public static StructWriter<Long> fixed64Writer = (buf, v) -> buf.putLong(v);
    public static StructWriter<Float> floatWriter = (buf, v) -> buf.putFloat(v);
    public static StructWriter<Double> doubleWriter = (buf, v) -> buf.putDouble(v);
    public static StructWriter<String> stringWriter = (buf, v) -> {
        byte[] bs = v.getBytes();
        UnsignedLexVarint.writeLexVarUInt32(buf, bs.length);
        buf.put(bs);
    };
    public static StructWriter<ByteBuffer> bytesWriter = (buf, v) -> {
        UnsignedLexVarint.writeLexVarUInt32(buf, v.remaining());
        ByteBuffers.copyTo(v, buf);
    };

    public static StructSizer<Boolean> boolSizer = v -> 1;
    public static StructSizer<Integer> int32Sizer = v -> UnsignedLexVarint.encodedLength(v);
    public static StructSizer<Integer> sint32Sizer = v -> SignedLexVarint.encodedLength(v);
    public static StructSizer<Integer> fixed32Sizer = v -> 4;
    public static StructSizer<Long> int64Sizer = v -> UnsignedLexVarint.encodedLength(v);
    public static StructSizer<Long> sint64Sizer = v -> SignedLexVarint.encodedLength(v);
    public static StructSizer<Long> fixed64Sizer = v -> 8;
    public static StructSizer<Float> floatSizer = v -> 4;
    public static StructSizer<Double> doubleSizer = v -> 8;
    public static StructSizer<String> stringSizer = v -> {
        int byteLength = v.getBytes().length;
        return UnsignedLexVarint.encodedLength(byteLength) + byteLength; 
    };
    public static StructSizer<ByteBuffer> bytesSizer = v -> {
        return UnsignedLexVarint.encodedLength(v.remaining()) + v.remaining();
    };

}
