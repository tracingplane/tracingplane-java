package brown.tracingplane;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.nio.ByteBuffer;
import org.junit.Test;
import brown.tracingplane.impl.NoOpTransitLayer;

public class TestActiveBaggage {

    @Test
    public void testDefaultTransitLayerIsNoOp() {
        TransitLayer transit = DefaultTransitLayer.get();
        assertNotNull(transit);
        assertTrue(transit instanceof NoOpTransitLayer);
    }

    @Test
    public void testStaticAPICallsInvokeDefaultTransitLayer() {
        assertEquals(DefaultTransitLayer.get(), ActiveBaggage.transit);

        // Invoke methods, expect no exception
        ActiveBaggage.discard();
        assertNull(ActiveBaggage.branch());
        assertNull(ActiveBaggage.branchBytes());
        ActiveBaggage.join((BaggageContext) null);
        ActiveBaggage.join((ByteBuffer) null);
        ActiveBaggage.join(null, 0, 0);
        ActiveBaggage.set((BaggageContext) null);
        ActiveBaggage.set((ByteBuffer) null);
        ActiveBaggage.set(null, 0, 0);
        assertNull(ActiveBaggage.take());
        assertNull(ActiveBaggage.takeBytes());
        assertNull(ActiveBaggage.peek());
        ActiveBaggage.update(null);
    }

    @Test
    public void testStaticAPICallsWorkWithInvalidContexts() {
        BaggageContext invalidContext = new BaggageContext() {};
        ActiveBaggage.join((BaggageContext) invalidContext);
        ActiveBaggage.set((BaggageContext) invalidContext);
        ActiveBaggage.update(invalidContext);
    }

    private static final class TransitLayerForTest implements TransitLayer {

        int discard, branch1, branch2, join1, join2, join3, set1, set2, set3, take1, take2, peek, update;

        @Override
        public void discard() {
            discard++;
        }

        @Override
        public BaggageContext branch() {
            branch1++;
            return null;
        }

        @Override
        public byte[] branchBytes() {
            branch2++;
            return null;
        }

        @Override
        public void join(BaggageContext otherContext) {
            join1++;
        }

        @Override
        public void join(ByteBuffer serializedContext) {
            join2++;
        }

        @Override
        public void join(byte[] serialized, int offset, int length) {
            join3++;
        }

        @Override
        public void set(BaggageContext baggage) {
            set1++;
        }

        @Override
        public void set(ByteBuffer serializedContext) {
            set2++;
        }

        @Override
        public void set(byte[] serialized, int offset, int length) {
            set3++;
        }

        @Override
        public BaggageContext take() {
            take1++;
            return null;
        }

        @Override
        public byte[] takeBytes() {
            take2++;
            return null;
        }

        @Override
        public BaggageContext peek() {
            peek++;
            return null;
        }

        @Override
        public void update(BaggageContext baggage) {
            update++;
        }

        public void reset() {
            discard = 0;
            branch1 = 0;
            branch2 = 0;
            join1 = 0;
            join2 = 0;
            join3 = 0;
            set1 = 0;
            set2 = 0;
            set3 = 0;
            take1 = 0;
            take2 = 0;
            peek = 0;
            update = 0;
        }

        public void expect(int discard, int branch1, int branch2, int join1, int join2, int join3, int set1, int set2,
                           int set3, int take1, int take2, int peek, int update) {
            assertEquals(discard, this.discard);
            assertEquals(branch1, this.branch1);
            assertEquals(branch2, this.branch2);
            assertEquals(join1, this.join1);
            assertEquals(join2, this.join2);
            assertEquals(join3, this.join3);
            assertEquals(set1, this.set1);
            assertEquals(set2, this.set2);
            assertEquals(set3, this.set3);
            assertEquals(take1, this.take1);
            assertEquals(take2, this.take2);
            assertEquals(peek, this.peek);
            assertEquals(update, this.update);
            reset();
        }

    }
    
    @Test
    public void testActiveBaggageAPICallsInvokeTransitLayer() {
        TransitLayer originalTransitLayer = ActiveBaggage.transit;
        TransitLayerForTest transitForTest = new TransitLayerForTest();
        ActiveBaggage.transit = transitForTest;
        
        try {
            ActiveBaggage.discard();
            transitForTest.expect(1,0,0,0,0,0,0,0,0,0,0,0,0);
            ActiveBaggage.branch();
            transitForTest.expect(0,1,0,0,0,0,0,0,0,0,0,0,0);
            ActiveBaggage.branchBytes();
            transitForTest.expect(0,0,1,0,0,0,0,0,0,0,0,0,0);
            ActiveBaggage.join((BaggageContext) null);
            transitForTest.expect(0,0,0,1,0,0,0,0,0,0,0,0,0);
            ActiveBaggage.join((ByteBuffer) null);
            transitForTest.expect(0,0,0,0,1,0,0,0,0,0,0,0,0);
            ActiveBaggage.join(null, 0, 0);
            transitForTest.expect(0,0,0,0,0,1,0,0,0,0,0,0,0);
            ActiveBaggage.set((BaggageContext) null);
            transitForTest.expect(0,0,0,0,0,0,1,0,0,0,0,0,0);
            ActiveBaggage.set((ByteBuffer) null);
            transitForTest.expect(0,0,0,0,0,0,0,1,0,0,0,0,0);
            ActiveBaggage.set(null, 0, 0);
            transitForTest.expect(0,0,0,0,0,0,0,0,1,0,0,0,0);
            ActiveBaggage.take();
            transitForTest.expect(0,0,0,0,0,0,0,0,0,1,0,0,0);
            ActiveBaggage.takeBytes();
            transitForTest.expect(0,0,0,0,0,0,0,0,0,0,1,0,0);
            ActiveBaggage.peek();
            transitForTest.expect(0,0,0,0,0,0,0,0,0,0,0,1,0);
            ActiveBaggage.update(null);
            transitForTest.expect(0,0,0,0,0,0,0,0,0,0,0,0,1);
        } finally {
            ActiveBaggage.transit = originalTransitLayer;
        }
    }

}
