package edu.brown.cs.systems.tracingplane.baggage_layer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import edu.brown.cs.systems.tracingplane.atom_layer.BaggageAtoms;
import edu.brown.cs.systems.tracingplane.atom_layer.impl.RawAtomLayer;
import edu.brown.cs.systems.tracingplane.baggage_layer.impl.GenericBaggageLayer;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;
import edu.brown.cs.systems.tracingplane.transit_layer.impl.NullTransitLayer;

public class TestDefaultContextLayer {

    @Test
    public void testDefaultContextLayer() {
        assertNotNull(Baggage.transit);
        assertFalse(Baggage.transit instanceof NullTransitLayer);
        assertFalse(Baggage.transit instanceof RawAtomLayer);
        assertTrue(Baggage.transit instanceof GenericBaggageLayer);

        assertNotNull(BaggageAtoms.atomLayer);
        assertFalse(BaggageAtoms.atomLayer instanceof RawAtomLayer);
        assertTrue(BaggageAtoms.atomLayer instanceof GenericBaggageLayer);

        assertNotNull(BaggageContents.baggageLayer);
        assertTrue(BaggageContents.baggageLayer instanceof GenericBaggageLayer);

        Baggage baggage = Baggage.newInstance();
        assertNull(baggage);
    }

}
