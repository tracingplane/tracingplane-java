package edu.brown.cs.systems.tracingplane.context_layer.impl;

import java.nio.ByteBuffer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.brown.cs.systems.tracingplane.context_layer.ContextBaggage;
import edu.brown.cs.systems.tracingplane.context_layer.ContextLayer;

/**
 * The simple implementation of the context layer that can be used in lieu of a
 * full baggage layer. Simply merges bags lexicographically and removes
 * duplicates
 */
public class BlindContextLayer implements ContextLayer {

	static final Logger log = LoggerFactory.getLogger(ContextLayer.class);

	@Override
	public void discard(ContextBaggage baggage) {
		if (baggage == null) {
			return;
		} else if (baggage instanceof BlindContextBaggage) {
			((BlindContextBaggage) baggage).discard();
		} else {
			log.warn("discard unknown Baggage implementation class {}", baggage.getClass().getName());
		}
	}

	@Override
	public ContextBaggage branch(ContextBaggage from) {
		if (from == null) {
			return null;
		} else if (from instanceof BlindContextBaggage) {
			return ((BlindContextBaggage) from).branch();
		} else {
			log.warn("branch unknown ContextBaggage implementation class {}", from.getClass().getName());
			return null;
		}
	}

	@Override
	public ContextBaggage join(ContextBaggage left, ContextBaggage right) {
		if (left == null) {
			if (right == null) {
				return null;
			} else if (right instanceof BlindContextBaggage) {
				return right;
			} else {
				log.warn("merge unknown ContextBaggage implementation class right={}", right.getClass().getName());
				return null;
			}
		} else if (left instanceof BlindContextBaggage) {
			if (right == null) {
				return left;
			} else if (right instanceof BlindContextBaggage) {
				return ((BlindContextBaggage) left).mergeWith((BlindContextBaggage) right);
			} else {
				log.warn("merge unknown ContextBaggage implementation class right={}", right.getClass().getName());
				return null;
			}
		} else {
			if (right == null || right instanceof BlindContextBaggage) {
				log.warn("merge unknown ContextBaggage implementation class left={}", left.getClass().getName());
				return null;
			} else {
				log.warn("merge unknown ContextBaggage implementation class left={} right={}",
						left.getClass().getName(), right.getClass().getName());
				return null;
			}
		}
	}

	@Override
	public ContextBaggage wrap(List<ByteBuffer> bags) {
		if (bags == null || bags.size() == 0) {
			return null;
		} else {
			return new BlindContextBaggage(bags);
		}
	}

	@Override
	public List<ByteBuffer> bags(ContextBaggage baggage) {
		if (baggage == null) {
			return null;
		} else if (baggage instanceof BlindContextBaggage) {
			List<ByteBuffer> bags = ((BlindContextBaggage) baggage).contents.bags;
			if (bags == null || bags.size() == 0) {
				return null;
			} else {
				return bags;
			}
		} else {
			log.warn("bags unknown ContextBaggage implementation class {}", baggage.getClass().getName());
			return null;
		}
	}

}
