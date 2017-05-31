package brown.tracingplane.baggageprotocol;

import java.nio.ByteBuffer;

/**
 * Iterator-like interface for writing atoms.
 */
public interface ElementWriter {

    public ByteBuffer newDataAtom(int expectedSize);

    public BaggageWriter writeBytes(ByteBuffer buf);

    public void flush();

    public void sortData();

}