package brown.tracingplane.baggageprotocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import brown.tracingplane.baggageprotocol.AtomPrefixTypes.AtomType;
import brown.tracingplane.baggageprotocol.AtomPrefixTypes.Level;
import brown.tracingplane.baggageprotocol.AtomPrefixes.AtomPrefix;
import brown.tracingplane.baggageprotocol.AtomPrefixes.DataPrefix;
import brown.tracingplane.baggageprotocol.AtomPrefixes.IndexedHeaderPrefix;
import brown.tracingplane.baggageprotocol.AtomPrefixes.KeyedHeaderPrefix;

public class TestAtomPrefixes {

    @Test
    public void testValidPrefixes() {
        assertTrue(DataPrefix.prefix().isValid());
        for (BagOptions options : BagOptions.values()) {
            for (int level = 0; level < Level.LEVELS; level++) {
                assertTrue(IndexedHeaderPrefix.prefixFor(level, options).isValid());
            }
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
        for (BagOptions options : BagOptions.values()) {
            for (int i = 0; i < Level.LEVELS; i++) {
                AtomPrefix prefix = IndexedHeaderPrefix.prefixFor(i, options);
                assertNotNull(prefix);
                assertTrue(prefix.isHeader());
                assertTrue(prefix.isValid());
                assertFalse(prefix.isData());
                for (int j = 0; j < Level.LEVELS; j++) {
                    assertEquals(i, prefix.level(j));
                }
                assertEquals(prefix, AtomPrefixes.get(prefix.prefix));
            }
        }

        for (BagOptions options : BagOptions.values()) {
            for (int i = -10; i < 0; i++) {
                assertNull(IndexedHeaderPrefix.prefixFor(i, options));
            }
            for (int i = Level.LEVELS; i < Level.LEVELS + 10; i++) {
                assertNull(IndexedHeaderPrefix.prefixFor(i, options));
            }
        }
    }

    @Test
    public void testKeyedHeaderPrefix() {
        for (BagOptions options : BagOptions.values()) {
            for (int i = 0; i < Level.LEVELS; i++) {
                AtomPrefix prefix = KeyedHeaderPrefix.prefixFor(i, options);
                assertNotNull(prefix);
                assertTrue(prefix.isHeader());
                assertTrue(prefix.isValid());
                assertFalse(prefix.isData());
                for (int j = 0; j < Level.LEVELS; j++) {
                    assertEquals(i, prefix.level(j));
                }
                assertEquals(prefix, AtomPrefixes.get(prefix.prefix));
            }
        }

        for (BagOptions options : BagOptions.values()) {
            for (int i = -10; i < 0; i++) {
                assertNull(KeyedHeaderPrefix.prefixFor(i, options));
            }
            for (int i = Level.LEVELS; i < Level.LEVELS + 10; i++) {
                assertNull(KeyedHeaderPrefix.prefixFor(i, options));
            }
        }
    }

    @Test
    public void testCanGetAllPrefixes() {
        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
            assertNotNull(AtomPrefixes.get((byte) i));
        }
    }

}
