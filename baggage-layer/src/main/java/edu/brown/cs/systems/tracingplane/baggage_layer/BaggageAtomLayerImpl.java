package edu.brown.cs.systems.tracingplane.baggage_layer;

import java.nio.ByteBuffer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.brown.cs.systems.tracingplane.atom_layer.AtomLayer;
import edu.brown.cs.systems.tracingplane.atom_layer.BaggageAtoms;
import edu.brown.cs.systems.tracingplane.baggage_layer.impl.BaggageContentsImpl;

public class BaggageAtomLayerImpl implements AtomLayer<BaggageContentsImpl> {

	private static final Logger log = LoggerFactory.getLogger(BaggageAtomLayerImpl.class);

	final BaggageLayerConfig config;
	
	public BaggageAtomLayerImpl() {
		this(new BaggageLayerConfig());
	}
	
	public BaggageAtomLayerImpl(BaggageLayerConfig config) {
		this.config = config;
	}

	@Override
	public boolean isInstance(BaggageAtoms baggage) {
		return baggage == null || baggage instanceof BaggageContentsImpl;
	}

	@Override
	public BaggageContentsImpl newInstance() {
		return null;
	}

	@Override
	public void discard(BaggageContentsImpl baggage) {
		if (baggage != null) {
			
		}
	}

	@Override
	public BaggageContentsImpl branch(BaggageContentsImpl from) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaggageContentsImpl join(BaggageContentsImpl left, BaggageContentsImpl right) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaggageContentsImpl wrap(List<ByteBuffer> atoms) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ByteBuffer> atoms(BaggageContentsImpl baggage) {
		// TODO Auto-generated method stub
		return null;
	}

}
