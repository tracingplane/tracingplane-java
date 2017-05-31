package brown.tracingplane.bdl;

import java.nio.ByteBuffer;
import brown.tracingplane.baggageprotocol.ElementReader;
import brown.tracingplane.baggageprotocol.ElementWriter;
import brown.tracingplane.bdl.Parser.ElementParser;
import brown.tracingplane.bdl.Serializer.ElementSerializer;

/**
 * BDL supports structs, which are simply several data types combined into a single binary blob and
 * encoded as one atom.
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

    public interface StructHandler<S>
            extends StructReader<S>, StructWriter<S>, StructSizer<S>, ElementParser<S>, ElementSerializer<S> {

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
