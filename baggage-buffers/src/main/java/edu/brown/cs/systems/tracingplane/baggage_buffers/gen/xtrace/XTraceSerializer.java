package edu.brown.cs.systems.tracingplane.baggage_buffers.gen.xtrace;

import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageWriter;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.Serializer2;

public class XTraceSerializer implements Serializer2<XTrace> {

	public static final BagKey taskIdKey = BagKey.indexed(0);
	public static final BagKey parentEventIdsKey = BagKey.indexed(1);

	@Override
	public void serialize(BaggageWriter builder, int level, XTrace instance) {
		if (instance != null) {
			if (instance.overflow) {
				builder.writeOverflowMarker();
			}
			builder.writeHeader(level + 1, taskIdKey);
			builder.writeLong(instance.taskId);
			builder.writeHeader(level + 1, parentEventIdsKey);
			for (Long parentEventId : instance.parentEventIds) {
				builder.writeLong(parentEventId);
			}
			builder.flush();
		}
	}

}
