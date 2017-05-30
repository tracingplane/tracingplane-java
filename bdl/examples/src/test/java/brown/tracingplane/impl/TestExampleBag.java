package brown.tracingplane.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import org.junit.Test;
import brown.tracingplane.BaggageContext;
import brown.tracingplane.baggageprotocol.BagKey;
import brown.tracingplane.bdl.BDLUtils;
import brown.tracingplane.bdl.SpecialTypes.Counter;
import brown.tracingplane.bdl.examples.ExampleBag;
import brown.tracingplane.bdl.examples.SimpleBag2;
import brown.tracingplane.bdl.examples.SimpleStruct1;

public class TestExampleBag {

    static {
        BaggageHandlerRegistry.add(BagKey.indexed(10), ExampleBag.Handler.instance);
    } 
    
    private static final BDLContextProvider provider = new BDLContextProvider();
    
    @Test
    public void testStruct() throws Exception {
        SimpleStruct1.Handler handler = SimpleStruct1.Handler.instance;
        
        SimpleStruct1 struct = new SimpleStruct1();
        assertEquals(2, handler.serializedSize(struct));
        
        struct.integerField = 1000;
        assertEquals(3, handler.serializedSize(struct));
        
        struct.stringField = "hello";
        assertEquals(8, handler.serializedSize(struct));
        
        ByteBuffer buf = ByteBuffer.allocate(8);
        handler.writeTo(buf, struct);
        buf.flip();
        SimpleStruct1 struct2 = handler.readFrom(buf);
        assertEquals(struct, struct2);
        
        SimpleStruct1 struct3 = new SimpleStruct1();
        assertFalse(struct3.equals(struct));
        
        struct3.integerField = 100;
        assertFalse(struct3.equals(struct));
        
        struct3.stringField = "hello";
        assertFalse(struct3.equals(struct));
        
        struct3.integerField = 1000;
        assertTrue(struct3.equals(struct));
        
        ExampleBag bag = new ExampleBag();
        bag.structsetfield = new HashSet<>();
        bag.structsetfield.add(struct);
        bag.structsetfield.add(struct2);
        bag.structsetfield.add(struct3);
        
        assertEquals(1, bag.structsetfield.size());
    }

    @Test
    public void testExampleBag() {

        assertNull(ExampleBag.get());

        ExampleBag b1 = new ExampleBag();
        b1.int32field = 10;

        b1.bagMap = new HashMap<>();

        SimpleBag2 b2 = new SimpleBag2();
        b2.secondField = "boshank";
        b1.bagMap.put("jon", b2);

        BaggageContext baggage = ExampleBag.setIn(null, b1);

        assertEquals(b1, ExampleBag.getFrom(baggage));
    }
    
    static List<ByteBuffer> atoms(BaggageContext baggage) {
        if (baggage != null && baggage instanceof BDLContext) {
            return ((BDLContext) baggage).serialize().atoms();
        } else {
            return null;
        }
    }

    @Test
    public void testTaint() {

        BaggageContext baggage = ExampleBag.setIn(null, new ExampleBag());
        
        assertNotNull(baggage);
        
        List<ByteBuffer> atoms = atoms(baggage);
        assertEquals(0, atoms.size());
        
        byte[] byteRepr = provider.serialize((BDLContext) baggage);
        assertEquals(0, byteRepr.length);
    }

    @Test
    public void testTaint2() {

        ExampleBag eb = new ExampleBag();
        eb.sampled = true;
        BaggageContext baggage = ExampleBag.setIn(null, eb);
        
        assertNotNull(baggage);
        
        List<ByteBuffer> atoms = atoms(baggage);
        assertEquals(3, atoms.size());
        
        byte[] byteRepr = provider.serialize((BDLContext) baggage);
        assertEquals(9, byteRepr.length);
        
    }
    
    @Test
    public void testCounter() {
        ExampleBag eb1 = new ExampleBag();
        eb1.c = Counter.newInstance();
        eb1.c.increment(174);
        
        BaggageContext sourceBaggage = ExampleBag.setIn(null, eb1);
        byte[] bytes = provider.serialize((BDLContext) sourceBaggage);
        BaggageContext b = provider.deserialize(bytes, 0, bytes.length);
        assertEquals(174, ExampleBag.getFrom(b).c.getValue());
        
    }

    @Test
    public void testTaint3() {

        ExampleBag eb1 = new ExampleBag();
        eb1.sampled = true;
        
        ExampleBag eb2 = new ExampleBag();
        eb2.sampled = false;

        BaggageContext b1 = ExampleBag.setIn(null, eb1);
        BaggageContext b2 = ExampleBag.setIn(null, eb2);
        
        BaggageContext bj = provider.join((BDLContext) b1, (BDLContext) b2);
        
        ExampleBag ebj = ExampleBag.getFrom(bj);
        assertTrue(ebj.sampled);
    }
    
