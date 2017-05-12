package brown.tracingplane;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.nio.ByteBuffer;
import org.junit.Test;
import brown.tracingplane.impl.NoOpBaggageContextProvider;

public class TestStaticAPI {

    @Test
    public void testDefaultBaggageProviderIsNoOp() {
        BaggageProvider<?> provider = DefaultBaggageProvider.get();
        assertNotNull(provider);
        assertTrue(provider instanceof NoOpBaggageContextProvider);
    }

    @Test
    public void testNoOpBaggageProviderIsNotWrapped() {
        BaggageProvider<?> provider = DefaultBaggageProvider.getWrapped();
        assertNotNull(provider);
        assertTrue(provider instanceof NoOpBaggageContextProvider);
    }

    @Test
    public void testStaticAPICallsInvokeDefaultProvider() {
        assertEquals(DefaultBaggageProvider.get(), Baggage.provider);

        assertNull(Baggage.newInstance());
        assertNull(Baggage.branch(null));
        assertNull(Baggage.join(null, null));
        assertNull(Baggage.serialize(null));
        assertNull(Baggage.serialize(null, 0));
        assertNull(Baggage.deserialize(null));
        assertNull(Baggage.deserialize(null, 0, 0));
    }

    @Test
    public void testStaticAPICallsIgnoreInvalidContext() {
        BaggageContext invalidContext = new BaggageContext() {};
        assertNull(Baggage.branch(invalidContext));
        assertNull(Baggage.join(invalidContext, invalidContext));
        assertNull(Baggage.serialize(invalidContext));
        assertNull(Baggage.serialize(invalidContext, 0));
    }

    private static final class BaggageContextForTest implements BaggageContext {}

    private static final class BaggageProviderForTest implements BaggageProvider<BaggageContextForTest> {

        int isValid, newInstance, discard, branch, join, deserialize1, deserialize2, serialize1, serialize2;

        @Override
        public boolean isValid(BaggageContext baggage) {
            isValid++;
            return true;
        }

        @Override
        public BaggageContextForTest newInstance() {
            newInstance++;
            return null;
        }

        @Override
        public void discard(BaggageContextForTest baggage) {
            discard++;
        }

        @Override
        public BaggageContextForTest branch(BaggageContextForTest from) {
            branch++;
            return null;
        }

        @Override
        public BaggageContextForTest join(BaggageContextForTest left, BaggageContextForTest right) {
            join++;
            return null;
        }

        @Override
        public BaggageContextForTest deserialize(byte[] serialized, int offset, int length) {
            deserialize1++;
            return null;
        }

        @Override
        public BaggageContextForTest deserialize(ByteBuffer buf) {
            deserialize2++;
            return null;
        }

        @Override
        public byte[] serialize(BaggageContextForTest baggage) {
            serialize1++;
            return null;
        }

        @Override
        public byte[] serialize(BaggageContextForTest baggage, int maximumSerializedSize) {
            serialize2++;
            return null;
        }

        public void reset() {
            isValid = 0;
            newInstance = 0;
            discard = 0;
            branch = 0;
            join = 0;
            deserialize1 = 0;
            deserialize2 = 0;
            serialize1 = 0;
            serialize2 = 0;
        }

        public void expect(int isValid, int newInstance, int discard, int branch, int join, int d1, int d2, int s1,
                           int s2) {
            assertEquals(isValid, this.isValid);
            assertEquals(newInstance, this.newInstance);
            assertEquals(discard, this.discard);
            assertEquals(branch, this.branch);
            assertEquals(join, this.join);
            assertEquals(d1, this.deserialize1);
            assertEquals(d2, this.deserialize2);
            assertEquals(s1, this.serialize1);
            assertEquals(s2, this.serialize2);
            reset();
        }

    }

    @Test
    public void testStaticAPICallsInvokeProvider() {
        BaggageProvider<BaggageContext> originalProvider = Baggage.provider;
        BaggageProviderForTest providerForTest = new BaggageProviderForTest();
        Baggage.provider = BaggageProviderProxy.wrap(providerForTest);

        try {
            providerForTest.expect(0, 0, 0, 0, 0, 0, 0, 0, 0);
            Baggage.newInstance();
            providerForTest.expect(0, 1, 0, 0, 0, 0, 0, 0, 0);
            Baggage.branch(null);
            providerForTest.expect(1, 0, 0, 1, 0, 0, 0, 0, 0);
            Baggage.join(null, null);
            providerForTest.expect(2, 0, 0, 0, 1, 0, 0, 0, 0);
            Baggage.discard(null);
            providerForTest.expect(1, 0, 1, 0, 0, 0, 0, 0, 0);
            Baggage.serialize(null);
            providerForTest.expect(1, 0, 0, 0, 0, 0, 0, 1, 0);
            Baggage.serialize(null, 10);
            providerForTest.expect(1, 0, 0, 0, 0, 0, 0, 0, 1);
            Baggage.deserialize(null);
            providerForTest.expect(0, 0, 0, 0, 0, 0, 1, 0, 0);
            Baggage.deserialize(null, 0, 0);
            providerForTest.expect(0, 0, 0, 0, 0, 1, 0, 0, 0);
        } finally {
            Baggage.provider = originalProvider;
        }
    }

}
