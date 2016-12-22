package edu.brown.cs.systems.baggage_buffers_examples;

import static org.junit.Assert.*;
import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import com.google.common.collect.Sets;
import edu.brown.cs.systems.tracingplane.baggage_buffers.Registrations;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;
import edu.brown.xtrace.XTraceMetadata;

public class TestXTraceMetadata {
    
    static {
        // Temporary registration for testing etc.
        Registrations.register(BagKey.indexed(0), XTraceMetadata.Handler.instance);
    }
    
    @Test
    public void simpleTest() {
        // Create an XTraceMetadata object
        XTraceMetadata xmd = new XTraceMetadata();
        xmd.taskId = 100L;
        xmd.parentEventIds = Sets.<Long>newHashSet(1000L, 300L, 200L);
        
        // Set it to the current thread's baggage
        XTraceMetadata.set(xmd);
        
        // Take away entire current thread's baggage
        Baggage b1 = Baggage.take();
        
        
        // Create another XTraceMetadata object
        XTraceMetadata xmd2 = new XTraceMetadata();
        xmd2.taskId = 100L;
        xmd2.parentEventIds = Sets.<Long>newHashSet(1000L, 500L);
        
        // Set it to the current thread's baggage
        XTraceMetadata.set(xmd2);
        
        // Take away entire current thread's baggage
        Baggage b2 = Baggage.take();
        
        System.out.println(b1);
        System.out.println(b2);
        
        // Merge them explicitly
        Baggage b3 = Baggage.join(Baggage.branch(b1), Baggage.branch(b2));
        
        System.out.println(b3);
        
        // Get the merged XTraceMetadata
        XTraceMetadata xmd3 = XTraceMetadata.getFrom(b3);
        System.out.println(xmd3);
        assertEquals(Long.valueOf(100L), xmd3.taskId);
        assertEquals(Sets.newHashSet(200L, 300L, 500L, 1000L), xmd3.parentEventIds);
    }

}
