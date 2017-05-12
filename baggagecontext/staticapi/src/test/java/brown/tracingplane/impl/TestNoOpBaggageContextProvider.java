package brown.tracingplane.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import brown.tracingplane.BaggageContext;
import brown.tracingplane.BaggageProvider;
import brown.tracingplane.BaggageProviderFactory;

public class TestNoOpBaggageContextProvider {
    
    @Test
    public void testNoOpBaggageContextProviderFactory() {
        BaggageProviderFactory factory = new NoOpBaggageContextProviderFactory();
        
        BaggageProvider<?> provider = factory.provider();
        assertNotNull(provider);
        assertTrue(provider instanceof NoOpBaggageContextProvider);
    }
    
    @Test
    public void testNoOpBaggageContextProvider() {
        BaggageProvider<?> provider = new NoOpBaggageContextProvider();
        
        assertNull(provider.newInstance());
        assertNull(provider.branch(null));
        assertNull(provider.join(null, null));
        assertNull(provider.serialize(null));
        assertNull(provider.serialize(null, 0));
        assertNull(provider.deserialize(null));
        assertNull(provider.deserialize(null, 0, 0));
        assertTrue(provider.isValid(null));
        
        BaggageContext invalidContext = new BaggageContext() {};
        assertFalse(provider.isValid(invalidContext));
        
    }

}
