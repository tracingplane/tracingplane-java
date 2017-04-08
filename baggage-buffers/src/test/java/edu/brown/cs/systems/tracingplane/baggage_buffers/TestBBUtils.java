package edu.brown.cs.systems.tracingplane.baggage_buffers;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class TestBBUtils {
    
    @Test
    public void testSerializedSize() {
        assertEquals(0, BaggageBuffersUtils.serializedSize(null));
    }

}
