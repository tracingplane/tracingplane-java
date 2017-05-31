package brown.tracingplane.bdl;

import java.nio.ByteBuffer;
import brown.tracingplane.atomlayer.AtomLayerException;
import brown.tracingplane.atomlayer.ByteBuffers;
import brown.tracingplane.atomlayer.SignedLexVarint;
import brown.tracingplane.atomlayer.UnsignedLexVarint;

/**
 * Converts between {@link ByteBuffer} and Object representations for primitive BDL types.
 */
public class Cast {

    public static Boolean to_bool(ByteBuffer buf) {
        return buf.remaining() == 1 ? buf.get(buf.position()) != 0 : null;
    }

    public static Integer to_int32(ByteBuffer buf) {
        try {
            return UnsignedLexVarint.readLexVarUInt32(buf);
        } catch (AtomLayerException e) {
            return null;
        }
    }

    public static Integer to_sint32(ByteBuffer buf) {
        try {
            return SignedLexVarint.readLexVarInt32(buf);
        } catch (AtomLayerException e) {
            return null;
        }
    }

    public static Integer to_fixed32(ByteBuffer buf) {
        return buf.remaining() == 4 ? buf.getInt(buf.position()) : null;
    }

    public static Integer to_sfixed32(ByteBuffer buf) {
        return buf.remaining() == 4 ? buf.getInt(buf.position()) : null;
    }

    public static Long to_int64(ByteBuffer buf) {
        try {
            return UnsignedLexVarint.readLexVarUInt64(buf);
        } catch (AtomLayerException e) {
            return null;
        }
    }

    public static Long to_sint64(ByteBuffer buf) {
        try {
            return SignedLexVarint.readLexVarInt64(buf);
        } catch (AtomLayerException e) {
            return null;
        }
    }

    public static Long to_fixed64(ByteBuffer buf) {
        return buf.remaining() == 8 ? buf.getLong(buf.position()) : null;
    }

    public static Long to_sfixed64(ByteBuffer buf) {
        return buf.remaining() == 8 ? buf.getLong(buf.position()) : null;
    }

    public static Float to_float(ByteBuffer buf) {
        return buf.remaining() == 4 ? buf.getFloat(buf.position()) : null;
    }

    public static Double to_double(ByteBuffer buf) {
        return buf.remaining() == 8 ? buf.getDouble(buf.position()) : null;
    }

    public static ByteBuffer to_bytes(ByteBuffer buf) {
        return buf.slice();
    }

    public static String to_string(ByteBuffer buf) {
        return ByteBuffers.getString(buf);
    }

    public static ByteBuffer from_bool(Boolean value) {
        if (value != null) {
            ByteBuffer buf = ByteBuffer.allocate(1);
            buf.put((byte) (value ? 1 : 0));
            buf.flip();
            return buf;
        } else {
            return null;
        }
    }

    public static ByteBuffer from_int32(Integer value) {
        if (value != null) {
            ByteBuffer buf = ByteBuffer.allocate(UnsignedLexVarint.encodedLength(value));
            UnsignedLexVarint.writeLexVarUInt32(buf, value);
            buf.flip();
            return buf;
        } else {
            return null;
        }
    }

    public static ByteBuffer from_sint32(Integer value) {
        if (value != null) {
            ByteBuffer buf = ByteBuffer.allocate(SignedLexVarint.encodedLength(value));
            SignedLexVarint.writeLexVarInt32(buf, value);
            buf.flip();
            return buf;
        } else {
            return null;
        }
    }

    public static ByteBuffer from_fixed32(Integer value) {
        if (value != null) {
            ByteBuffer buf = ByteBuffer.allocate(4);
            buf.putInt(value);
            buf.flip();
            return buf;
        } else {
            return null;
        }
    }

    public static ByteBuffer from_sfixed32(Integer value) {
        if (value != null) {
            ByteBuffer buf = ByteBuffer.allocate(4);
            buf.putInt(value);
            buf.flip();
            return buf;
        } else {
            return null;
        }
    }

    public static ByteBuffer from_int64(Long value) {
        if (value != null) {
            ByteBuffer buf = ByteBuffer.allocate(UnsignedLexVarint.encodedLength(value));
            UnsignedLexVarint.writeLexVarUInt64(buf, value);
            buf.flip();
            return buf;
        } else {
            return null;
        }
    }

    public static ByteBuffer from_sint64(Long value) {
        if (value != null) {
            ByteBuffer buf = ByteBuffer.allocate(SignedLexVarint.encodedLength(value));
            SignedLexVarint.writeLexVarInt64(buf, value);
            buf.flip();
            return buf;
        } else {
            return null;
        }
    }

    public static ByteBuffer from_fixed64(Long value) {
        if (value != null) {
            ByteBuffer buf = ByteBuffer.allocate(8);
            buf.putLong(value);
            buf.flip();
            return buf;
        } else {
            return null;
        }
    }

    public static ByteBuffer from_sfixed64(Long value) {
        if (value != null) {
            ByteBuffer buf = ByteBuffer.allocate(8);
            buf.putLong(value);
            buf.flip();
            return buf;
        } else {
            return null;
        }
    }

    public static ByteBuffer from_float(Float value) {
        if (value != null) {
            ByteBuffer buf = ByteBuffer.allocate(4);
            buf.putFloat(value);
            buf.flip();
            return buf;
        } else {
            return null;
        }
    }

    public static ByteBuffer from_double(Double value) {
        if (value != null) {
            ByteBuffer buf = ByteBuffer.allocate(8);
            buf.putDouble(value);
            buf.flip();
            return buf;
        } else {
            return null;
        }
    }

    public static ByteBuffer from_string(String value) {
        if (value != null) {
            return ByteBuffer.wrap(value.getBytes());
        } else {
            return null;
        }
    }

    public static ByteBuffer from_bytes(ByteBuffer value) {
        return value.slice();
    }

}
