package brown.tracingplane.bdl;

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
