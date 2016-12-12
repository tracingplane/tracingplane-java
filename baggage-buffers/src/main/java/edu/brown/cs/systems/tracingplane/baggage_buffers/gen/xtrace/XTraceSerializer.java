package edu.brown.cs.systems.tracingplane.baggage_buffers.gen.xtrace;

import java.util.ArrayList;
import java.util.List;
import edu.brown.cs.systems.tracingplane.atom_layer.types.TypeUtils;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageWriter;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.Serializer;

public class XTraceSerializer implements Serializer<XTrace> {

    public static final BagKey taskIdKey = BagKey.indexed(0);
    public static final BagKey parentEventIdsKey = BagKey.indexed(1);

    @Override
    public void serialize(BaggageWriter builder, XTrace instance) {
        if (instance != null) {
            builder.didOverflowHere(instance.overflow);

            if (instance.taskId != null) {
                builder.enter(taskIdKey);
                builder.writeLong(instance.taskId);
                builder.exit();
            }

            if (instance.parentEventIds != null && instance.parentEventIds.size() > 0) {
                builder.enter(parentEventIdsKey);
                for (Long parentEventId : instance.parentEventIds) {
                    builder.writeLong(parentEventId);
                }
                builder.exit();
            }
        }
    }
    
    public static void main(String[] args) {
        List<Long> parentIds = new ArrayList<>();
        parentIds.add(15L);
        XTrace xtrace = new XTrace(7L, parentIds, true);
        
        BaggageWriter writer = BaggageWriter.create();
        new XTraceSerializer().serialize(writer, xtrace);
        writer.atoms();
        
        System.out.println(TypeUtils.toHexString(writer.atoms(), "\n"));
    }

}
