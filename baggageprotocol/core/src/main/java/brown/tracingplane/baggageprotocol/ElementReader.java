package brown.tracingplane.baggageprotocol;

import java.nio.ByteBuffer;

public interface ElementReader {
    public boolean hasData();

    public ByteBuffer nextData();

    public void dropData();

    public void keepData();
}
