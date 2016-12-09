package edu.brown.cs.systems.tracingplane.atom_layer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import edu.brown.cs.systems.tracingplane.atom_layer.impl.RawAtomLayerImpl;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;
import edu.brown.cs.systems.tracingplane.transit_layer.impl.NullTransitLayer;

public class TestDefaultTransitLayer {

    @Test
    public void testDefaultTransitLayer() {
        // The context layer is configured to be the default transit layer --
        // test that this is the case

        assertNotNull(Baggage.transit);
        assertFalse(Baggage.transit instanceof NullTransitLayer);
        assertTrue(Baggage.transit instanceof AtomTransitLayerImpl);

        AtomTransitLayerImpl<?> transit = (AtomTransitLayerImpl<?>) Baggage.transit;
        assertNotNull(transit.contextLayer);
        assertTrue(transit.contextLayer instanceof RawAtomLayerImpl);

        Baggage baggage = Baggage.newInstance();
        assertNull(baggage);

        assertEquals(transit.contextLayer, BaggageAtoms.atomLayer);
    }

}
