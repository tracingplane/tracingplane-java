package edu.brown.cs.systems.tracingplane.context_layer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ContextLayerListener {

	static final Logger log = LoggerFactory.getLogger(ContextLayerListener.class);

	public void preBranch(BaggageImpl baggage);

	public void postBranch(BaggageImpl original, BaggageImpl branched);

	public void preJoin(BaggageImpl left, BaggageImpl right);

	public void postJoin(BaggageImpl joined);

	public void preSerialize(BaggageImpl baggage);

	public void postDeserialize(BaggageImpl baggage);

	/**
	 * Container for multiple listeners, that also wraps and logs exceptions
	 */
	static class ContextLayerListenerContainer implements ContextLayerListener {

		private final int size;
		private final ContextLayerListener[] listeners;

		ContextLayerListenerContainer(ContextLayerListener[] listeners) {
			this.size = listeners.length;
			this.listeners = listeners;
		}

		@Override
		public void preBranch(BaggageImpl baggage) {
			for (int i = 0; i < size; i++) {
				try {
					listeners[i].preBranch(baggage);
				} catch (Exception e) {
					log.warn(String.format("%s.preBranch(%s)", listeners[i].getClass().getName(), baggage), e);
				}
			}
		}

		@Override
		public void postBranch(BaggageImpl original, BaggageImpl branched) {
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
		public void preJoin(BaggageImpl left, BaggageImpl right) {
			for (int i = 0; i < size; i++) {
				try {
					listeners[i].preJoin(left, right);
				} catch (Exception e) {
					log.warn(String.format("%s.preJoin(%s, %s)", listeners[i].getClass().getName(), left, right), e);
				}
			}
		}

		@Override
		public void postJoin(BaggageImpl joined) {
			for (int i = 0; i < size; i++) {
				try {
					listeners[i].postJoin(joined);
				} catch (Exception e) {
					log.warn(String.format("%s.postJoin(%s)", listeners[i].getClass().getName(), joined), e);
				}
			}
		}

		@Override
		public void preSerialize(BaggageImpl baggage) {
			for (int i = 0; i < size; i++) {
				try {
					listeners[i].preSerialize(baggage);
				} catch (Exception e) {
					log.warn(String.format("%s.preSerialize(%s)", listeners[i].getClass().getName(), baggage), e);
				}
			}
		}

		@Override
		public void postDeserialize(BaggageImpl baggage) {
			for (int i = 0; i < size; i++) {
				try {
					listeners[i].postDeserialize(baggage);
				} catch (Exception e) {
					log.warn(String.format("%s.postDeserialize(%s)", listeners[i].getClass().getName(), baggage), e);
				}
			}
		}

	}

}
