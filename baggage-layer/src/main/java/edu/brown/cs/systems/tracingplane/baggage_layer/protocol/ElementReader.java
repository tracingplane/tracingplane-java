package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

import java.nio.ByteBuffer;

public interface ElementReader {
    public boolean hasData();
    public ByteBuffer nextData();
    public void dropData();
    public void keepData();
}
