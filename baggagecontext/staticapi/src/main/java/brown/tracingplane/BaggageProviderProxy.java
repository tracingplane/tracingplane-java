package brown.tracingplane;

import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Wraps a {@link BaggageProvider} and provides equivalent methods but on generic {@link BaggageContext} instances
 * rather than any specific subclass. Logs errors and returns null if the types provided are incompatible with the
 * wrapped provider's types.
 * </p>
 */
@SuppressWarnings("unchecked")
class BaggageProviderProxy<B extends BaggageContext> implements BaggageProvider<BaggageContext> {

    private static final Logger log = LoggerFactory.getLogger(BaggageProviderProxy.class);

    private final BaggageProvider<B> provider; // The wrapped provider implementation
    
    static <B extends BaggageContext> BaggageProvider<BaggageContext> wrap(BaggageProvider<B> provider) {
        return new BaggageProviderProxy<B>(provider);
    }

    private BaggageProviderProxy(BaggageProvider<B> provider) {
        this.provider = provider;
    }

    @Override
    public boolean isValid(BaggageContext baggage) {
        return provider.isValid(baggage);
    }

    @Override
    public BaggageContext newInstance() {
        return provider.newInstance();
    }

    @Override
    public void discard(BaggageContext baggage) {
        if (provider.isValid(baggage)) {
            provider.discard((B) baggage);
        } else {
            log.warn("discarding incompatible baggage to {}.discard, baggage class is {}",
                     provider.getClass().getName(), baggage.getClass().getName());
        }
    }

    @Override
    public BaggageContext branch(BaggageContext from) {
        if (provider.isValid(from)) {
            return provider.branch((B) from);
        } else {
            log.warn("discarding incompatible baggage to {}.branch; baggage class is {}", provider.getClass().getName(),
                     from.getClass().getName());
        }
        return null;
    }

    @Override
    public BaggageContext join(BaggageContext left, BaggageContext right) {
        if (provider.isValid(left) && provider.isValid(right)) {
            return provider.join((B) left, (B) right);
        } else if (provider.isValid(left)) {
            log.warn("discarding incompatible right baggage to {}.join; left baggage class is {}; right baggage class is {}",
                     provider.getClass().getName(), left.getClass().getName(), right.getClass().getName());
        } else if (provider.isValid(right)) {
            log.warn("discarding incompatible left baggage to {}.join; left baggage class is {}; right baggage class is {}",
                     provider.getClass().getName(), left.getClass().getName(), right.getClass().getName());
        } else {
            log.warn("discarding incompatible baggage to {}.join; left baggage class is {}; right baggage class is {}",
                     provider.getClass().getName(), left.getClass().getName(), right.getClass().getName());
        }
        return null;
    }

    @Override
    public BaggageContext deserialize(byte[] serialized, int offset, int length) {
        return provider.deserialize(serialized, offset, length);
    }

    @Override
    public BaggageContext deserialize(ByteBuffer buf) {
        return provider.deserialize(buf);
    }

    @Override
    public byte[] serialize(BaggageContext baggage) {
        if (provider.isValid(baggage)) {
            return provider.serialize((B) baggage);
        } else {
            log.warn("discarding incompatible baggage to {}.serialize; baggage class is {}",
                     provider.getClass().getName(), baggage.getClass().getName());
        }
        return null;
    }

    @Override
    public byte[] serialize(BaggageContext baggage, int maximumSerializedSize) {
        if (provider.isValid(baggage)) {
            return provider.serialize((B) baggage, maximumSerializedSize);
        } else {
            log.warn("discarding incompatible baggage to {}.serialize; baggage class is {}",
                     provider.getClass().getName(), baggage.getClass().getName());
        }
        return null;
    }

}
