package edu.brown.cs.systems.tracingplane.baggage_layer.impl;

import java.nio.ByteBuffer;
import java.util.List;

import edu.brown.cs.systems.tracingplane.atom_layer.BaggageAtoms;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey.BagPath;
import edu.brown.cs.systems.tracingplane.baggage_layer.BaggageLayer;

/**
 * TODO: documentation and description
 * 
 */
public class GenericBaggageLayer implements BaggageLayer<GenericBaggageContents> {

	@Override
	public boolean isInstance(BaggageAtoms baggage) {
		return baggage == null || baggage instanceof GenericBaggageContents;
	}

	@Override
	public GenericBaggageContents newInstance() {
		return null;
	}

	@Override
	public void discard(GenericBaggageContents baggage) {
	}

	@Override
	public GenericBaggageContents branch(GenericBaggageContents from) {
		if (from != null) {
			return from.branch();
		} else {
			return null;
		}
	}

	@Override
	public GenericBaggageContents join(GenericBaggageContents left, GenericBaggageContents right) {
		if (left == null && right == null) {
			return null;
		} else if (left != null) {
			return left.mergeWith(right);
		} else {
			return right.mergeWith(left);
		}
	}

	@Override
	public GenericBaggageContents wrap(List<ByteBuffer> atoms) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ByteBuffer> atoms(GenericBaggageContents baggage) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean checkOverflow(GenericBaggageContents baggage, BagPath path, boolean includeChildren,
			boolean includeTrimmed) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean exists(GenericBaggageContents baggage, BagPath path) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<ByteBuffer> get(GenericBaggageContents baggage, BagPath path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void add(GenericBaggageContents baggage, BagPath path, ByteBuffer data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void add(GenericBaggageContents baggage, BagPath path, List<ByteBuffer> datas) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void replace(GenericBaggageContents baggage, BagPath path, ByteBuffer data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void replace(GenericBaggageContents baggage, BagPath path, List<ByteBuffer> datas) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear(GenericBaggageContents baggage, BagPath path) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drop(GenericBaggageContents baggage, BagPath path) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<BagKey> children(GenericBaggageContents baggage, BagPath path) {
		// TODO Auto-generated method stub
		return null;
	}

}
