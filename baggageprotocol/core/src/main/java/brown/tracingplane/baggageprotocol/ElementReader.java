package brown.tracingplane.baggageprotocol;

import java.nio.ByteBuffer;

/**
 * Iterator-like interface for reading data atoms
 */
public interface ElementReader {
    public boolean hasData();

    public ByteBuffer nextData();

    public void dropData();

    public void keepData();
}
