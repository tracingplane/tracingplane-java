package brown.tracingplane.baggageprotocol;

import java.nio.ByteBuffer;
import brown.tracingplane.atomlayer.AtomLayerOverflow;
import brown.tracingplane.baggageprotocol.AtomPrefixes.DataPrefix;

public class BaggageProtocol {
    
    public static final ByteBuffer TRIM_MARKER = ByteBuffer.wrap(new byte[] { DataPrefix.prefix });
    public static final ByteBuffer OVERFLOW_MARKER = AtomLayerOverflow.OVERFLOW_MARKER;

}
