package brown.tracingplane.bdl;

/**
 * Defines the interfaces for special BDL types such as {@link Counter}
 */
public class SpecialTypes {

    /**
     * A P-Counter CRDT, corresponding to the {@code counter} BDL type
     */
    public interface Counter extends Bag {

        public static Counter newInstance() {
            return new CounterImpl();
        }

        public void increment();
        
        public void increment(long quantity);

        public long getValue();

    }

}
