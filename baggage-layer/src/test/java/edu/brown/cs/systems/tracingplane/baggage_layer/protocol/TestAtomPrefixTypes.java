package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixTypes.AtomType;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixTypes.HeaderType;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixTypes.Level;

public class TestAtomPrefixTypes {

    /** Test that the first two bits only are used for atom type */
    @Test
    public void testAtomTypeBits() {
        for (AtomType type : AtomType.values()) {
            assertEquals(0, type.byteValue & 0x3F); // remove first two bits, check rest are zero
        }

    }

    /** Test that the middle four bits are only used for level */
    @Test
    public void testLevelBits() {
        for (Level level : Level.levels) {
            assertEquals(0, level.byteValue & 0xC3); // remove middle four bits, check rest are 0
        }
    }

    /** Test that the final two bits are only used for header type */
    @Test
    public void testHeaderTypeBits() {
        for (HeaderType type : HeaderType.values()) {
            assertEquals(0, type.byteValue & 0xFC); // remove final two bits, check rest are zero
        }
    }

    @Test
    public void testValidLevels() {
        for (int i = -10; i < 0; i++) {
            assertFalse(Level.isValidLevel(i));
            assertNull(Level.get(i));
        }
        for (int i = 0; i < Level.LEVELS; i++) {
            assertTrue(Level.isValidLevel(i));
            assertNotNull(Level.get(i));
        }
        for (int i = Level.LEVELS; i < Level.LEVELS + 10; i++) {
            assertFalse(Level.isValidLevel(i));
            assertNull(Level.get(i));
        }
    }

    @Test
    public void testLevelIdentity() {
        for (int i = 0; i < Level.LEVELS; i++) {
            assertEquals(Level.get(i), Level.fromByte(Level.get(i).byteValue));
            for (int j = 0; j < Level.LEVELS; j++) {
                if (i == j) {
                    assertTrue(Level.get(i).match(Level.get(j).byteValue));
                    assertTrue(Level.get(j).match(Level.get(i).byteValue));
                } else {
                    assertFalse(Level.get(i).match(Level.get(j).byteValue));
                    assertFalse(Level.get(j).match(Level.get(i).byteValue));
                }
            }
        }
    }

    @Test
    public void testHeaderTypeIdentity() {
        for (HeaderType a : HeaderType.values()) {
            for (HeaderType b : HeaderType.values()) {
                if (a == b) {
                    assertTrue(a.match(b.byteValue));
                    assertTrue(b.match(a.byteValue));
                } else {
                    assertFalse(a.match(b.byteValue));
                    assertFalse(b.match(a.byteValue));
                }
            }
        }
    }

    @Test
    public void testValidAtomTypes() {
        assertNull(AtomType.fromByte((byte) 0));
        assertNotNull(AtomType.fromByte((byte) 64));
        assertNotNull(AtomType.fromByte((byte) 128));
        assertNull(AtomType.fromByte((byte) 192));
    }

    @Test
    public void testValidHeaderTypes() {
        assertNull(HeaderType.fromByte((byte) 0));
        assertNotNull(HeaderType.fromByte((byte) 1));
        assertNotNull(HeaderType.fromByte((byte) 2));
        assertNull(HeaderType.fromByte((byte) 3));
    }

    @Test
    public void testAllBytes() {
        for (int i = 0; i < 256; i++) {
            byte b = (byte) i;
            AtomType.fromByte(b);
            Level.fromByte(b);
            HeaderType.fromByte(b);
        }
    }

}
