package brown.tracingplane.impl;

import java.nio.ByteBuffer;
import brown.tracingplane.ActiveBaggage;
import brown.tracingplane.BaggageContext;
import brown.tracingplane.TransitLayer;

/**
 * <p>
 * A {@link TransitLayer} that does nothing. This is the default {@link TransitLayer} implementation that will be used
 * in lieu of anything else. If this is the {@link TransitLayer} implementation used, the effect is that any calls to
 * the {@link ActiveBaggage} interface will find that there is no active baggage.
 * </p>
 * 
 * <p>
 * {@link NoOpTransitLayer} is typically going to be used at instrumentation time, prior to binding to a particular
 * implementation.
 * </p>
 * 
 * <p>
 * If you intend to use a different instrumentation library altogether to propagate contexts (e.g., OpenTracing), then
 * you would either:
 * <ul>
 * <li>Implement a {@link TransitLayer} that opaquely proxies to your other instrumentation library</li>
 * <li>Not use this library at all</li>
 * </ul>
 * </p>
 */
public class NoOpTransitLayer implements TransitLayer {

    @Override
    public void discard() {}

    @Override
    public BaggageContext branch() {
        return null;
    }

    @Override
    public byte[] branchBytes() {
        return null;
    }

    @Override
    public void join(BaggageContext otherContext) {}

    @Override
    public void join(ByteBuffer serializedContext) {}

    @Override
    public void join(byte[] serialized, int offset, int length) {}

    @Override
    public void set(BaggageContext baggage) {}

    @Override
    public void set(ByteBuffer serializedContext) {}

    @Override
    public void set(byte[] serialized, int offset, int length) {}

    @Override
    public BaggageContext take() {
        return null;
    }

    @Override
    public byte[] takeBytes() {
        return null;
    }

    @Override
    public BaggageContext peek() {
        return null;
    }

    @Override
    public void update(BaggageContext baggage) {}

}
