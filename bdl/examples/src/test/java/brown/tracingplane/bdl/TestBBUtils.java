package brown.tracingplane.bdl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.HashSet;
import java.util.Map;
import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import brown.tracingplane.BaggageContext;
import brown.tracingplane.baggageprotocol.BagKey;
import brown.tracingplane.bdl.examples.ExampleBag;
import brown.tracingplane.bdl.examples.SimpleBag;
import brown.tracingplane.impl.BDLContext;
import brown.tracingplane.impl.BDLContextProvider;
import brown.tracingplane.impl.BDLContextProviderFactory;
import brown.tracingplane.impl.BDLContextUtils;
import brown.tracingplane.impl.BaggageHandlerRegistry;

public class TestBBUtils {

    static {
        BasicConfigurator.configure();

        BaggageHandlerRegistry.add(BagKey.indexed(10), ExampleBag.Handler.instance);
        BaggageHandlerRegistry.add(BagKey.indexed(3), SimpleBag.Handler.instance);
    }
    
    BDLContextProvider provider = (BDLContextProvider) new BDLContextProviderFactory().provider();

    @Test
    public void testSerializedSize() {
        assertEquals(0, BDLContextUtils.serializedSize(null));
    }

    @Test
    public void testSerializedSize2() {
        assertEquals(0, BDLContextUtils.serializedSize(ExampleBag.setIn(null, new ExampleBag())));
    }

    @Test
    public void testSerializedSize3() {
        ExampleBag eb = new ExampleBag();
        eb.boolfield = false;
        assertEquals(9, provider.serialize((BDLContext) ExampleBag.setIn(null, eb)).length);
        assertEquals(9, BDLContextUtils.serializedSize(ExampleBag.setIn(null, eb)));
        assertEquals(6, BDLContextUtils.serializedSizeOfBag(eb));

        Map<String, String> summary = BDLContextUtils.getSizeSummary(ExampleBag.setIn(null, eb));
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
        assertEquals(9, provider.serialize((BDLContext) ExampleBag.setIn(null, eb)).length);
        assertEquals(9, BDLContextUtils.serializedSize(ExampleBag.setIn(null, eb)));
        assertEquals(6, BDLContextUtils.serializedSizeOfBag(eb));

        SimpleBag sb = new SimpleBag();
        sb.ids = new HashSet<>();
        sb.ids.add(5L);
        assertEquals(16, provider.serialize((BDLContext) SimpleBag.setIn(null, sb)).length);
        assertEquals(16, BDLContextUtils.serializedSize(SimpleBag.setIn(null, sb)));
        assertEquals(13, BDLContextUtils.serializedSizeOfBag(sb));

        BaggageContext baggage = ExampleBag.setIn(SimpleBag.setIn(null, sb), eb);
        Map<String, String> summary = BDLContextUtils.getSizeSummary(baggage);
        assertEquals(3, summary.size());
        assertTrue(summary.containsKey("ExampleBag"));
        assertTrue(summary.containsKey("SimpleBag"));
        assertTrue(summary.containsKey("BaggageTotalSize"));
        assertEquals("9", summary.get("ExampleBag"));
        assertEquals("16", summary.get("SimpleBag"));
        assertEquals("25", summary.get("BaggageTotalSize"));

        byte[] serialized = provider.serialize((BDLContext) baggage, 20);
        assertEquals(20, serialized.length);
        baggage = provider.deserialize(serialized, 0, serialized.length);
        assertEquals(20, BDLContextUtils.serializedSize(baggage));
        assertEquals(13, BDLContextUtils.serializedSizeOfBag(SimpleBag.getFrom(baggage)));

        summary = BDLContextUtils.getSizeSummary(baggage);
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
