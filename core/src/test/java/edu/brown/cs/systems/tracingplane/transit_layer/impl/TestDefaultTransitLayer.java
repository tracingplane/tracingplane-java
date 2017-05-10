package edu.brown.cs.systems.tracingplane.transit_layer.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;

public class TestDefaultTransitLayer {

    @Test
    public void testNullTransitLayerImpl() {
        assertNotNull(Baggage.transit);
        assertTrue(Baggage.transit instanceof NullTransitLayer);

        Baggage baggage = Baggage.newInstance();
        assertNotNull(baggage);
        assertEquals(baggage, NullBaggage.INSTANCE);
    }

}
