package edu.brown.cs.systems.baggage_buffers_examples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import edu.brown.cs.systems.baggage_buffers.gen.example.ExampleBag;
import edu.brown.cs.systems.baggage_buffers.gen.example.SimpleBag2;
import edu.brown.cs.systems.tracingplane.atom_layer.BaggageAtoms;
import edu.brown.cs.systems.tracingplane.atom_layer.types.TypeUtils;
import edu.brown.cs.systems.tracingplane.baggage_buffers.Registrations;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;

public class TestExampleBag {

    static {
        BasicConfigurator.configure();

        Registrations.register(BagKey.indexed(10), ExampleBag.Handler.instance);
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

}
