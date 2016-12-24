package edu.brown.cs.systems.tracingplane.baggage_buffers.impl;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.BaggageHandler;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.SpecialTypes.Counter;

public class TestCounterImpl {

    @Test
    public void testCounterImpl() {

        Counter counter = new CounterImpl();

        assertEquals(0L, counter.getValue());

        for (int i = 0; i < 100; i++) {
            assertEquals((long) i, counter.getValue());
            counter.increment();
            assertEquals((long) (i + 1), counter.getValue());
        }

        BaggageHandler<?> handler = counter.handler();

        Counter c2 = (Counter) handler.branch(counter);

        assertEquals(100, counter.getValue());

        assertEquals(100, c2.getValue());

        for (int i = 0; i < 50; i++) {
            assertEquals(100 + i, counter.getValue());
            assertEquals(100, c2.getValue());
            counter.increment();
            assertEquals(100 + (i + 1), counter.getValue());
            assertEquals(100, c2.getValue());
        }

        for (int i = 0; i < 50; i++) {
            assertEquals(100 + i, c2.getValue());
            assertEquals(150, counter.getValue());
            c2.increment();
            assertEquals(100 + (i + 1), c2.getValue());
            assertEquals(150, counter.getValue());
        }

        assertEquals(150, counter.getValue());
        assertEquals(150, c2.getValue());

        Counter c3 = (Counter) handler.join(counter, c2);

        assertEquals(200, c3.getValue());
    }

}
