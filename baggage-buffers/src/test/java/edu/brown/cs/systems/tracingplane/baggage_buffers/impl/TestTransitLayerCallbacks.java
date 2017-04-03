package edu.brown.cs.systems.tracingplane.baggage_buffers.impl;

import static org.junit.Assert.assertEquals;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.junit.Test;
import edu.brown.cs.systems.tracingplane.baggage_buffers.BaggageBuffers;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.BaggageHandler;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.SpecialTypes.Counter;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;
import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayerCallbacks.TransitHandler;

public class TestTransitLayerCallbacks {
    
    @Test
    public void testRegistration() throws IOException {
        AtomicInteger branchCount = new AtomicInteger(0);
        AtomicInteger joinCount = new AtomicInteger(0);
        TransitHandler handler = new TransitHandler() {
            @Override
            public <B extends Baggage> B branch(B from, Function<B, B> wrapped) {
                branchCount.getAndIncrement();
                return wrapped.apply(from);
            }
            @Override
            public <B extends Baggage> B join(B left, B right, BiFunction<B, B, B> wrapped) {
                joinCount.getAndIncrement();
                return wrapped.apply(left, right);
            }
        };
        Closeable closer = BaggageBuffers.registerCallbackHandler(handler);
        assertEquals(0, branchCount.get());
        assertEquals(0, joinCount.get());

        Baggage.branch();
        assertEquals(1, branchCount.get());
        assertEquals(0, joinCount.get());
        
        Baggage.branch();
        assertEquals(2, branchCount.get());
        assertEquals(0, joinCount.get());
        
        Baggage.join(null);
        assertEquals(2, branchCount.get());
        assertEquals(1, joinCount.get());

        Baggage.branch();
        Baggage.join(null);
        assertEquals(3, branchCount.get());
        assertEquals(2, joinCount.get());
        
        closer.close();

        Baggage.branch();
        Baggage.join(null);
        assertEquals(3, branchCount.get());
        assertEquals(2, joinCount.get());
    }
    
    @Test
    public void testNestedRegistration() throws IOException {
        AtomicInteger branchCount = new AtomicInteger(0);
        AtomicInteger joinCount = new AtomicInteger(0);
        AtomicInteger branchCount2 = new AtomicInteger(0);
        AtomicInteger joinCount2 = new AtomicInteger(0);
        TransitHandler handler1 = new TransitHandler() {
            @Override
            public <B extends Baggage> B branch(B from, Function<B, B> wrapped) {
                int count = branchCount.get();
                try {
                    return wrapped.apply(from);
                } finally {
                    assertEquals(count+1, branchCount.get());
                    branchCount2.getAndIncrement();
                }
            }
            @Override
            public <B extends Baggage> B join(B left, B right, BiFunction<B, B, B> wrapped) {
                int count = joinCount.get();
                try {
                    return wrapped.apply(left, right);
                } finally {
                    assertEquals(count+1, joinCount.get());
                    joinCount2.getAndIncrement();
                }
            }
        };
        TransitHandler handler2 = new TransitHandler() {
            @Override
            public <B extends Baggage> B branch(B from, Function<B, B> wrapped) {
                branchCount.getAndIncrement();
                return wrapped.apply(from);
            }
            @Override
            public <B extends Baggage> B join(B left, B right, BiFunction<B, B, B> wrapped) {
                joinCount.getAndIncrement();
                return wrapped.apply(left, right);
            }
        };
        Closeable closer2 = BaggageBuffers.registerCallbackHandler(handler2);
        Closeable closer1 = BaggageBuffers.registerCallbackHandler(handler1);
        
        assertEquals(0, branchCount.get());
        assertEquals(0, joinCount.get());
        assertEquals(0, branchCount2.get());
        assertEquals(0, joinCount2.get());

        Baggage.branch();
        assertEquals(1, branchCount.get());
        assertEquals(0, joinCount.get());
        assertEquals(1, branchCount2.get());
        assertEquals(0, joinCount2.get());
        
        Baggage.branch();
        assertEquals(2, branchCount.get());
        assertEquals(0, joinCount.get());
        assertEquals(2, branchCount2.get());
        assertEquals(0, joinCount2.get());
        
        Baggage.join(null);
        assertEquals(2, branchCount.get());
        assertEquals(1, joinCount.get());
        assertEquals(2, branchCount2.get());
        assertEquals(1, joinCount2.get());

        Baggage.branch();
        Baggage.join(null);
        assertEquals(3, branchCount.get());
        assertEquals(2, joinCount.get());
        assertEquals(3, branchCount2.get());
        assertEquals(2, joinCount2.get());

        closer1.close();

        Baggage.branch();
        Baggage.join(null);
        assertEquals(4, branchCount.get());
        assertEquals(3, joinCount.get());
        assertEquals(3, branchCount2.get());
        assertEquals(2, joinCount2.get());

        closer2.close();

        Baggage.branch();
        Baggage.join(null);
        assertEquals(4, branchCount.get());
        assertEquals(3, joinCount.get());
        assertEquals(3, branchCount2.get());
        assertEquals(2, joinCount2.get());
    }

}
