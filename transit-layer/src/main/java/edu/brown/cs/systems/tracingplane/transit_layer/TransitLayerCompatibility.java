package edu.brown.cs.systems.tracingplane.transit_layer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TransitLayerCompatibility {

    static final Logger log = LoggerFactory.getLogger(TransitLayerCompatibility.class);

    public static <B extends Baggage> B newInstance(TransitLayer<B> transit) {
        return transit.newInstance();
    }

    @SuppressWarnings("unchecked")
    public static <B extends Baggage> Baggage branch(TransitLayer<B> transit, Baggage from) {
        if (transit.isInstance(from)) {
            return transit.branch((B) from);
        } else {
            log.warn("discarding incompatible baggage to {}.branch; baggage class is {}", transit.getClass().getName(),
                     from.getClass().getName());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <B extends Baggage> Baggage join(TransitLayer<B> transit, Baggage left, Baggage right) {
        if (transit.isInstance(left) && transit.isInstance(right)) {
            return transit.join((B) left, (B) right);
        } else if (transit.isInstance(left)) {
            log.warn("discarding incompatible right baggage to {}.join; left baggage class is {}; right baggage class is {}",
                     transit.getClass().getName(), left.getClass().getName(), right.getClass().getName());
        } else if (transit.isInstance(right)) {
            log.warn("discarding incompatible left baggage to {}.join; left baggage class is {}; right baggage class is {}",
                     transit.getClass().getName(), left.getClass().getName(), right.getClass().getName());
        } else {
            log.warn("discarding incompatible baggage to {}.join; left baggage class is {}; right baggage class is {}",
                     transit.getClass().getName(), left.getClass().getName(), right.getClass().getName());
        }
        return null;
    }

    public static <B extends Baggage> B deserialize(TransitLayer<B> transit, byte[] data, int offset, int length) {
        return transit.deserialize(data, offset, length);
    }

    public static <B extends Baggage> B readFrom(TransitLayer<B> transit, InputStream in) throws IOException {
        return transit.readFrom(in);
    }

    @SuppressWarnings("unchecked")
    public static <B extends Baggage> byte[] serialize(TransitLayer<B> transit, Baggage baggage) {
        if (transit.isInstance(baggage)) {
            return transit.serialize((B) baggage);
        } else {
            log.warn("discarding incompatible baggage to {}.serialize; baggage class is {}",
                     transit.getClass().getName(), baggage.getClass().getName());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <B extends Baggage> void writeTo(TransitLayer<B> transit, OutputStream out,
                                                   Baggage baggage) throws IOException {
        if (transit.isInstance(baggage)) {
            transit.writeTo(out, (B) baggage);
        } else {
            log.warn("discarding incompatible baggage to {}.writeTo; baggage class is {}", transit.getClass().getName(),
                     baggage.getClass().getName());
        }
    }

    @SuppressWarnings("unchecked")
    public static <B extends Baggage> void discard(TransitLayer<B> transit, Baggage baggage) {
        if (transit.isInstance(baggage)) {
            transit.discard((B) baggage);
        } else {
            log.warn("discarding incompatible baggage to {}.discard, baggage class is {}", transit.getClass().getName(),
                     baggage.getClass().getName());
        }
    }

}
