package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

import org.junit.Test;

import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixTypes.AtomType;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixTypes.HeaderType;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixTypes.Level;
import junit.framework.TestCase;

public class TestAtomPrefixTypes extends TestCase {

	/**
	 * Test that the first two bits only are used for atom type
	 */
	@Test
	public void testAtomTypeBits() {
		for (AtomType type : AtomType.values()) {
			assertEquals(0, type.byteValue & 0x3F); // remove first two bits,
													// check rest are zero
		}

	}

	/**
	 * Test that the middle four bits are only used for level
	 */
	@Test
	public void testLevelBits() {
		for (Level level : Level.levels) {
			assertEquals(0, level.byteValue & 0xC3); // remove middle four bits,
														// check rest are 0
		}
	}

	/**
	 * Test that the final two bits are only used for header type
	 */
	@Test
	public void testHeaderTypeBits() {
		for (HeaderType type : HeaderType.values()) {
			assertEquals(0, type.byteValue & 0xFC); // remove final two bits,
													// check rest are zero
		}
	}

}
