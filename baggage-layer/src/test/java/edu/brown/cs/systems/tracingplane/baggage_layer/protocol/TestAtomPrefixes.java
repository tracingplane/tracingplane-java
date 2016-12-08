package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

import org.junit.Test;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixTypes.AtomType;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixTypes.Level;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.AtomPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.DataPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.IndexedHeaderPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.KeyedHeaderPrefix;
import junit.framework.TestCase;

public class TestAtomPrefixes extends TestCase {

    @Test
    public void testValidPrefixes() {
        assertTrue(DataPrefix.prefix().isValid());
        for (int level = 0; level < Level.LEVELS; level++) {
            assertTrue(IndexedHeaderPrefix.prefixFor(level).isValid());
        }
    }

    @Test
    public void testDataPrefix() {
        AtomPrefix prefix = DataPrefix.prefix();
        assertNotNull(prefix);
        assertFalse(prefix.isHeader());
        assertTrue(prefix.isValid());
        assertTrue(prefix.isData());
        for (int i = 0; i < 10; i++) {
            assertEquals(i + 1, prefix.level(i));
        }
        assertEquals(DataPrefix.atomType, AtomType.Data);
        assertEquals(prefix, AtomPrefixes.get(prefix.prefix));
    }

    @Test
    public void testIndexedHeaderPrefix() {
        for (int i = 0; i < Level.LEVELS; i++) {
            AtomPrefix prefix = IndexedHeaderPrefix.prefixFor(i);
            assertNotNull(prefix);
            assertTrue(prefix.isHeader());
            assertTrue(prefix.isValid());
            assertFalse(prefix.isData());
            for (int j = 0; j < Level.LEVELS; j++) {
                assertEquals(i, prefix.level(j));
            }
            assertEquals(prefix, AtomPrefixes.get(prefix.prefix));
        }

        for (int i = -10; i < 0; i++) {
            assertNull(IndexedHeaderPrefix.prefixFor(i));
        }
        for (int i = Level.LEVELS; i < Level.LEVELS + 10; i++) {
            assertNull(IndexedHeaderPrefix.prefixFor(i));
        }
    }

    @Test
    public void testKeyedHeaderPrefix() {
        for (int i = 0; i < Level.LEVELS; i++) {
            AtomPrefix prefix = KeyedHeaderPrefix.prefixFor(i);
            assertNotNull(prefix);
            assertTrue(prefix.isHeader());
            assertTrue(prefix.isValid());
            assertFalse(prefix.isData());
            for (int j = 0; j < Level.LEVELS; j++) {
                assertEquals(i, prefix.level(j));
            }
            assertEquals(prefix, AtomPrefixes.get(prefix.prefix));
        }

        for (int i = -10; i < 0; i++) {
            assertNull(KeyedHeaderPrefix.prefixFor(i));
        }
        for (int i = Level.LEVELS; i < Level.LEVELS + 10; i++) {
            assertNull(KeyedHeaderPrefix.prefixFor(i));
        }
    }

    @Test
    public void testCanGetAllPrefixes() {
        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
            assertNotNull(AtomPrefixes.get((byte) i));
        }
    }

}
