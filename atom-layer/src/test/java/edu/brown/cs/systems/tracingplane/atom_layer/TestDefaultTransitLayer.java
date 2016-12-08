package edu.brown.cs.systems.tracingplane.atom_layer;

import org.junit.Test;
import edu.brown.cs.systems.tracingplane.atom_layer.impl.RawAtomLayerImpl;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;
import edu.brown.cs.systems.tracingplane.transit_layer.impl.NullTransitLayer;
import junit.framework.TestCase;

public class TestDefaultTransitLayer extends TestCase {

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
