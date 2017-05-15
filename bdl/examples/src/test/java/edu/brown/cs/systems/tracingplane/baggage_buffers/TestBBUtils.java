package edu.brown.cs.systems.tracingplane.baggage_buffers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.HashSet;
import java.util.Map;
import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import edu.brown.cs.systems.baggage_buffers.gen.example.ExampleBag;
import edu.brown.cs.systems.baggage_buffers.gen.example.SimpleBag;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;

public class TestBBUtils {

    static {
        BasicConfigurator.configure();

        Registrations.register(BagKey.indexed(10), ExampleBag.Handler.instance);
        Registrations.register(BagKey.indexed(3), SimpleBag.Handler.instance);
    }
    
    @Test
    public void testSerializedSize() {
        assertEquals(0, BaggageBuffersUtils.serializedSize(null));
    }
    
    @Test
    public void testSerializedSize2() {
        assertEquals(0, BaggageBuffersUtils.serializedSize(ExampleBag.setIn(null, new ExampleBag())));
    }
    
    @Test
    public void testSerializedSize3() {
        ExampleBag eb = new ExampleBag();
        eb.boolfield = false;
        assertEquals(9, Baggage.serialize(ExampleBag.setIn(null, eb)).length);
        assertEquals(9, BaggageBuffersUtils.serializedSize(ExampleBag.setIn(null, eb)));
        assertEquals(6, BaggageBuffersUtils.serializedSizeOfBag(eb));
        
        Map<String, String> summary = BaggageBuffersUtils.getSizeSummary(ExampleBag.setIn(null, eb));
        assertEquals(2, summary.size());
        assertTrue(summary.containsKey("ExampleBag"));
        assertTrue(summary.containsKey("BaggageTotalSize"));
        assertEquals("9", summary.get("ExampleBag"));
        assertEquals("9", summary.get("BaggageTotalSize"));
    }
    
    @Test
    public void testSerializedSize4() {
        ExampleBag eb = new ExampleBag();
        eb.boolfield = false;
        assertEquals(9, Baggage.serialize(ExampleBag.setIn(null, eb)).length);
        assertEquals(9, BaggageBuffersUtils.serializedSize(ExampleBag.setIn(null, eb)));
        assertEquals(6, BaggageBuffersUtils.serializedSizeOfBag(eb));
        
        SimpleBag sb = new SimpleBag();
        sb.ids = new HashSet<>();
        sb.ids.add(5L);
        assertEquals(16, Baggage.serialize(SimpleBag.setIn(null, sb)).length);
        assertEquals(16, BaggageBuffersUtils.serializedSize(SimpleBag.setIn(null, sb)));
        assertEquals(13, BaggageBuffersUtils.serializedSizeOfBag(sb));
        
        Baggage baggage = ExampleBag.setIn(SimpleBag.setIn(null, sb), eb);
        Map<String, String> summary = BaggageBuffersUtils.getSizeSummary(baggage);
        assertEquals(3, summary.size());
        assertTrue(summary.containsKey("ExampleBag"));
        assertTrue(summary.containsKey("SimpleBag"));
        assertTrue(summary.containsKey("BaggageTotalSize"));
        assertEquals("9", summary.get("ExampleBag"));
        assertEquals("16", summary.get("SimpleBag"));
        assertEquals("25", summary.get("BaggageTotalSize"));
        
        byte[] serialized = Baggage.serialize(baggage, 20);
        assertEquals(20, serialized.length);
        baggage = Baggage.deserialize(serialized);
        assertEquals(20, BaggageBuffersUtils.serializedSize(baggage));
        assertEquals(13, BaggageBuffersUtils.serializedSizeOfBag(SimpleBag.getFrom(baggage)));

        summary = BaggageBuffersUtils.getSizeSummary(baggage);
        assertEquals(4, summary.size());
        assertTrue(summary.containsKey("ExampleBag"));
        assertTrue(summary.containsKey("SimpleBag"));
        assertTrue(summary.containsKey("OverflowAtoms"));
        assertTrue(summary.containsKey("BaggageTotalSize"));
        assertEquals("16", summary.get("SimpleBag"));
        assertEquals("4", summary.get("ExampleBag"));
        assertEquals("20", summary.get("BaggageTotalSize"));
        assertEquals("F80A,", summary.get("OverflowAtoms"));
    }

}
