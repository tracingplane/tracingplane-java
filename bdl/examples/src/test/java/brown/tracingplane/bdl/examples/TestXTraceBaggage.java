package brown.tracingplane.bdl.examples;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.google.common.collect.Sets;
import brown.tracingplane.BaggageContext;
import brown.tracingplane.baggageprotocol.BagKey;
import brown.tracingplane.impl.BDLContext;
import brown.tracingplane.impl.BDLContextProvider;
import brown.tracingplane.impl.BDLContextProviderFactory;
import brown.tracingplane.impl.BaggageHandlerRegistry;
import brown.xtrace.XTraceBaggage;

public class TestXTraceBaggage {
    
    static {
        // Temporary registration for testing etc.
        BaggageHandlerRegistry.add(BagKey.indexed(0), XTraceBaggage.Handler.instance);
    }
    
    private static final BDLContextProvider provider = (BDLContextProvider) new BDLContextProviderFactory().provider();
    
    @Test
    public void simpleTest() {
        // Create an XTraceBaggage object
        XTraceBaggage xmd = new XTraceBaggage();
        xmd.taskId = 100L;
        xmd.parentEventIds = Sets.<Long>newHashSet(1000L, 300L, 200L);
        BaggageContext a = XTraceBaggage.setIn(null, xmd);
        
        // Create another XTraceBaggage object
        XTraceBaggage xmd2 = new XTraceBaggage();
        xmd2.taskId = 100L;
        xmd2.parentEventIds = Sets.<Long>newHashSet(1000L, 500L);
        BaggageContext b = XTraceBaggage.setIn(null, xmd2);
        
        // Merge them explicitly
        BaggageContext b3 = provider.join(provider.branch((BDLContext) a), provider.branch((BDLContext) b));
        
        // Get the merged XTraceBaggage
        XTraceBaggage xmd3 = XTraceBaggage.getFrom(b3);
        assertEquals(Long.valueOf(100L), xmd3.taskId);
        assertEquals(Sets.newHashSet(200L, 300L, 500L, 1000L), xmd3.parentEventIds);
    }

}
