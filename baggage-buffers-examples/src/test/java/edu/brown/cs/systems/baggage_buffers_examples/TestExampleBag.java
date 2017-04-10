package edu.brown.cs.systems.baggage_buffers_examples;

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
import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import edu.brown.cs.systems.baggage_buffers.gen.example.ExampleBag;
import edu.brown.cs.systems.baggage_buffers.gen.example.SimpleBag2;
import edu.brown.cs.systems.baggage_buffers.gen.example.SimpleStruct1;
import edu.brown.cs.systems.baggage_buffers.gen.example.SimpleStruct1.Handler;
import edu.brown.cs.systems.tracingplane.atom_layer.BaggageAtoms;
import edu.brown.cs.systems.tracingplane.atom_layer.types.TypeUtils;
import edu.brown.cs.systems.tracingplane.baggage_buffers.BaggageBuffers;
import edu.brown.cs.systems.tracingplane.baggage_buffers.Registrations;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.SpecialTypes.Counter;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;

public class TestExampleBag {

    static {
        BasicConfigurator.configure();

        Registrations.register(BagKey.indexed(10), ExampleBag.Handler.instance);
    }
    
    @Test
    public void testStruct() throws Exception {
        Handler handler = SimpleStruct1.Handler.instance;
        
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

        System.out.println(b1);

        ExampleBag.set(b1);

        assertEquals(b1, ExampleBag.get());

        System.out.println(TypeUtils.toHexString(Baggage.serialize()));

    }

    @Test
    public void testTaint() {

        Baggage baggage = ExampleBag.setIn(null, new ExampleBag());
        
        assertNotNull(baggage);
        
        List<ByteBuffer> atoms = BaggageAtoms.atoms((BaggageAtoms) baggage);
        assertEquals(0, atoms.size());
        
        byte[] byteRepr = Baggage.serialize(baggage);
        assertEquals(0, byteRepr.length);
        
        
        
    }

    @Test
    public void testTaint2() {

        ExampleBag eb = new ExampleBag();
        eb.sampled = true;
        Baggage baggage = ExampleBag.setIn(null, eb);
        
        assertNotNull(baggage);
        
        List<ByteBuffer> atoms = BaggageAtoms.atoms((BaggageAtoms) baggage);
        assertEquals(3, atoms.size());
        
        byte[] byteRepr = Baggage.serialize(baggage);
        assertEquals(9, byteRepr.length);
        
    }
    
    @Test
    public void testCounter() {
        ExampleBag eb1 = new ExampleBag();
        eb1.c = Counter.newInstance();
        eb1.c.increment(174);
        
        Baggage sourceBaggage = ExampleBag.setIn(null, eb1);
        byte[] bytes = Baggage.serialize(sourceBaggage);
        Baggage b = Baggage.deserialize(bytes);
        System.out.println(b);
        
        assertEquals(174, ExampleBag.getFrom(b).c.getValue());
        
        
    }

    @Test
    public void testTaint3() {

        ExampleBag eb1 = new ExampleBag();
        eb1.sampled = true;
        
        ExampleBag eb2 = new ExampleBag();
        eb2.sampled = false;

        Baggage b1 = ExampleBag.setIn(null, eb1);
        Baggage b2 = ExampleBag.setIn(null, eb2);
        
        Baggage bj = Baggage.join(b1, b2);
        
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

        Baggage b1 = ExampleBag.setIn(null, eb1);
        Baggage b2 = ExampleBag.setIn(null, eb2);
        
        Baggage b3 = Baggage.branch(b1);
        Baggage b4 = Baggage.join(b1, b2);
        
        System.out.println("B4 is: " + b4);
        
    }
    
    @Test
    public void testCounterCompact1a() {
        Counter c1 = Counter.newInstance();
        for (int i = 0; i < 3; i++) c1.increment();
        
        ExampleBag eb1 = new ExampleBag();
        eb1.countermap = new TreeMap<>();
        eb1.countermap.put("c1", c1);
        
        Baggage b1 = ExampleBag.setIn(null, eb1);
        
        Baggage b2 = Baggage.branch(b1);
        ExampleBag.getFrom(b2).countermap.get("c1").increment(5);
        
        Baggage bjoined = Baggage.join(b1, b2);
        
        assertEquals(28, Baggage.serialize(bjoined).length);
        assertEquals(8, ExampleBag.getFrom(bjoined).countermap.get("c1").getValue());
    }
    
    @Test
    public void testCounterCompact1b() {
        Counter c1 = Counter.newInstance();
        for (int i = 0; i < 3; i++) c1.increment();
        
        ExampleBag eb1 = new ExampleBag();
        eb1.countermap = new TreeMap<>();
        eb1.countermap.put("c1", c1);
        
        Baggage b1 = ExampleBag.setIn(null, eb1);
        
        Baggage b2 = Baggage.branch(b1);
        ExampleBag.getFrom(b2).countermap.get("c1").increment(5);
        
        Baggage bjoined = BaggageBuffers.compact(b1, b2);
        
        assertEquals(19, Baggage.serialize(bjoined).length);
        assertEquals(8, ExampleBag.getFrom(bjoined).countermap.get("c1").getValue());
    }
    
    @Test
    public void testCounterCompact2a() {
        Counter c1 = Counter.newInstance();
        for (int i = 0; i < 3; i++) c1.increment();
        
        ExampleBag eb1 = new ExampleBag();
        eb1.countermap = new TreeMap<>();
        eb1.countermap.put("c1", c1);
        
        Baggage b1 = ExampleBag.setIn(null, eb1);
        
        Baggage b2 = Baggage.branch(b1);
        ExampleBag.getFrom(b2).countermap.get("c1").increment(5);
        
        Baggage b3 = Baggage.branch(b2);
        ExampleBag.getFrom(b3).countermap.get("c1").increment(100);
        
        Baggage b2joined = Baggage.join(b2, b3);
        assertEquals(37, Baggage.serialize(b2joined).length);
        assertEquals(108, ExampleBag.getFrom(b2joined).countermap.get("c1").getValue());
        
        ExampleBag.getFrom(b2joined).countermap.get("c1").increment(7);
        
        
        Baggage bjoined = Baggage.join(b1, b2joined);
        assertEquals(37, Baggage.serialize(bjoined).length);
        assertEquals(115, ExampleBag.getFrom(bjoined).countermap.get("c1").getValue());
    }
    
    @Test
    public void testCounterCompact2b() {
        Counter c1 = Counter.newInstance();
        for (int i = 0; i < 3; i++) c1.increment();
        
        ExampleBag eb1 = new ExampleBag();
        eb1.countermap = new TreeMap<>();
        eb1.countermap.put("c1", c1);
        
        Baggage b1 = ExampleBag.setIn(null, eb1);
        
        Baggage b2 = Baggage.branch(b1);
        ExampleBag.getFrom(b2).countermap.get("c1").increment(5);
        
        Baggage b3 = Baggage.branch(b2);
        ExampleBag.getFrom(b3).countermap.get("c1").increment(100);
        
        Baggage b2joined = Baggage.join(b2, b3);
        assertEquals(37, Baggage.serialize(b2joined).length);
        assertEquals(108, ExampleBag.getFrom(b2joined).countermap.get("c1").getValue());
        
        ExampleBag.getFrom(b2joined).countermap.get("c1").increment(7);
        
        
        Baggage bjoined = BaggageBuffers.compact(b1, b2joined);
        assertEquals(19, Baggage.serialize(bjoined).length);
        assertEquals(115, ExampleBag.getFrom(bjoined).countermap.get("c1").getValue());
    }

}
