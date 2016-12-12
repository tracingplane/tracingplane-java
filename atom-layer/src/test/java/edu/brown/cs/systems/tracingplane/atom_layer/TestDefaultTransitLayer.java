package edu.brown.cs.systems.tracingplane.atom_layer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import edu.brown.cs.systems.tracingplane.atom_layer.impl.RawAtomLayer;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;
import edu.brown.cs.systems.tracingplane.transit_layer.impl.NullTransitLayer;

public class TestDefaultTransitLayer {

    @Test
    public void testDefaultTransitLayer() {
        assertNotNull(Baggage.transit);
        assertFalse(Baggage.transit instanceof NullTransitLayer);
        assertTrue(Baggage.transit instanceof RawAtomLayer);

        assertNotNull(BaggageAtoms.atomLayer);
        assertTrue(BaggageAtoms.atomLayer instanceof RawAtomLayer);

        Baggage baggage = Baggage.newInstance();
        assertNull(baggage);

        assertEquals(Baggage.transit, BaggageAtoms.atomLayer);
    }

}
