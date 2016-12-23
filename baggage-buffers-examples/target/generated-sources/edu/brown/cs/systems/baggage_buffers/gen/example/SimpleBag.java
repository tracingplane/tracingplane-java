/** Generated by BaggageBuffersCompiler */
package edu.brown.cs.systems.baggage_buffers.gen.example;

import java.util.Set;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageReader;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageWriter;
import edu.brown.cs.systems.tracingplane.baggage_buffers.BaggageBuffers;
import edu.brown.cs.systems.tracingplane.baggage_buffers.Registrations;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.Bag;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.Parser;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.Serializer;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.Brancher;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.Joiner;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.BaggageHandler;
import edu.brown.cs.systems.tracingplane.baggage_buffers.impl.BBUtils;
import edu.brown.cs.systems.tracingplane.baggage_buffers.impl.ReaderHelpers;
import edu.brown.cs.systems.tracingplane.baggage_buffers.impl.WriterHelpers;
import edu.brown.cs.systems.tracingplane.baggage_buffers.impl.Parsers;
import edu.brown.cs.systems.tracingplane.baggage_buffers.impl.Serializers;
import edu.brown.cs.systems.tracingplane.baggage_buffers.impl.Branchers;
import edu.brown.cs.systems.tracingplane.baggage_buffers.impl.Joiners;

public class SimpleBag implements Bag {

    private static final Logger _log = LoggerFactory.getLogger(SimpleBag.class);

    public Set<Long> ids = null;

    public boolean _overflow = false;

    /**
    * <p>
    * Get the {@link SimpleBag} set in the {@link Baggage} carried by the current thread. If no baggage is being
    * carried by the current thread, or if there is no SimpleBag in it, then this method returns {@code null}.
    * </p>
    *
    * <p>
    * To get SimpleBag from a specific Baggage instance, use {@link #getFrom(Baggage)}.
    * </p>
    *
    * @return the SimpleBag being carried in the {@link Baggage} of the current thread, or {@code null}
    *         if none is being carried. The returned instance maybe be modified and modifications will be reflected in
    *         the baggage.
    */
    public static SimpleBag get() {
        Bag bag = BaggageBuffers.get(Handler.registration());
        if (bag instanceof SimpleBag) {
            return (SimpleBag) bag;
        } else {
            return null;
        }
    }

    /**
    * <p>
    * Get the {@link SimpleBag} set in {@code baggage}. If {@code baggage} has no SimpleBag set then
    * this method returns null.
    * </p>
    *
    * <p>
    * This method does <b>not</b> affect the Baggage being carried by the current thread.  To get SimpleBag
    * from the current thread's Baggage, use {@link #get()}.
    * </p>
    *
    * @param baggage A baggage instance to get the {@link SimpleBag} from
    * @return the {@link SimpleBag} instance being carried in {@code baggage}, or {@code null} if none is being carried.
    *         The returned instance can be modified, and modifications will be reflected in the baggage.
    */
    public static SimpleBag getFrom(Baggage baggage) {
        Bag bag = BaggageBuffers.get(baggage, Handler.registration());
        if (bag instanceof SimpleBag) {
            return (SimpleBag) bag;
        } else if (bag != null) {
            Handler.checkRegistration();
        }
        return null;
    }

    /**
    * <p>
    * Update the {@link SimpleBag} set in the current thread's baggage. This method will overwrite any existing
    * SimpleBag set in the current thread's baggage.
    * </p>
    *
    * <p>
    * To set the {@link SimpleBag} in a specific {@link Baggage} instance, use
    * {@link #setIn(Baggage, SimpleBag)}
    * </p>
    *
    * @param simpleBag the new {@link SimpleBag} to set in the current thread's {@link Baggage}. If {@code null}
    *            then any existing mappings will be removed.
    */
    public static void set(SimpleBag simpleBag) {
        BaggageBuffers.set(Handler.registration(), simpleBag);
    }

    /**
    * <p>
    * Update the {@link SimpleBag} set in {@code baggage}. This method will overwrite any existing
    * SimpleBag set in {@code baggage}.
    * </p>
    *
    * <p>
    * This method does <b>not</b> affect the {@link Baggage} being carried by the current thread. To set the
    * {@link SimpleBag} for the current thread, use {@link #set(SimpleBag)}
    * </p>
    *
    * @param baggage A baggage instance to set the {@link SimpleBag} in
    * @param simpleBag the new SimpleBag to set in {@code baggage}. If {@code null}, it will remove any
    *            mapping present.
    * @return a possibly new {@link Baggage} instance that contains all previous mappings plus the new mapping.
    */
    public static Baggage setIn(Baggage baggage, SimpleBag simpleBag) {
        return BaggageBuffers.set(baggage, Handler.registration(), simpleBag);
    }

    @Override
    public BaggageHandler<?> handler() {
        return Handler.instance;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("SimpleBag{\n");
            b.append(this.ids == null ? "" : BBUtils.indent(String.format("ids = %s\n", BBUtils.toString(this.ids))));
            b.append("}");
        return b.toString();
    }

    public static class Handler implements BaggageHandler<SimpleBag> {

        public static final Handler instance = new Handler();
        private static BagKey registration = null;

        static synchronized BagKey checkRegistration() {
            registration = Registrations.lookup(instance);
            if (registration == null) {
                _log.error("SimpleBag MUST be registered to a key before it can be propagated.  " +
                "There is currently no registration for SimpleBag and it will not be propagated. " +
                "To register a bag set the baggage-buffers.bags property in your application.conf " +
                "or with -Dbaggage-buffers.bags flag (eg, for key 10, -Dbaggage-buffers.bags.10=" + SimpleBag.class.getName());
            }
            return registration;
        }

        static BagKey registration() {
            return registration == null ? checkRegistration() : registration;
        }

        private Handler(){}

        private static final BagKey _idsKey = BagKey.indexed(1);

        private static final Parser<Set<Long>> _idsParser = Parsers.setParser(Parsers.fixed64Parser());
        private static final Serializer<Set<Long>> _idsSerializer = Serializers.setSerializer(Serializers.fixed64Serializer());
        private static final Brancher<Set<Long>> _idsBrancher = Branchers.<Long>set();
        private static final Joiner<Set<Long>> _idsJoiner = Joiners.<Long>setUnion();

        @Override
        public boolean isInstance(Bag bag) {
            return bag == null || bag instanceof SimpleBag;
        }

        @Override
        public SimpleBag parse(BaggageReader reader) {
            SimpleBag instance = new SimpleBag();

            if (reader.enter(_idsKey)) {
                instance.ids = _idsParser.parse(reader);
                reader.exit();
            }
            instance._overflow = reader.didOverflow();

            return instance;
        }

        @Override
        public void serialize(BaggageWriter writer, SimpleBag instance) {
            if (instance == null) {
                return;
            }

            writer.didOverflowHere(instance._overflow);

            if (instance.ids != null) {
                writer.enter(_idsKey);
                _idsSerializer.serialize(writer, instance.ids);
                writer.exit();
            }
        }

        @Override
        public SimpleBag branch(SimpleBag instance) {
            if (instance == null) {
                return null;
            }

            SimpleBag newInstance = new SimpleBag();
            newInstance.ids = _idsBrancher.branch(instance.ids);
            return newInstance;
        }

        @Override
        public SimpleBag join(SimpleBag left, SimpleBag right) {
            if (left == null) {
                return right;
            } else if (right == null) {
                return left;
            } else {
                left.ids = _idsJoiner.join(left.ids, right.ids);
                return left;
            }
        }
    }
}