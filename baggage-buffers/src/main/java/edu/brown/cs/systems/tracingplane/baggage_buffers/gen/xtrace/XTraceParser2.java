package edu.brown.cs.systems.tracingplane.baggage_buffers.gen.xtrace;

import java.util.List;

import edu.brown.cs.systems.tracingplane.baggage_buffers.impl.ReaderHelpers;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageReader;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.Parser2;

public class XTraceParser2 implements Parser2<XTrace> {

	public static final BagKey taskIdKey = BagKey.indexed(0);
	public static final BagKey parentEventIdsKey = BagKey.indexed(1);

	@Override
	public XTrace parse(BaggageReader reader) {
		Long taskId = null;
		List<Long> parentEventIds = null;
		
		int hasTaskId = 1;
		while ((hasTaskId = reader.enterBag(taskIdKey)) < 0) {
			// Unknown field, do something
		}
		if (hasTaskId == 0) {
			taskId = ReaderHelpers.firstLong(reader);
			reader.exitBag();
		}
		
		if (reader.enterBag(parentEventIdsKey)) {
			parentEventIds = ReaderHelpers.longs(reader);
			reader.exitBag();
		}
		return new XTrace(taskId, parentEventIds, reader.didOverflow());
	}
	
	

}
