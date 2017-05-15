package brown.tracingplane.atomlayer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import com.google.common.primitives.UnsignedBytes;

/**
 * <p>
 * This class has static methods for lexicographically comparing bytes. Lexicographic comparison compares the
 * <b>unsigned</b> byte representations starting from the left-most bit. It is like alphabetical comparison but for
 * bytes. For example, consider the following:
 * </p>
 * <ul>
 * <li>a = 0000 0001</li>
 * <li>b = 1000 0010 0010 0000</li>
 * <li>c = 0000 0000 0000 1111</li>
 * </ul>
 * 
 * <p>
 * The lexicographic ordering for this example is {@code c < a < b}.
 * </p>
 */
public class Lexicographic {

    private Lexicographic() {}

    /**
     * A Comparator for performing lexicographical comparison on {@link byte[]} arrays.
     */
    public static final Comparator<byte[]> BYTE_ARRAY_COMPARATOR = UnsignedBytes.lexicographicalComparator();

    /**
     * A Comparator for performing lexicographical comparison on {@link ByteBuffer} instances.
     */
    public static final Comparator<ByteBuffer> BYTE_BUFFER_COMPARATOR = UnsignedByteBuffer.lexicographicalComparator();

    /**
     * Perform lexicographical (ie, unsigned) comparison on two bytes
     * 
     * @param a a byte. this function uses the unsigned value of a, so if {@code a < 0}, the function compares a + 256.
     * @param b a byte. this function uses the unsigned value of b, so if {@code b < 0}, the function compares b + 256.
     * @return compares the unsigned values of <code>a</code> and <code>b</code>, returning a negative integer if
     *         {@code a < b}, 0 if {@code a == b}, or a positive integer if {@code a > b}.
     */
    public static int compare(byte a, byte b) {
        return UnsignedBytes.compare(a, b);
    }

    /**
     * Performs lexicographical comparison on two byte arrays, starting with index 0.
     * 
     * @param a an array of bytes
     * @param b an array of bytes
     * @return the lexicographical comparison of a and b: a negative integer if {@code a < b}, 0 if {@code a == b}, or a
     *         positive integer if {@code a > b}.
     */
    public static int compare(byte[] a, byte[] b) {
        return BYTE_ARRAY_COMPARATOR.compare(a, b);
    }

    /**
     * Performs lexicographical comparison on two {@link ByteBuffer}s, starting with index 0. This method uses absolute
     * indexes to compare a and b, and does not change their position or limit.
     * 
     * @param a an array of bytes
     * @param b an array of bytes
     * @return the lexicographical comparison of a and b: a negative integer if {@code a < b}, 0 if {@code a == b}, or a
     *         positive integer if {@code a > b}.
     */
    public static int compare(ByteBuffer a, ByteBuffer b) {
        return BYTE_BUFFER_COMPARATOR.compare(a, b);
    }

    /**
     * Performs an in-place sort of {@code bufs} lexicographically
     * 
     * @param bufs an array of bytebuffers
     * @return {@code bufs}, sorted lexicographically
     */
    public static ByteBuffer[] sort(ByteBuffer[] bufs) {
        Arrays.sort(bufs, BYTE_BUFFER_COMPARATOR);
        return bufs;
    }

    /**
     * Performs an in-place sort of {@code bufs} lexicographically
     * 
     * @param bufs a list of bytebuffers
     * @return {@code bufs}, sorted lexicographically
     */
    public static List<ByteBuffer> sort(List<ByteBuffer> bufs) {
        Collections.sort(bufs, BYTE_BUFFER_COMPARATOR);
        return bufs;
    }

    /**
     * <p>
     * Lexicographically merges the two provided lists of ByteBuffers. This method does <b>not</b> sort a, or b, or the
     * returned list.
     * </p>
     * 
     * <p>
     * If the merge encounters identical values (e.g., the next value of a == the next value of b), then both a and b
     * are advanced, and the value is only include once. For example:
     * </p>
     * 
     * <pre>
     * a = [ 00000100, 00001000, 00000001 ];                               // [4, 8, 1]
     * b = [ 00000010, 00001000, 00000011 ];                               // [2, 8, 3]
     * merge(a, b) = [ 00000010, 00000100, 00001000, 00000001, 00000011 ]; // [2, 4, 8, 1, 3]
     * </pre>
     * 
     * @param a a list of bytebuffers, possibly null
     * @param b a list of bytebuffers, possibly null
     * @return a and b, lexicographically merged
     */
    public static List<ByteBuffer> merge(List<ByteBuffer> a, List<ByteBuffer> b) {
        if (a == b || b == null) {
            return a;
        } else if (a == null) {
            return b;
        }
        int ia = 0, ib = 0, size_a = a.size(), size_b = b.size();
        final List<ByteBuffer> merged = new ArrayList<>(size_a + size_b);
        boolean different = false;
        while (ia < size_a && ib < size_b) {
            int comparison = BYTE_BUFFER_COMPARATOR.compare(a.get(ia), b.get(ib));
            if (comparison == 0) {
                merged.add(a.get(ia));
                ia++;
                ib++;
            } else if (comparison < 0) {
                merged.add(a.get(ia));
                ia++;
                different = true;
            } else if (comparison > 0) {
                merged.add(b.get(ib));
                ib++;
                different = true;
            }
        }
        
        if (!different) {
            return size_a < size_b ? b : a;
        }

        while (ia < size_a) {
            merged.add(a.get(ia++));
        }

        while (ib < size_b) {
            merged.add(b.get(ib++));
        }
        
        return merged;
    }

    /**
     * <p>
     * Takes two or more iterators as input and creates a {@link MergeIterator} that produces values in lexicographic
     * order as would be produced by {@link #merge(List, List)}
     * </p>
     * 
     * <p>
     * If the merge encounters identical values (e.g., the next value of a == the next value of b), then both a and b
     * are advanced, and the value is only include once. For example:
     * </p>
     * 
     * <pre>
     * a = [ 00000100, 00001000, 00000001 ];                               // [4, 8, 1]
     * b = [ 00000010, 00001000, 00000011 ];                               // [2, 8, 3]
     * merge(a, b) = [ 00000010, 00000100, 00001000, 00000001, 00000011 ]; // [2, 4, 8, 1, 3]
     * </pre>
     * 
     * @param a an iterator of bytebuffers, possibly null, possibly exhausted
     * @param b an iterator of bytebuffers, possibly null, possibly exhausted
     * @param moreIterators zero or more iterators of bytebuffers, possibly null, possibly exhausted
     * @return an iterator that produces elements from the input iterators, merged lexicographically
     */
    @SafeVarargs
    public static Iterator<ByteBuffer> merge(Iterator<ByteBuffer> a, Iterator<ByteBuffer> b,
                                             Iterator<ByteBuffer>... moreIterators) {
        List<Iterator<ByteBuffer>> iterators = new ArrayList<>(moreIterators.length + 2);
        if (a != null && a.hasNext()) iterators.add(a);
        if (b != null && b.hasNext()) iterators.add(b);
        for (Iterator<ByteBuffer> it : moreIterators) {
            if (it != null && it.hasNext()) iterators.add(it);
        }
        if (iterators.size() > 2) {
            return new MergeIterator<ByteBuffer>(iterators, BYTE_BUFFER_COMPARATOR);
        } else if (iterators.size() == 2) {
            return new MergeTwoIterator<>(a, b, BYTE_BUFFER_COMPARATOR);
        } else if (iterators.size() == 1) {
            return iterators.get(0);
        } else {
            return null;
        }
    }

}
