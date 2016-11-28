package edu.brown.cs.systems.tracingplane.context_layer.listener;

import java.util.List;

import edu.brown.cs.systems.tracingplane.context_layer.ContextBaggage;

/**
 * Container for multiple listeners that catches and logs exceptions
 */
public class ContextLayerListenerContainer implements ContextLayerListener {

	final int size;
	final ContextLayerListener[] listeners;
	
	public ContextLayerListenerContainer(List<ContextLayerListener> listeners) {
		this.size = listeners.size();
		this.listeners = new ContextLayerListener[size];
		listeners.toArray(this.listeners);
	}

	public ContextLayerListenerContainer(ContextLayerListener[] listeners) {
		this.size = listeners.length;
		this.listeners = listeners;
	}

	@Override
	public void preBranch(ContextBaggage baggage) {
		for (int i = 0; i < size; i++) {
			try {
				listeners[i].preBranch(baggage);
			} catch (Exception e) {
				log.warn(String.format("%s.preBranch(%s)", listeners[i].getClass().getName(), baggage), e);
			}
		}
	}

	@Override
	public void postBranch(ContextBaggage original, ContextBaggage branched) {
		for (int i = 0; i < size; i++) {
			try {
				listeners[i].postBranch(original, branched);
			} catch (Exception e) {
				log.warn(String.format("%s.postBranch(%s, %s)", listeners[i].getClass().getName(), original,
						branched), e);
			}
		}
	}

	@Override
	public void preJoin(ContextBaggage left, ContextBaggage right) {
		for (int i = 0; i < size; i++) {
			try {
				listeners[i].preJoin(left, right);
			} catch (Exception e) {
				log.warn(String.format("%s.preJoin(%s, %s)", listeners[i].getClass().getName(), left, right), e);
			}
		}
	}

	@Override
	public void postJoin(ContextBaggage joined) {
		for (int i = 0; i < size; i++) {
			try {
				listeners[i].postJoin(joined);
			} catch (Exception e) {
				log.warn(String.format("%s.postJoin(%s)", listeners[i].getClass().getName(), joined), e);
			}
		}
	}

	@Override
	public void preSerialize(ContextBaggage baggage) {
		for (int i = 0; i < size; i++) {
			try {
				listeners[i].preSerialize(baggage);
			} catch (Exception e) {
				log.warn(String.format("%s.preSerialize(%s)", listeners[i].getClass().getName(), baggage), e);
			}
		}
	}

	@Override
	public void postDeserialize(ContextBaggage baggage) {
		for (int i = 0; i < size; i++) {
			try {
				listeners[i].postDeserialize(baggage);
			} catch (Exception e) {
				log.warn(String.format("%s.postDeserialize(%s)", listeners[i].getClass().getName(), baggage), e);
			}
		}
	}

	@Override
	public void postNew(ContextBaggage baggage) {
		for (int i = 0; i < size; i++) {
			try {
				listeners[i].postNew(baggage);
			} catch (Exception e) {
				log.warn(String.format("%s.postNew(%s)", listeners[i].getClass().getName(), baggage), e);
			}
		}
	}

	@Override
	public void preDiscard(ContextBaggage baggage) {
		for (int i = 0; i < size; i++) {
			try {
				listeners[i].preDiscard(baggage);
			} catch (Exception e) {
				log.warn(String.format("%s.preDiscard(%s)", listeners[i].getClass().getName(), baggage), e);
			}
		}
	}

}