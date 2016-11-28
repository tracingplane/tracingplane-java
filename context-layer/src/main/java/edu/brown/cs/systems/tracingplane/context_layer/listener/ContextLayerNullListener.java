package edu.brown.cs.systems.tracingplane.context_layer.listener;

import edu.brown.cs.systems.tracingplane.context_layer.ContextBaggage;

public class ContextLayerNullListener implements ContextLayerListener {

	@Override
	public void postNew(ContextBaggage baggage) {
	}

	@Override
	public void preBranch(ContextBaggage baggage) {
	}

	@Override
	public void postBranch(ContextBaggage left, ContextBaggage right) {
	}

	@Override
	public void preJoin(ContextBaggage left, ContextBaggage right) {
	}

	@Override
	public void postJoin(ContextBaggage baggage) {
	}

	@Override
	public void preSerialize(ContextBaggage baggage) {
	}

	@Override
	public void postDeserialize(ContextBaggage baggage) {
	}

	@Override
	public void preDiscard(ContextBaggage baggage) {
	}

}
