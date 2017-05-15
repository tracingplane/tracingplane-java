package edu.brown.cs.systems.baggage_buffers_examples;

import static org.junit.Assert.assertEquals;
import java.nio.ByteBuffer;
import java.util.List;
import org.junit.Test;
import com.google.common.collect.Sets;
import edu.brown.cs.systems.tracingplane.atom_layer.BaggageAtoms;
import edu.brown.cs.systems.tracingplane.atom_layer.types.Lexicographic;
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
        Baggage a = XTraceMetadata.setIn(null, xmd);
        
        // Create another XTraceMetadata object
        XTraceMetadata xmd2 = new XTraceMetadata();
        xmd2.taskId = 100L;
        xmd2.parentEventIds = Sets.<Long>newHashSet(1000L, 500L);
        Baggage b = XTraceMetadata.setIn(null, xmd2);
        
        System.out.println(a);
        System.out.println(b);
        
        // Merge them explicitly
        Baggage b3 = Baggage.join(Baggage.branch(a), Baggage.branch(b));
        
        System.out.println(b3);
        
        // Get the merged XTraceMetadata
        XTraceMetadata xmd3 = XTraceMetadata.getFrom(b3);
        System.out.println(xmd3);
        assertEquals(Long.valueOf(100L), xmd3.taskId);
        assertEquals(Sets.newHashSet(200L, 300L, 500L, 1000L), xmd3.parentEventIds);
        
        List<ByteBuffer> aa = BaggageAtoms.atoms((BaggageAtoms) a);
        List<ByteBuffer> ab = BaggageAtoms.atoms((BaggageAtoms) b);
        
        List<ByteBuffer> merged = Lexicographic.merge(aa, ab);
        
        BaggageAtoms wrapped = BaggageAtoms.wrap(merged);
        
        System.out.println("rewrapped: \n" + wrapped);
    }

}
