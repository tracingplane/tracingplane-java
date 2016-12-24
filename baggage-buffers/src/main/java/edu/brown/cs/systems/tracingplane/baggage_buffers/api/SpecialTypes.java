package edu.brown.cs.systems.tracingplane.baggage_buffers.api;

public class SpecialTypes {
    
    public interface Counter extends Bag {
        
        public void increment();
        
        public long getValue();
        
    }

}
