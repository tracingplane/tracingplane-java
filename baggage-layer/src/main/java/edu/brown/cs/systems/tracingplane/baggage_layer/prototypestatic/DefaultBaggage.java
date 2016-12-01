package edu.brown.cs.systems.tracingplane.baggage_layer.prototypestatic;

import edu.brown.cs.systems.tracingplane.baggage_layer.prototype.baggage_buffers.xtrace.XTrace;

public class DefaultBaggage implements XTraceBaggage {

	@Override
	public XTrace getXTraceMetadata(BaggageInstance baggage) {
		return baggage.xtraceMetadata;
	}

}
