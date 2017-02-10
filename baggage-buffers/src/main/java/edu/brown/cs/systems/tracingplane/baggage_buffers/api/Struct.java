package edu.brown.cs.systems.tracingplane.baggage_buffers.api;

import java.nio.ByteBuffer;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.Parser.ElementParser;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.Serializer.ElementSerializer;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.ElementReader;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.ElementWriter;

/**
 * BaggageBuffers generates classes that implement this interface, Struct. They also generate BaggageHandler
 * implementations
 */
public interface Struct {

    StructHandler<?> handler();

    @FunctionalInterface
    public interface StructReader<S> {
        public S readFrom(ByteBuffer buf) throws Exception;
    }
    
    @FunctionalInterface
    public interface StructSizer<S> {
        public int serializedSize(S struct);
    }

    @FunctionalInterface
    public interface StructWriter<S> {
        public void writeTo(ByteBuffer buf, S struct) throws Exception;
    }
    
    public interface StructHandler<S> extends StructReader<S>, StructWriter<S>, StructSizer<S>, ElementParser<S>, ElementSerializer<S> {
        
        @Override
        public default S parse(ElementReader reader) {
            ByteBuffer buf = null;
            S out = null;
            while ((buf = reader.nextData()) != null) {
                try {
                    out = readFrom(buf);
                } catch (Exception e) {
                    // Ignore and continue
                }
                if (out != null) {
                    return out;
                }
            }
            return null;
        }
        
        @Override
        public default void serialize(ElementWriter writer, S instance) {
            if (instance != null) {
                int size = serializedSize(instance);
                ByteBuffer atom = writer.newDataAtom(size);
                try {
                    writeTo(atom, instance);
                } catch (Exception e) {
                    // Leave it for now
                }
            }
        }
        
    }

}
