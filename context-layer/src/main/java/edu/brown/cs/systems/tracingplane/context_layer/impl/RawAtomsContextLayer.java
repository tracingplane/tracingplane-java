package edu.brown.cs.systems.tracingplane.context_layer.impl;

import java.nio.ByteBuffer;
import java.util.List;

import edu.brown.cs.systems.tracingplane.context_layer.ContextBaggage;
import edu.brown.cs.systems.tracingplane.context_layer.ContextLayer;

/**
 * The simple implementation of the context layer that can be used in lieu of a
 * full baggage layer. Simply merges bags lexicographically and removes
 * duplicates
 */
public class RawAtomsContextLayer implements ContextLayer<RawAtomsBaggage> {

	@Override
	public boolean isInstance(ContextBaggage baggage) {
		return baggage == null || baggage instanceof RawAtomsBaggage;
	}

	@Override
	public RawAtomsBaggage newInstance() {
		return null;
	}

	@Override
	public void discard(RawAtomsBaggage baggage) {
		if (baggage != null) {
			baggage.discard();
		}
	}

	@Override
	public RawAtomsBaggage branch(RawAtomsBaggage from) {
		if (from != null) {
			return from.branch();
		} else {
			return null;
		}
	}

	@Override
	public RawAtomsBaggage join(RawAtomsBaggage left, RawAtomsBaggage right) {
		if (left == null) {
			return right;
		} else if (right == null) {
			return left;
		} else {
			return left.mergeWith(right);
		}
	}

	@Override
	public RawAtomsBaggage wrap(List<ByteBuffer> atoms) {
		if (atoms == null || atoms.size() == 0) {
			return null;
		} else {
			return new RawAtomsBaggage(atoms);
		}
	}

	@Override
	public List<ByteBuffer> atoms(RawAtomsBaggage baggage) {
		if (baggage != null && baggage.contents.atoms != null && baggage.contents.atoms.size() > 0) {
			return baggage.contents.atoms;
		} else {
			return null;
		}
	}

}
