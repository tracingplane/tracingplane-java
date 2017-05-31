package brown.tracingplane.bdl;

import brown.tracingplane.baggageprotocol.BaggageWriter;

/**
 * <p>
 * Provides serialization, deserialization, branch, and merge logic for a bag type.
 * </p>
 */
public interface BaggageHandler<T extends Bag> extends Parser<T>, Serializer<T>, Joiner<T>, Brancher<T> {

    public boolean isInstance(Bag bag);

    @SuppressWarnings("unchecked")
    public default void serialize(BaggageWriter writer, Bag bag) {
        if (isInstance(bag)) {
            ((Serializer<T>) this).serialize(writer, (T) bag);
        }
    }

    @SuppressWarnings("unchecked")
    public default T join(Bag first, Bag second) {
        if (isInstance(first)) {
            if (isInstance(second)) {
                return ((Joiner<T>) this).join((T) first, (T) second);
            } else {
                return (T) first;
            }
        } else {
            if (isInstance(second)) {
                return (T) second;
            } else {
                return null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public default T branch(Bag from) {
        if (isInstance(from)) {
            return ((Brancher<T>) this).branch((T) from);
        } else {
            return null;
        }
    }

}
