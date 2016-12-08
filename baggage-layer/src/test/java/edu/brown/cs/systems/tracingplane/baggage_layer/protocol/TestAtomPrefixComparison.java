package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

import java.nio.ByteBuffer;
import org.junit.Test;
import edu.brown.cs.systems.tracingplane.atom_layer.BaggageAtoms;
import edu.brown.cs.systems.tracingplane.atom_layer.types.Lexicographic;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixTypes.Level;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.AtomPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.DataPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.IndexedHeaderPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.KeyedHeaderPrefix;
import junit.framework.TestCase;

/**
 * Tests that the protocol-specified comparisons between prefixes are valid
 */
public class TestAtomPrefixComparison extends TestCase {

    private static ByteBuffer wrap(byte b) {
        return ByteBuffer.wrap(new byte[] { b });
    }

    @Test
    public void testOverflowMarkerIsKing() {
        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
            ByteBuffer buf = wrap(AtomPrefixes.get((byte) i).prefix);

            assertEquals(-1, Lexicographic.compare(BaggageAtoms.OVERFLOW_MARKER, buf));
            assertEquals(1, Lexicographic.compare(buf, BaggageAtoms.OVERFLOW_MARKER));
        }
    }

    @Test
    public void testDataPrefixIsNearlyKing() {
        ByteBuffer prefixBufA = wrap(DataPrefix.prefix);
        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
            AtomPrefix prefix = AtomPrefixes.get((byte) i);
            if (!prefix.isValid()) {
                continue;
            }
            ByteBuffer prefixBufB = wrap(AtomPrefixes.get((byte) i).prefix);

            if (prefix == DataPrefix.prefix()) {
                assertEquals(0, Lexicographic.compare(prefixBufA, prefixBufB));
                assertEquals(0, Lexicographic.compare(prefixBufB, prefixBufA));
            } else {
                assertTrue(Lexicographic.compare(prefixBufA, prefixBufB) < 0);
                assertTrue(Lexicographic.compare(prefixBufB, prefixBufA) > 0);
            }

            ByteBuffer buf = ByteBuffer.wrap(new byte[] { prefix.prefix });

            assertTrue(Lexicographic.compare(BaggageAtoms.OVERFLOW_MARKER, buf) < 0);
            assertTrue(Lexicographic.compare(buf, BaggageAtoms.OVERFLOW_MARKER) > 0);
        }
    }

    @Test
    public void testIndexedHeadersBeatKeyedHeaders() {
        for (int i = 0; i < Level.LEVELS; i++) {
            IndexedHeaderPrefix prefixA = IndexedHeaderPrefix.prefixFor(i);
            KeyedHeaderPrefix prefixB = KeyedHeaderPrefix.prefixFor(i);

            ByteBuffer bufA = wrap(prefixA.prefix);
            ByteBuffer bufB = wrap(prefixB.prefix);

            assertEquals(0, Lexicographic.compare(bufA, bufA));
            assertEquals(0, Lexicographic.compare(bufB, bufB));
            assertTrue(0 > Lexicographic.compare(bufA, bufB));
            assertTrue(0 < Lexicographic.compare(bufB, bufA));
        }
    }

    @Test
    public void testHigherLevelsBeatLowerLevels() {
        for (int i = 0; i < Level.LEVELS; i++) {
            ByteBuffer a1 = wrap(IndexedHeaderPrefix.prefixFor(i).prefix);
            ByteBuffer b1 = wrap(KeyedHeaderPrefix.prefixFor(i).prefix);
            for (int j = 0; j < i; j++) {
                ByteBuffer a2 = wrap(IndexedHeaderPrefix.prefixFor(j).prefix);
                ByteBuffer b2 = wrap(KeyedHeaderPrefix.prefixFor(j).prefix);

                assertTrue(0 > Lexicographic.compare(a1, a2));
                assertTrue(0 > Lexicographic.compare(a1, b2));
                assertTrue(0 > Lexicographic.compare(b1, a2));
                assertTrue(0 > Lexicographic.compare(b1, b2));
            }
        }
    }

}
