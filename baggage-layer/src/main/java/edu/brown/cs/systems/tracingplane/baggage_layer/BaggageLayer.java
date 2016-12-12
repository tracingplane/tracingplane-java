package edu.brown.cs.systems.tracingplane.baggage_layer;

import java.nio.ByteBuffer;
import java.util.List;
import edu.brown.cs.systems.tracingplane.atom_layer.AtomLayer;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey.BagPath;

/**
 * <p>
 * The BaggageLayer is an implementation of the AtomLayer and protocol for storing data in Baggage atoms. The static
 * methods in {@link BaggageContents} offer access to the data contained in a Baggage instance
 * </p>
 * 
 * <p>
 * The BaggageLayer protocol provides a generic, general-purpose map for user data. Data can be stored under keys or
 * paths (e.g., "xtrace.taskId") and accessed accordingly. The BaggageLayer protocol defines how to translate between
 * data stored in a map and data stored as atoms. The atom representation of Baggage is amenable to AtomLayer merging,
 * such that two baggages with different values for a field will correctly merge agnostic to the semantics of the layer.
 * </p>
 *
 * @param <B> Some implementation of {@link BaggageContents} used by this transit layer.
 */
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
