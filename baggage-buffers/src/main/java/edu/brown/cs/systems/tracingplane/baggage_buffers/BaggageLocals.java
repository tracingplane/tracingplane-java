package edu.brown.cs.systems.tracingplane.baggage_buffers;

import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;
import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayerCallbacks.TransitHandler;

/**
 * {@link BaggageLocals} provides methods for attaching additional arbitrary {@link Objects} to {@link Baggage}
 * instances. The {@link Objects} will be propagated with the {@link Baggage} within the current process. They have
 * simple propagation rules as follows:
 * 
 * <ul>
 * <li><b>Branch:</b> the attachment is copied by reference, so be aware that you could end up with concurrent
 * modifications</li>
 * <li><b>Join:</b> only the attachment from the left side of the join is retained; the right side attachment is
 * discarded
 * <li><b>Serialize and Deserialize:</b> attachments are not included in serialized representations</li>
 * </ul>
 * 
 * The branch and join behaviors can be overridden by creating a {@link TransitHandler} and registering it with
 * {@link BaggageBuffers#registerCallbackHandler(TransitHandler)}
 */
public class BaggageLocals {

    private BaggageLocals() {}

    /**
     * Remove the object attached to the provided baggage under the specified key
     * 
     * @param baggage a baggage instance, possibly null
     * @param key a key
     * @return the same baggage instance
     */
    public static Baggage removeAttachment(Baggage baggage, Object key) {
        if (key != null && baggage instanceof BaggageBuffersContents) {
            ((BaggageBuffersContents) baggage).detach(key);
        }
        return baggage;
    }

    /**
     * Attaches an object to the baggage.  Replaces any existing attachment for this key.
     * 
     * The default behavior for attached objects is:
     * <ul>
     * <li><b>Branch:</b> the attachment is copied by reference, so be aware that you could end up with concurrent
     * modifications</li>
     * <li><b>Join:</b> only the attachment from the left side of the join is retained; the right side attachment is
     * discarded
     * <li><b>Serialize and Deserialize:</b> attachments are not included in serialized representations</li>
     * </ul>
     * 
     * To override behavior for these methods you must implement a {@link TransitHandler} and register it with
     * {@link BaggageBuffers#registerCallbackHandler(TransitHandler)}
     * 
     * @param baggage the baggage instance to modify
     * @param key the key to set
     * @param value the new value to map to this key
     * @return a possibly new baggage instance with the updated mapping
     */
    public static Baggage attachObject(Baggage baggage, Object key, Object value) {
        if (key != null) {
            if (baggage instanceof BaggageBuffersContents) {
                return ((BaggageBuffersContents) baggage).attach(key, value);
            } else {
                return new BaggageBuffersContents().attach(key, value);
            }
        } else {
            return baggage;
        }
    }

    /**
     * Get the value attached to the baggage for the specified key
     * @param baggage the baggage instance to check
     * @param key the attachment key
     * @return the attached value, or none if nothing is attached
     */
    public static Object getAttachment(Baggage baggage, Object key) {
        if (baggage instanceof BaggageBuffersContents) {
            return ((BaggageBuffersContents) baggage).getAttachment(key);
        }
        return null;
    }

}
