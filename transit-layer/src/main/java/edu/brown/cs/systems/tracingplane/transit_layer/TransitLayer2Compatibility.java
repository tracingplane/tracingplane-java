package edu.brown.cs.systems.tracingplane.transit_layer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TransitLayer2Compatibility {

	static final Logger log = LoggerFactory.getLogger(TransitLayer2.class);

	static <B extends Baggage2> B newInstance(TransitLayer2<B> transit) {
		return transit.newInstance();
	}

	@SuppressWarnings("unchecked")
	static <B extends Baggage2> Baggage2 branch(TransitLayer2<B> transit, Baggage2 from) {
		if (transit.isInstance(from)) {
			return transit.branch((B) from);
		} else {
			log.warn("discarding incompatible baggage to {}.branch; baggage class is {}", transit.getClass().getName(),
					from.getClass().getName());
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	static <B extends Baggage2> Baggage2 join(TransitLayer2<B> transit, Baggage2 left, Baggage2 right) {
		if (transit.isInstance(left) && transit.isInstance(right)) {
			return transit.join((B) left, (B) right);
		} else if (transit.isInstance(left)) {
			log.warn(
					"discarding incompatible right baggage to {}.join; left baggage class is {}; right baggage class is {}",
					transit.getClass().getName(), left.getClass().getName(), right.getClass().getName());
		} else if (transit.isInstance(right)) {
			log.warn(
					"discarding incompatible left baggage to {}.join; left baggage class is {}; right baggage class is {}",
					transit.getClass().getName(), left.getClass().getName(), right.getClass().getName());
		} else {
			log.warn("discarding incompatible baggage to {}.join; left baggage class is {}; right baggage class is {}",
					transit.getClass().getName(), left.getClass().getName(), right.getClass().getName());
		}
		return null;
	}

	static <B extends Baggage2> B deserialize(TransitLayer2<B> transit, byte[] data, int offset, int length) {
		return transit.deserialize(data, offset, length);
	}

	static <B extends Baggage2> B readFrom(TransitLayer2<B> transit, InputStream in) throws IOException {
		return transit.readFrom(in);
	}

	@SuppressWarnings("unchecked")
	static <B extends Baggage2> byte[] serialize(TransitLayer2<B> transit, Baggage2 baggage) {
		if (transit.isInstance(baggage)) {
			return transit.serialize((B) baggage);
		} else {
			log.warn("discarding incompatible baggage to {}.serialize; baggage class is {}",
					transit.getClass().getName(), baggage.getClass().getName());
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	static <B extends Baggage2> void writeTo(TransitLayer2<B> transit, OutputStream out, Baggage2 baggage)
			throws IOException {
		if (transit.isInstance(baggage)) {
			transit.writeTo(out, (B) baggage);
		} else {
			log.warn("discarding incompatible baggage to {}.writeTo; baggage class is {}", transit.getClass().getName(),
					baggage.getClass().getName());
		}
	}

}
