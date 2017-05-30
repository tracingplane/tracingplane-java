package brown.tracingplane;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import brown.tracingplane.impl.BDLContextProvider;
import brown.tracingplane.impl.ThreadLocalTransitLayer;

public class TestConfiguration {

    @Test
    public void testDefaultBaggageProviderIsBDLContextProvider() {
        BaggageProvider<?> provider = DefaultBaggageProvider.get();
        assertNotNull(provider);
        assertTrue(provider instanceof BDLContextProvider);
    }
    
    @Test
    public void testDefaultTransitLayerIsThreadLocalTransitLayer() {
        TransitLayer transit = DefaultTransitLayer.get();
        assertNotNull(transit);
        assertTrue(transit instanceof ThreadLocalTransitLayer);
    }

}
