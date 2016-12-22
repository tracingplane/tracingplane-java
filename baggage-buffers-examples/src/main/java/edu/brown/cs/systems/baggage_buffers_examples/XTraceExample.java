package edu.brown.cs.systems.baggage_buffers_examples;

import java.nio.ByteBuffer;
import java.util.List;
import com.google.common.collect.Sets;
import edu.brown.cs.systems.tracingplane.atom_layer.types.Lexicographic;
import edu.brown.cs.systems.tracingplane.atom_layer.types.TypeUtils;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageReader;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageWriter;
import edu.brown.xtrace.XTraceMetadata;

public class XTraceExample {
    
    public static void main(String[] args) {
        
        
        XTraceMetadata xmd = new XTraceMetadata();
        
        xmd.taskId = 100L;
        xmd.parentEventIds = Sets.<Long>newHashSet(1000L, 300L, 200L);
        

        XTraceMetadata xmd2 = new XTraceMetadata();
        
        xmd2.taskId = 100L;
        xmd2.parentEventIds = Sets.<Long>newHashSet(1000L, 500L);
        

        BaggageWriter writer = BaggageWriter.create();
        
        XTraceMetadata.Handler.instance.serialize(writer, xmd);
        
        BaggageWriter writer2 = BaggageWriter.create();
        
        XTraceMetadata.Handler.instance.serialize(writer2, xmd2);
        
        List<ByteBuffer> atoms = Lexicographic.merge(writer.atoms(), writer2.atoms());
        
        System.out.println(TypeUtils.toHexString(atoms));
        
        BaggageReader reader = BaggageReader.create(atoms);
        
        XTraceMetadata xmd3 = XTraceMetadata.Handler.instance.parse(reader);
        
        System.out.println(xmd3.taskId);
        for (Long parentId : xmd3.parentEventIds) {
            System.out.println(parentId);
        }
        
    }

}
