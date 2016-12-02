package edu.brown.cs.systems.tracingplane.context_layer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.brown.cs.systems.tracingplane.context_layer.listener.ContextLayerListener;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;
import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayer;

public class TransitLayerImpl implements TransitLayer {

	private static final Logger log = LoggerFactory.getLogger(TransitLayerImpl.class);

	final ContextLayerConfig config;
	
	public final ContextLayer context;
	public final ContextLayerListener listener;
	
	public TransitLayerImpl() {
		this(new ContextLayerConfig());
	}
	
	public TransitLayerImpl(ContextLayerConfig config) {
		this.config = config;
		this.context = config.createContextLayerInstanceOrDefault();
		this.listener = config.createContextLayerListeners();
	}

	@Override
	public Baggage newInstance() {
		ContextBaggage baggage = context.wrap(null);
		listener.postNew(baggage);
		return baggage;
	}

	@Override
	public void discard(Baggage baggage) {
		if (baggage == null || baggage instanceof ContextBaggage) {
			ContextBaggage contextBaggage = (ContextBaggage) baggage;
			listener.preDiscard(contextBaggage);
			context.discard(contextBaggage);
		} else {
			log.warn("discard unknown Baggage implementation class {}", baggage.getClass().getName());
		}
	}

	@Override
	public Baggage branch(Baggage baggage) {
		if (baggage == null || baggage instanceof ContextBaggage) {
			ContextBaggage left = (ContextBaggage) baggage;
			listener.preBranch(left);
			ContextBaggage right = context.branch(left);
			listener.postBranch(left, right);
			return right;
		} else {
			log.warn("branch unknown Baggage implementation class {}", baggage.getClass().getName());
			return null;
		}
	}

	@Override
	public Baggage join(Baggage left, Baggage right) {
		boolean validLeft = left == null || left instanceof ContextBaggage;
		boolean validRight = right == null || right instanceof ContextBaggage;
		if (validLeft && validRight) {
			ContextBaggage contextLeft = (ContextBaggage) left;
			ContextBaggage contextRight = (ContextBaggage) right;
			listener.preJoin(contextLeft, contextRight);
			ContextBaggage joined = context.join((ContextBaggage) left, (ContextBaggage) right);
			listener.postJoin(joined);
			return joined;
		} else {
			if (!validLeft && !validRight) {
				log.warn("join unknown Baggage implementation class left={} right={}", left.getClass().getName(), right.getClass().getName());
			} else if (!validLeft) {
				log.warn("join unknown Baggage implementation class left={}", left.getClass().getName());
			} else if (!validRight) {
				log.warn("join unknown Baggage implementation class right={}", right.getClass().getName());
			}
			return null;
		}
	}

	@Override
	public Baggage deserialize(byte[] serialized, int offset, int length) {
		ContextBaggage baggage = context.wrap(ContextLayerSerialization.deserialize(serialized, offset, length));
		listener.postDeserialize(baggage);
		return baggage;
	}

	@Override
	public Baggage readFrom(InputStream in) throws IOException {
		ContextBaggage baggage = context.wrap(ContextLayerSerialization.readFrom(in));
		listener.postDeserialize(baggage);
		return baggage;
	}

	@Override
	public byte[] serialize(Baggage baggage) {
		if (baggage == null || baggage instanceof ContextBaggage) {
			ContextBaggage contextBaggage = (ContextBaggage) baggage;
			listener.preSerialize(contextBaggage);
			return ContextLayerSerialization.serialize(context.atoms(contextBaggage));
		} else {
			log.warn("serialize unknown Baggage implementation class {}", baggage.getClass().getName());
			return null;
		}
	}

	@Override
	public void writeTo(OutputStream out, Baggage baggage) throws IOException {
		if (baggage == null || baggage instanceof ContextBaggage) {
			ContextBaggage contextBaggage = (ContextBaggage) baggage;
			listener.preSerialize(contextBaggage);
			ContextLayerSerialization.write(out, context.atoms(contextBaggage));
		} else {
			log.warn("writeTo unknown Baggage implementation class {}", baggage.getClass().getName());
		}
	}

}
