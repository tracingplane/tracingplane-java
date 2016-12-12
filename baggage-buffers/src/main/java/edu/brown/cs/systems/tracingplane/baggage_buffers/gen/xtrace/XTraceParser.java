package edu.brown.cs.systems.tracingplane.baggage_buffers.gen.xtrace;

import java.util.List;
import edu.brown.cs.systems.tracingplane.baggage_buffers.impl.ReaderHelpers;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageReader;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.Parser;

public class XTraceParser implements Parser<XTrace> {

    public static final BagKey taskIdKey = BagKey.indexed(0);
    public static final BagKey parentEventIdsKey = BagKey.indexed(1);

    @Override
    public XTrace parse(BaggageReader reader) {
        Long taskId = null;
        List<Long> parentEventIds = null;

        if (reader.enter(taskIdKey)) {
            taskId = ReaderHelpers.firstLong(reader);
            reader.exit();
        }

        if (reader.enter(parentEventIdsKey)) {
            parentEventIds = ReaderHelpers.longs(reader);
            reader.exit();
        }

        return new XTrace(taskId, parentEventIds, reader.didOverflow());
    }

}
