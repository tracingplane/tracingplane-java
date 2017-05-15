package brown.tracingplane.baggageprotocol;

import java.nio.ByteBuffer;

/** Used to restrict usage of writer */
public interface ElementWriter {

    public ByteBuffer newDataAtom(int expectedSize);

    public BaggageWriter writeBytes(ByteBuffer buf);

    public void flush();

    public void sortData();

}