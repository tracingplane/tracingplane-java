package edu.brown.cs.systems.baggage_buffers_examples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.apache.log4j.BasicConfigurator;
import edu.brown.cs.systems.baggage_buffers.gen.example.ExampleBag;

public class TestExampleBag {
    
    static {
        BasicConfigurator.configure();
    }
    
    public static void main(String[] args) {
        assertNull(ExampleBag.get());
        
        ExampleBag b1 = new ExampleBag();
        b1.int32field = 10;
        
        ExampleBag.set(b1);
        
        assertEquals(b1, ExampleBag.get());
        
    }

}
