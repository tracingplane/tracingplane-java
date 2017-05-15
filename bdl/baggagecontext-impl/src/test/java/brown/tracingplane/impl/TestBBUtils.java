package brown.tracingplane.impl;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class TestBBUtils {
    
    @Test
    public void testSerializedSize() {
        assertEquals(0, BDLContextUtils.serializedSize(null));
    }

}
