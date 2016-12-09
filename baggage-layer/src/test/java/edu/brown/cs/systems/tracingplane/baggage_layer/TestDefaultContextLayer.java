package edu.brown.cs.systems.tracingplane.baggage_layer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import edu.brown.cs.systems.tracingplane.atom_layer.AtomTransitLayerImpl;
import edu.brown.cs.systems.tracingplane.atom_layer.BaggageAtoms;
import edu.brown.cs.systems.tracingplane.atom_layer.impl.RawAtomLayerImpl;
import edu.brown.cs.systems.tracingplane.baggage_layer.impl.GenericBaggageLayer;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;
import edu.brown.cs.systems.tracingplane.transit_layer.impl.NullTransitLayer;

public class TestDefaultContextLayer {

    @Test
    public void testDefaultContextLayer() {
        assertNotNull(Baggage.transit);
        assertFalse(Baggage.transit instanceof NullTransitLayer);
        assertTrue(Baggage.transit instanceof AtomTransitLayerImpl);

        AtomTransitLayerImpl<?> transit = (AtomTransitLayerImpl<?>) Baggage.transit;
        assertNotNull(transit.contextLayer);
        assertFalse(transit.contextLayer instanceof RawAtomLayerImpl);
        assertTrue(transit.contextLayer instanceof GenericBaggageLayer);

        Baggage baggage = Baggage.newInstance();
        assertNull(baggage);

        assertEquals(transit.contextLayer, BaggageAtoms.atomLayer);
        assertEquals(transit.contextLayer, BaggageContents.baggageLayer);
    }

}
