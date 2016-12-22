package edu.brown.cs.systems.baggage_buffers_examples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import edu.brown.cs.systems.baggage_buffers.gen.example.ExampleBag;
import edu.brown.cs.systems.tracingplane.atom_layer.types.TypeUtils;
import edu.brown.cs.systems.tracingplane.baggage_buffers.Registrations;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;

public class TestExampleBag {
    
    static {
        BasicConfigurator.configure();
    }
    
    @Test
    public void testExampleBag() {
        Registrations.register(BagKey.indexed(10), ExampleBag.Handler.instance);
        
        assertNull(ExampleBag.get());
        
        ExampleBag b1 = new ExampleBag();
        b1.int32field = 10;
        
        ExampleBag.set(b1);
        
        assertEquals(b1, ExampleBag.get());
        
        System.out.println(TypeUtils.toHexString(Baggage.serialize()));
        
    }

}
