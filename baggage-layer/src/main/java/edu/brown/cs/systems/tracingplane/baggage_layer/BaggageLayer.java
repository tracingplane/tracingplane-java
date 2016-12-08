package edu.brown.cs.systems.tracingplane.baggage_layer;

import java.nio.ByteBuffer;
import java.util.List;
import edu.brown.cs.systems.tracingplane.atom_layer.AtomLayer;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey.BagPath;

//TODO: description and method documentation
public interface BaggageLayer<B extends BaggageContents> extends AtomLayer<B> {

    public boolean checkOverflow(B baggage, BagPath path, boolean includeChildren, boolean includeTrimmed);

    public boolean exists(B baggage, BagPath path);

    public List<ByteBuffer> get(B baggage, BagPath path);

    public void add(B baggage, BagPath path, ByteBuffer data);

    public void add(B baggage, BagPath path, List<ByteBuffer> datas);

    public void replace(B baggage, BagPath path, ByteBuffer data);

    public void replace(B baggage, BagPath path, List<ByteBuffer> datas);

    public void clear(B baggage, BagPath path);

    public void drop(B baggage, BagPath path);

    public List<BagKey> children(B baggage, BagPath path);

}
