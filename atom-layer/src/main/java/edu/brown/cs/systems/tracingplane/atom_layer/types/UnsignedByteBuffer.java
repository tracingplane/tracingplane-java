package edu.brown.cs.systems.tracingplane.atom_layer.types;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Comparator;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.primitives.Longs;
import com.google.common.primitives.UnsignedBytes;
import com.google.common.primitives.UnsignedLongs;
import sun.misc.Unsafe;

/** Adapted from Guava's com.google.common.primitives.UnsignedBytes to provide comparators for unsigned ByteBuffers */
public final class UnsignedByteBuffer {
    private UnsignedByteBuffer() {}

    private static final int UNSIGNED_MASK = 0xFF;

    public static int compare(ByteBuffer a, ByteBuffer b) {
        return lexicographicalComparator().compare(a, b);
    }

    /**
     * Returns a comparator that compares two {@code java.nio.ByteBuffer}s
     * <a href="http://en.wikipedia.org/wiki/Lexicographical_order">lexicographically</a>. That is, it compares, using
     * {@link UnsignedBytes#compare(byte, byte)}), the first pair of values that follow any common prefix, or when one array is a
     * prefix of the other, treats the shorter array as the lesser. For example,
     * {@code [] < [0x01] < [0x01, 0x7F] < [0x01, 0x80] < [0x02]}. Values are treated as unsigned.
     *
     * <p>
     * The returned comparator is inconsistent with {@link Object#equals(Object)} (since arrays support only identity
     * equality), but it is consistent with {@link java.util.Arrays#equals(byte[], byte[])}.
     *
     * @since 2.0
     */
    public static Comparator<ByteBuffer> lexicographicalComparator() {
        return LexicographicalComparatorHolder.BEST_COMPARATOR;
    }

    @VisibleForTesting
    static Comparator<ByteBuffer> lexicographicalComparatorJavaImpl() {
        return LexicographicalComparatorHolder.PureJavaComparator.INSTANCE;
    }

    /**
     * Provides a lexicographical comparator implementation; either a Java implementation or a faster implementation
     * based on {@link Unsafe}.
     *
     * <p>
     * Uses reflection to gracefully fall back to the Java implementation if {@code Unsafe} isn't available.
     */
    @VisibleForTesting
    static class LexicographicalComparatorHolder {
        static final String UNSAFE_COMPARATOR_NAME =
                LexicographicalComparatorHolder.class.getName() + "$UnsafeComparator";

        static final Comparator<ByteBuffer> BEST_COMPARATOR = getBestComparator();

        @VisibleForTesting
        enum UnsafeComparator implements Comparator<ByteBuffer> {
                                                                 INSTANCE;

            static final boolean BIG_ENDIAN = ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN);

            /* The following static final fields exist for performance reasons. In UnsignedBytesBenchmark, accessing the
             * following objects via static final fields is the fastest (more than twice as fast as the Java
             * implementation, vs ~1.5x with non-final static fields, on x86_32) under the Hotspot server compiler. The
             * reason is obviously that the non-final fields need to be reloaded inside the loop. And, no, defining
             * (final or not) local variables out of the loop still isn't as good because the null check on the
             * theUnsafe object remains inside the loop and BYTE_ARRAY_BASE_OFFSET doesn't get constant-folded. The
             * compiler can treat static final fields as compile-time constants and can constant-fold them while (final
             * or not) local variables are run time values. */

            static final Unsafe theUnsafe;

            /** The offset to the first element in a byte array. */
            static final int BYTE_ARRAY_BASE_OFFSET;

            static {
                theUnsafe = getUnsafe();

                BYTE_ARRAY_BASE_OFFSET = theUnsafe.arrayBaseOffset(byte[].class);

                // sanity check - this should never fail
                if (theUnsafe.arrayIndexScale(byte[].class) != 1) {
                    throw new AssertionError();
                }
            }

            /**
             * Returns a sun.misc.Unsafe. Suitable for use in a 3rd party package. Replace with a simple call to
             * Unsafe.getUnsafe when integrating into a jdk.
             *
             * @return a sun.misc.Unsafe
             */
            private static sun.misc.Unsafe getUnsafe() {
                try {
                    return sun.misc.Unsafe.getUnsafe();
                } catch (SecurityException e) {
                    // that's okay; try reflection instead
                }
                try {
                    return java.security.AccessController.doPrivileged(new java.security.PrivilegedExceptionAction<sun.misc.Unsafe>() {
                        @Override
                        public sun.misc.Unsafe run() throws Exception {
                            Class<sun.misc.Unsafe> k = sun.misc.Unsafe.class;
                            for (java.lang.reflect.Field f : k.getDeclaredFields()) {
                                f.setAccessible(true);
                                Object x = f.get(null);
                                if (k.isInstance(x)) {
                                    return k.cast(x);
                                }
                            }
                            throw new NoSuchFieldError("the Unsafe");
                        }
                    });
                } catch (java.security.PrivilegedActionException e) {
                    throw new RuntimeException("Could not initialize intrinsics", e.getCause());
                }
            }

