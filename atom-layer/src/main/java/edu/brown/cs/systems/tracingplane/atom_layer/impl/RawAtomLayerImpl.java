package edu.brown.cs.systems.tracingplane.atom_layer.impl;

import java.nio.ByteBuffer;
import java.util.List;

import edu.brown.cs.systems.tracingplane.atom_layer.AtomLayer;
import edu.brown.cs.systems.tracingplane.atom_layer.BaggageAtoms;

/**
 * The simple implementation of the context layer that can be used in lieu of a
 * full baggage layer. Simply merges bags lexicographically and removes
 * duplicates
 */
public class RawAtomLayerImpl implements AtomLayer<RawBaggageAtoms> {

	@Override
	public boolean isInstance(BaggageAtoms baggage) {
		return baggage == null || baggage instanceof RawBaggageAtoms;
	}

	@Override
	public RawBaggageAtoms newInstance() {
		return null;
	}

	@Override
	public void discard(RawBaggageAtoms baggage) {
		if (baggage != null) {
			baggage.discard();
		}
	}

	@Override
	public RawBaggageAtoms branch(RawBaggageAtoms from) {
		if (from != null) {
			return from.branch();
		} else {
			return null;
		}
	}

	@Override
	public RawBaggageAtoms join(RawBaggageAtoms left, RawBaggageAtoms right) {
		if (left == null) {
			return right;
		} else if (right == null) {
			return left;
		} else {
			return left.mergeWith(right);
		}
	}

	@Override
	public RawBaggageAtoms wrap(List<ByteBuffer> atoms) {
		if (atoms == null || atoms.size() == 0) {
			return null;
		} else {
			return new RawBaggageAtoms(atoms);
		}
	}

	@Override
	public List<ByteBuffer> atoms(RawBaggageAtoms baggage) {
		if (baggage != null && baggage.contents.atoms != null && baggage.contents.atoms.size() > 0) {
			return baggage.contents.atoms;
		} else {
			return null;
		}
	}

}
