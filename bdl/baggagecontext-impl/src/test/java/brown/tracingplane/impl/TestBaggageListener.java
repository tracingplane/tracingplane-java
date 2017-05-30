package brown.tracingplane.impl;

import static org.junit.Assert.assertEquals;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import brown.tracingplane.BaggageListener.BranchListener;
import brown.tracingplane.BaggageListener.JoinListener;

public class TestBaggageListener {

    BDLContextProvider provider = new BDLContextProvider();

    @Test
    public void testBranchListener() {
        final AtomicInteger branchCount = new AtomicInteger(0);
        BranchListener<BDLContext> branchListener = (from, wrapped) -> {
            branchCount.getAndIncrement();
            return wrapped.apply(from);
        };
        provider.addListener(branchListener);

        final AtomicInteger joinCount = new AtomicInteger(0);
        JoinListener<BDLContext> joinListener = (l, r, wrapped) -> {
            joinCount.getAndIncrement();
            return wrapped.apply(l, r);
        };
        provider.addListener(joinListener);

        assertEquals(0, branchCount.get());
        assertEquals(0, joinCount.get());

        provider.branch(null);
        assertEquals(1, branchCount.get());
        assertEquals(0, joinCount.get());

        provider.branch(null);
        assertEquals(2, branchCount.get());
        assertEquals(0, joinCount.get());

        provider.join(null, null);
        assertEquals(2, branchCount.get());
        assertEquals(1, joinCount.get());

        provider.branch(null);
        provider.join(null, null);
        assertEquals(3, branchCount.get());
        assertEquals(2, joinCount.get());


        provider.branch(null);
        provider.join(null, null);
        assertEquals(4, branchCount.get());
        assertEquals(3, joinCount.get());
    }
    
    @Test
    public void testNestedBranch() {
        final AtomicInteger branchCount1 = new AtomicInteger(0);
        final AtomicInteger branchCount2 = new AtomicInteger(0);
        
        BranchListener<BDLContext> branchListener1 = (from, wrapped) -> {
            assertEquals(branchCount2.get()-1, branchCount1.get());
            branchCount1.getAndIncrement();
            return wrapped.apply(from);
        };
        provider.addListener(branchListener1);
        
        BranchListener<BDLContext> branchListener2 = (from, wrapped) -> {
            assertEquals(branchCount1.get(), branchCount2.get());
            branchCount2.getAndIncrement();
            return wrapped.apply(from);
        };
        provider.addListener(branchListener2);

        assertEquals(0, branchCount1.get());
        assertEquals(0, branchCount2.get());

        provider.branch(null);
        provider.branch(null);
        provider.branch(null);
        provider.branch(null);

        assertEquals(4, branchCount1.get());
        assertEquals(4, branchCount2.get());
    }

//    @Test
//    public void testNestedRegistration() throws IOException {
//        AtomicInteger branchCount = new AtomicInteger(0);
//        AtomicInteger joinCount = new AtomicInteger(0);
//        AtomicInteger branchCount2 = new AtomicInteger(0);
//        AtomicInteger joinCount2 = new AtomicInteger(0);
//        TransitHandler handler1 = new TransitHandler() {
//            @Override
//            public <B extends Baggage> B branch(B from, Function<B, B> wrapped) {
//                int count = branchCount.get();
//                try {
//                    return wrapped.apply(from);
//                } finally {
//                    assertEquals(count + 1, branchCount.get());
//                    branchCount2.getAndIncrement();
//                }
//            }
//
//            @Override
//            public <B extends Baggage> B join(B left, B right, BiFunction<B, B, B> wrapped) {
//                int count = joinCount.get();
//                try {
//                    return wrapped.apply(left, right);
//                } finally {
//                    assertEquals(count + 1, joinCount.get());
//                    joinCount2.getAndIncrement();
//                }
//            }
//        };
//        TransitHandler handler2 = new TransitHandler() {
//            @Override
//            public <B extends Baggage> B branch(B from, Function<B, B> wrapped) {
//                branchCount.getAndIncrement();
//                return wrapped.apply(from);
//            }
//
//            @Override
//            public <B extends Baggage> B join(B left, B right, BiFunction<B, B, B> wrapped) {
//                joinCount.getAndIncrement();
//                return wrapped.apply(left, right);
//            }
//        };
//        Closeable closer2 = BaggageBuffers.registerCallbackHandler(handler2);
//        Closeable closer1 = BaggageBuffers.registerCallbackHandler(handler1);
//
//        assertEquals(0, branchCount.get());
//        assertEquals(0, joinCount.get());
//        assertEquals(0, branchCount2.get());
//        assertEquals(0, joinCount2.get());
//
//        Baggage.branch();
//        assertEquals(1, branchCount.get());
//        assertEquals(0, joinCount.get());
//        assertEquals(1, branchCount2.get());
//        assertEquals(0, joinCount2.get());
//
//        Baggage.branch();
//        assertEquals(2, branchCount.get());
//        assertEquals(0, joinCount.get());
//        assertEquals(2, branchCount2.get());
//        assertEquals(0, joinCount2.get());
//
//        Baggage.join(null);
//        assertEquals(2, branchCount.get());
//        assertEquals(1, joinCount.get());
//        assertEquals(2, branchCount2.get());
//        assertEquals(1, joinCount2.get());
//
//        Baggage.branch();
//        Baggage.join(null);
//        assertEquals(3, branchCount.get());
//        assertEquals(2, joinCount.get());
//        assertEquals(3, branchCount2.get());
//        assertEquals(2, joinCount2.get());
//
//        closer1.close();
//
//        Baggage.branch();
//        Baggage.join(null);
//        assertEquals(4, branchCount.get());
//        assertEquals(3, joinCount.get());
//        assertEquals(3, branchCount2.get());
//        assertEquals(2, joinCount2.get());
//
//        closer2.close();
//
//        Baggage.branch();
//        Baggage.join(null);
//        assertEquals(4, branchCount.get());
//        assertEquals(3, joinCount.get());
//        assertEquals(3, branchCount2.get());
//        assertEquals(2, joinCount2.get());
//    }

}
