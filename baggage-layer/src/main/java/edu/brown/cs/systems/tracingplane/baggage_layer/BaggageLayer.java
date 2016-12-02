package edu.brown.cs.systems.tracingplane.baggage_layer;

import java.nio.ByteBuffer;
import java.util.List;

import edu.brown.cs.systems.tracingplane.context_layer.ContextBaggage;
import edu.brown.cs.systems.tracingplane.context_layer.ContextLayer;

public class BaggageLayer implements ContextLayer {

	@Override
	public void discard(ContextBaggage baggage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ContextBaggage branch(ContextBaggage from) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ContextBaggage join(ContextBaggage left, ContextBaggage right) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ContextBaggage wrap(List<ByteBuffer> bags) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ByteBuffer> atoms(ContextBaggage baggage) {
		// TODO Auto-generated method stub
		return null;
	}

}
