package edu.brown.cs.systems.tracingplane.baggage_layer;

import edu.brown.cs.systems.tracingplane.context_layer.BaggageImpl;
import edu.brown.cs.systems.tracingplane.context_layer.ContextLayerListener;

public class BaggageLayer implements ContextLayerListener {

	@Override
	public void preBranch(BaggageImpl baggage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postBranch(BaggageImpl original, BaggageImpl branched) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void preJoin(BaggageImpl left, BaggageImpl right) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postJoin(BaggageImpl joined) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void preSerialize(BaggageImpl baggage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postDeserialize(BaggageImpl baggage) {
		// TODO Auto-generated method stub
		
	}

}
