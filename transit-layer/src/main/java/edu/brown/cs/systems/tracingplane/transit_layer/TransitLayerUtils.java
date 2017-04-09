package edu.brown.cs.systems.tracingplane.transit_layer;

public interface TransitLayerUtils {
    
    /** Utility for seeing/auditing baggage propagation */
    public static interface TransitAccessListener {
        public boolean enter();
        public void get();
        public void set();
        public void take();
        public void discard();
        public void branch();
        public void join();
        public void serialize();
        public void serialize(int maxLength);
        public void exit();
        public void deserialized(Baggage baggage, int length);
    }
    
    public static class NullTransitListener implements TransitAccessListener {
        public boolean enter() { return false; }
        public void get() {}
        public void set() {}
        public void take() {}
        public void discard() {}
        public void branch() {}
        public void join() {}
        public void serialize() {}
        public void serialize(int maxLength){}
        public void exit() {}
        public void deserialized(Baggage baggage, int length) {}
    }
    
    
}
