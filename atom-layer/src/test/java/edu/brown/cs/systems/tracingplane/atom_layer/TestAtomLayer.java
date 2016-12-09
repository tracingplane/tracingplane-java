package edu.brown.cs.systems.tracingplane.atom_layer;

import static org.junit.Assert.assertNull;
import org.junit.Test;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;

public class TestAtomLayer {

    @Test
    public void testNewInstance() {
        assertNull(Baggage.transit.newInstance());
    }

}
