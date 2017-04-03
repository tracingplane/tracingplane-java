package edu.brown.cs.systems.tracingplane.baggage_buffers.api;

import edu.brown.cs.systems.tracingplane.baggage_buffers.impl.CounterImpl;

public class SpecialTypes {

    public interface Counter extends Bag {

        public static Counter newInstance() {
            return new CounterImpl();
        }

        public void increment();
        
        public void increment(long quantity);

        public long getValue();

    }

}
