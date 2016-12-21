package edu.brown.cs.systems.tracingplane.baggage_buffers;

import edu.brown.cs.systems.tracingplane.baggage_layer.BaggageLayer;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageReader;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageWriter;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;

/**
 * <p>
 * The final layer in the baggage stack. BaggageBuffers is an implementation of the BaggageLayer. It enables you to
 * register baggage-buffers compiled classes then access them through the generated accessor methods.
 * </p>
 */
public class BaggageBuffers implements BaggageLayer<BaggageBuffersContents> {

    @Override
    public boolean isInstance(Baggage baggage) {
        return baggage == null || baggage instanceof BaggageBuffersContents;
    }

    @Override
    public BaggageBuffersContents newInstance() {
        return null;
    }

    @Override
    public void discard(BaggageBuffersContents baggage) {}

    @Override
    public BaggageBuffersContents branch(BaggageBuffersContents from) {
        return from == null ? null : from.branch();
    }

    @Override
    public BaggageBuffersContents join(BaggageBuffersContents left, BaggageBuffersContents right) {
        if (left == null) {
            return right;
        } else if (right == null) {
            return left;
        } else {
            return left.mergeWith(right);
        }
    }

    @Override
    public BaggageBuffersContents read(BaggageReader reader) {
        return BaggageBuffersContents.parseFrom(reader);
    }

    @Override
    public BaggageWriter write(BaggageBuffersContents instance) {
        return instance == null ? null : instance.serialize();
    }

}