    @Test
    public void testCounterMap() {

        Counter c1 = Counter.newInstance();
        for (int i = 0; i < 3; i++) c1.increment();
        
        Counter c2a = Counter.newInstance();
        for (int i = 0; i < 5; i++) c2a.increment();
        
        Counter c2b = Counter.newInstance();
        for (int i = 0; i < 7; i++) c2b.increment();

        ExampleBag eb1 = new ExampleBag();
        eb1.countermap = new TreeMap<>();
        eb1.countermap.put("c1", c1);
        eb1.countermap.put("c2", c2a);

        ExampleBag eb2 = new ExampleBag();
        eb2.countermap = new TreeMap<>();
        eb2.countermap.put("c1", c1);
        eb2.countermap.put("c2", c2b);

        BaggageContext b1 = ExampleBag.setIn(null, eb1);
        BaggageContext b2 = ExampleBag.setIn(null, eb2);
        
        BaggageContext b3 = provider.branch((BDLContext) b1);
        BaggageContext b4 = provider.join((BDLContext) b1, (BDLContext) b2);
    }
    
    @Test
    public void testCounterCompact1a() {
        Counter c1 = Counter.newInstance();
        for (int i = 0; i < 3; i++) c1.increment();
        
        ExampleBag eb1 = new ExampleBag();
        eb1.countermap = new TreeMap<>();
        eb1.countermap.put("c1", c1);
        
        BaggageContext b1 = ExampleBag.setIn(null, eb1);
        
        BaggageContext b2 = provider.branch((BDLContext) b1);
        ExampleBag.getFrom(b2).countermap.get("c1").increment(5);
        
        BaggageContext bjoined = provider.join((BDLContext) b1, (BDLContext) b2);
        
        assertEquals(28, provider.serialize((BDLContext) bjoined).length);
        assertEquals(8, ExampleBag.getFrom(bjoined).countermap.get("c1").getValue());
    }
    
    @Test
    public void testCounterCompact1b() {
        Counter c1 = Counter.newInstance();
        for (int i = 0; i < 3; i++) c1.increment();
        
        ExampleBag eb1 = new ExampleBag();
        eb1.countermap = new TreeMap<>();
        eb1.countermap.put("c1", c1);
        
        BaggageContext b1 = ExampleBag.setIn(null, eb1);
        
        BaggageContext b2 = provider.branch((BDLContext) b1);
        ExampleBag.getFrom(b2).countermap.get("c1").increment(5);
        
        BaggageContext bjoined = provider.compact((BDLContext) b1, (BDLContext) b2);
        
        assertEquals(19, provider.serialize((BDLContext) bjoined).length);
        assertEquals(8, ExampleBag.getFrom(bjoined).countermap.get("c1").getValue());
    }
    
    @Test
    public void testCounterCompact2a() {
        Counter c1 = Counter.newInstance();
        for (int i = 0; i < 3; i++) c1.increment();
        
        ExampleBag eb1 = new ExampleBag();
        eb1.countermap = new TreeMap<>();
        eb1.countermap.put("c1", c1);
        
        BaggageContext b1 = ExampleBag.setIn(null, eb1);
        
        BaggageContext b2 = provider.branch((BDLContext) b1);
        ExampleBag.getFrom(b2).countermap.get("c1").increment(5);
        
        BaggageContext b3 = provider.branch((BDLContext) b2);
        ExampleBag.getFrom(b3).countermap.get("c1").increment(100);
        
        BaggageContext b2joined = provider.join((BDLContext) b2, (BDLContext) b3);
        assertEquals(37, provider.serialize((BDLContext) b2joined).length);
        assertEquals(108, ExampleBag.getFrom(b2joined).countermap.get("c1").getValue());
        
        ExampleBag.getFrom(b2joined).countermap.get("c1").increment(7);
        
        
        BaggageContext bjoined = provider.join((BDLContext) b1, (BDLContext) b2joined);
        assertEquals(37, provider.serialize((BDLContext) bjoined).length);
        assertEquals(115, ExampleBag.getFrom(bjoined).countermap.get("c1").getValue());
    }
    
    @Test
    public void testCounterCompact2b() {
        Counter c1 = Counter.newInstance();
        for (int i = 0; i < 3; i++) c1.increment();
        
        ExampleBag eb1 = new ExampleBag();
        eb1.countermap = new TreeMap<>();
        eb1.countermap.put("c1", c1);
        
        BaggageContext b1 = ExampleBag.setIn(null, eb1);
        
        BaggageContext b2 = provider.branch((BDLContext) b1);
        ExampleBag.getFrom(b2).countermap.get("c1").increment(5);
        
        BaggageContext b3 = provider.branch((BDLContext) b2);
        ExampleBag.getFrom(b3).countermap.get("c1").increment(100);
        
        BaggageContext b2joined = provider.join((BDLContext) b2, (BDLContext) b3);
        assertEquals(37, provider.serialize((BDLContext) b2joined).length);
        assertEquals(108, ExampleBag.getFrom(b2joined).countermap.get("c1").getValue());
        
        ExampleBag.getFrom(b2joined).countermap.get("c1").increment(7);
        
        BDLUtils.is_compaction.set(true);
        BaggageContext bjoined = provider.join((BDLContext) b1, (BDLContext) b2joined);
        BDLUtils.is_compaction.set(false);
        
        assertEquals(19, provider.serialize((BDLContext) bjoined).length);
        assertEquals(115, ExampleBag.getFrom(bjoined).countermap.get("c1").getValue());
    }

}
