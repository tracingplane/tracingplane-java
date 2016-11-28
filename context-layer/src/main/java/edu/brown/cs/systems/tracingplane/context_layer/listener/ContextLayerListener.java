package edu.brown.cs.systems.tracingplane.context_layer.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.brown.cs.systems.tracingplane.context_layer.ContextBaggage;

public interface ContextLayerListener {

	static final Logger log = LoggerFactory.getLogger(ContextLayerListener.class);
	
	public void postNew(ContextBaggage baggage);

	public void preBranch(ContextBaggage baggage);

	public void postBranch(ContextBaggage left, ContextBaggage right);

	public void preJoin(ContextBaggage left, ContextBaggage right);

	public void postJoin(ContextBaggage baggage);

	public void preSerialize(ContextBaggage baggage);

	public void postDeserialize(ContextBaggage baggage);
	
	public void preDiscard(ContextBaggage baggage);

}