            @Override
            public int compare(ByteBuffer left, ByteBuffer right) {
                if (!left.hasArray() || !right.hasArray()) {
                    // TODO: might nonetheless be faster to copy bytes out of buffers in chunks of 8
                    return UnsignedByteBuffer.lexicographicalComparatorJavaImpl().compare(left, right);
                }
                int initialLeftPosition = left.position();
                int initialRightPosition = right.position();

                try {
                    int minLength = Math.min(left.remaining(), right.remaining());
                    int minWords = minLength / Longs.BYTES;

                    byte[] leftArray = left.array();
                    byte[] rightArray = right.array();
                    int leftOffset = left.arrayOffset() + initialLeftPosition;
                    int rightOffset = right.arrayOffset() + initialRightPosition;

                    /* Compare 8 bytes at a time. Benchmarking shows comparing 8 bytes at a time is no slower than
                     * comparing 4 bytes at a time even on 32-bit. On the other hand, it is substantially faster on
                     * 64-bit. */
                    for (int i = 0; i < minWords * Longs.BYTES; i += Longs.BYTES) {
                        long lw = theUnsafe.getLong(leftArray, BYTE_ARRAY_BASE_OFFSET + leftOffset + (long) i);
                        long rw = theUnsafe.getLong(rightArray, BYTE_ARRAY_BASE_OFFSET + rightOffset + (long) i);
                        if (lw != rw) {
                            if (BIG_ENDIAN) {
                                return UnsignedLongs.compare(lw, rw);
                            }

                            /* We want to compare only the first index where left[index] != right[index]. This
                             * corresponds to the least significant nonzero byte in lw ^ rw, since lw and rw are
                             * little-endian. Long.numberOfTrailingZeros(diff) tells us the least significant nonzero
                             * bit, and zeroing out the first three bits of L.nTZ gives us the shift to get that least
                             * significant nonzero byte. */
                            int n = Long.numberOfTrailingZeros(lw ^ rw) & ~0x7;
                            return ((int) ((lw >>> n) & UNSIGNED_MASK)) - ((int) ((rw >>> n) & UNSIGNED_MASK));
                        }
                    }

                    // The epilogue to cover the last (minLength % 8) elements.
                    for (int i = minWords * Longs.BYTES; i < minLength; i++) {
                        int result = UnsignedBytes.compare(leftArray[leftOffset + i], rightArray[rightOffset + i]);
                        if (result != 0) {
                            return result;
                        }
                    }
                    return left.remaining() - right.remaining();
                } finally {
                    left.position(initialLeftPosition);
                    right.position(initialRightPosition);
                }
            }

            @Override
            public String toString() {
                return "UnsignedBytes.lexicographicalComparator() (sun.misc.Unsafe version)";
            }
        }

        enum PureJavaComparator implements Comparator<ByteBuffer> {
                                                                   INSTANCE;

            @Override
            public int compare(ByteBuffer left, ByteBuffer right) {
                int initialLeftPosition = left.position();
                int initialRightPosition = right.position();
                try {
                    int minLength = Math.min(left.remaining(), right.remaining());
                    for (int i = 0; i < minLength; i++) {
                        int result = UnsignedBytes.compare(left.get(), right.get());
                        if (result != 0) {
                            return result;
                        }
                    }
                    return left.remaining() - right.remaining();
                } finally {
                    left.position(initialLeftPosition);
                    right.position(initialRightPosition);
                }
            }

            @Override
            public String toString() {
                return "UnsignedBytes.lexicographicalComparator() (pure Java version)";
            }
        }

        /** @return the Unsafe-using Comparator, or falls back to the pure-Java implementation if unable to do so. */
        static Comparator<ByteBuffer> getBestComparator() {
            try {
                Class<?> theClass = Class.forName(UNSAFE_COMPARATOR_NAME);

                // yes, UnsafeComparator does implement Comparator<byte[]>
                @SuppressWarnings("unchecked")
                Comparator<ByteBuffer> comparator = (Comparator<ByteBuffer>) theClass.getEnumConstants()[0];
                return comparator;
            } catch (Throwable t) { // ensure we really catch *everything*
                return lexicographicalComparatorJavaImpl();
            }
        }
    }
}