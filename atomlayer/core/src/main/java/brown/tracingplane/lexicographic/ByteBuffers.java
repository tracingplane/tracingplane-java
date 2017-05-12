package brown.tracingplane.lexicographic;

import java.nio.ByteBuffer;
import java.util.Comparator;

/**
 * Some functions I found useful for working with byte buffers
 */
public class ByteBuffers {

    public static final Comparator<ByteBuffer> LEXICOGRAPHIC_COMPARATOR =
            UnsignedByteBuffer.lexicographicalComparator();

    /**
     * <p>Copies all remaining bytes from src to dest. This is done without modifying the src's position. However, the
     * dest's position is updated.</p>
     * 
     * @param src the buffer to copy from. its position will not be updated.
     * @param dest the buffer to copy to. its position will be updated.
     */
    public static void copyTo(ByteBuffer src, ByteBuffer dest) {
        if (src == null || dest == null) {
            return;
        }
        if (src.hasArray()) {
            byte[] array = src.array();
            int offset = src.arrayOffset() + src.position();
            int length = src.remaining();
            dest.put(array, offset, length);
        } else {
            for (int i = src.position(); i < src.limit(); i++) {
                dest.put(src.get(i));
            }
        }
    }

    public static ByteBuffer copyWithPrefix(byte prefix, ByteBuffer src) {
        ByteBuffer buf = ByteBuffer.allocate(src.remaining() + 1);
        buf.put(prefix);
        copyTo(src, buf);
        buf.position(0);
        return buf;
    }

    /**
     * Copies the remaining bytes form src to a new byte buffer. This is done without modifying the src position. The
     * dest position will be 0.
     */
    public static ByteBuffer copyRemaining(ByteBuffer src) {
        ByteBuffer buf = ByteBuffer.allocate(src.remaining());
        copyTo(src, buf);
        buf.position(0);
        return buf;
    }

    /** Copies the remaining bytes form src to a byte array. This is done without modifying the src position. */
    public static byte[] copyRemainingBytes(ByteBuffer src) {
        return copyRemaining(src).array();
    }
    
    /** Read the remaining bytes from a bytebuffer as a string */
    public static String getString(ByteBuffer buf) {
        if (buf == null) {
            return null;
        }
        
        final byte[] array;
        final int offset;
        final int length = buf.remaining();
        if (buf.hasArray()) {
            array = buf.array();
            offset = buf.arrayOffset() + buf.position();
        } else {
            array = new byte[length];
            offset = 0;
            buf.get(array);
        }
        return new String(array, offset, length);
    }

}
