package edu.brown.cs.systems.tracingplane.context_layer;

import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;
import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayer;

public class ContextLayer implements TransitLayer {
	
	static final Logger log = LoggerFactory.getLogger(ContextLayer.class);
	
	final ContextLayerConfig config;
	final ContextLayerListener listener;
	
	public ContextLayer() {
		this(new ContextLayerConfig());
	}
	
	public ContextLayer(ContextLayerConfig config) {
		this.config = config;
		this.listener = config.tryCreateListeners();
	}

	@Override
	public Baggage newInstance() {
		return new BaggageImpl();
	}

	@Override
	public Baggage branch(Baggage from) {
		if (from == null || from instanceof BaggageImpl) {
			return doBranch((BaggageImpl) from);
		} else {
			log.warn("branch unknown Baggage implementation class {}", from.getClass().getName());
			return null;
		}
	}
	
	private Baggage doBranch(BaggageImpl from) {
		listener.preBranch(from);
		BaggageImpl branched = from == null ? null : from.branch();
		listener.postBranch(from, branched);
		return branched;
	}

	@Override
	public Baggage join(Baggage left, Baggage right) {
		boolean validLeft = left == null || left instanceof BaggageImpl;
		boolean validRight = right == null || right instanceof BaggageImpl;
		if (validLeft && validRight) {
			return doJoin((BaggageImpl) left, (BaggageImpl) right);
		} else {
			if (!validLeft && !validRight) {
				log.warn("merge unknown Baggage implementation class left={} right={}", left.getClass().getName(), right.getClass().getName());
			} else if (!validLeft) {
				log.warn("merge unknown Baggage implementation class left={}", left.getClass().getName());
			} else if (!validRight) {
				log.warn("merge unknown Baggage implementation class right={}", right.getClass().getName());
			}
			return null;
		}
	}
	
	private Baggage doJoin(BaggageImpl left, BaggageImpl right) {
		listener.preJoin(left, right);
		BaggageImpl joined = left == null ? right : left.mergeWith(right);
		listener.postJoin(joined);
		return joined;
	}

	@Override
	public Baggage deserialize(byte[] serialized, int offset, int length) {
		BaggageImpl deserialized = BaggageImpl.deserialize(serialized, offset, length);
		listener.postDeserialize(deserialized);
		return deserialized;
	}

	@Override
	public Baggage readFrom(InputStream in) {
		BaggageImpl deserialized = BaggageImpl.readFrom(in);
		listener.postDeserialize(deserialized);
		return deserialized;
	}

	@Override
	public byte[] serialize(Baggage instance) {
		if (instance == null || instance instanceof BaggageImpl) {
			return doSerialize((BaggageImpl) instance);
		} else {
			log.warn("serialize unknown Baggage implementation class {}", instance.getClass().getName());
			return null;
		}
	}
	
	private byte[] doSerialize(BaggageImpl baggage) {
		listener.preSerialize(baggage);
		return baggage == null ? BaggageImpl.EMPTY_BYTES : baggage.serialize();
	}

	@Override
	public void writeTo(Baggage instance, OutputStream out) {
		if (instance == null || instance instanceof BaggageImpl) {
			doWriteTo((BaggageImpl) instance, out);
		} else {
			log.warn("writeto unknown Baggage implementation class {}", instance.getClass().getName());
		}
	}
	
	private void doWriteTo(BaggageImpl baggage, OutputStream out) {
		listener.preSerialize(baggage);
		if (baggage != null) {
			baggage.writeTo(out);
		}
	}

}
