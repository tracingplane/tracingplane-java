package brown.tracingplane.impl;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import brown.tracingplane.baggageprotocol.BagKey;
import brown.tracingplane.baggageprotocol.BaggageReader;
import brown.tracingplane.baggageprotocol.BaggageWriter;
import brown.tracingplane.bdl.Bag;
import brown.tracingplane.bdl.BaggageHandler;

public class TestBaggageRegistry {

    @Test
    public void testEmptyByDefault() {

        BaggageHandlerRegistry defaultregistry = BaggageHandlerRegistry.create();

        assertEquals(0, defaultregistry.registrations.keys.length);
        assertEquals(0, defaultregistry.registrations.handlers.length);

    }

    private static class BaggageHandlerForTest implements BaggageHandler<BagForTest> {

        @Override
        public BagForTest parse(BaggageReader reader) {
            return null;
        }

        @Override
        public void serialize(BaggageWriter writer, BagForTest instance) {}

        @Override
        public BagForTest join(BagForTest first, BagForTest second) {
            return null;
        }

        @Override
        public BagForTest branch(BagForTest from) {
            return null;
        }

        @Override
        public boolean isInstance(Bag bag) {
            return false;
        }
    }

    private static BaggageHandlerForTest handler = new BaggageHandlerForTest();

    private static class BagForTest implements Bag {

        @Override
        public BaggageHandler<?> handler() {
            return handler;
        }

    }

    @Test
    public void testHandlerReflection() {
        Config config =
                ConfigFactory.load().withValue("bag.30", ConfigValueFactory.fromAnyRef(BagForTest.class.getName()));
        BaggageHandlerRegistry registry = BaggageHandlerRegistry.create(config);

        assertEquals(1, registry.registrations.keys.length);
        assertEquals(1, registry.registrations.handlers.length);
        assertEquals(BagKey.indexed(30), registry.registrations.keys[0]);
        assertEquals(handler, registry.registrations.handlers[0]);

        BaggageHandlerForTest handler2 = new BaggageHandlerForTest();
        registry.doAdd(BagKey.indexed(5), handler2);

        assertEquals(2, registry.registrations.keys.length);
        assertEquals(2, registry.registrations.handlers.length);
        assertEquals(BagKey.indexed(5), registry.registrations.keys[0]);
        assertEquals(handler2, registry.registrations.handlers[0]);
        assertEquals(BagKey.indexed(30), registry.registrations.keys[1]);
        assertEquals(handler, registry.registrations.handlers[1]);

    }

}
