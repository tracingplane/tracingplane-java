package brown.tracingplane;

import java.io.Closeable;
import java.util.function.BiFunction;
import java.util.function.Function;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;
import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayer;
import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayerCallbacks;
import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayerCallbacks.TransitLayerCallbackRegistry;

/**
 * <p>
 * Manages user-supplied {@link BaggageListeners} that were registered (eg, with methods
 * {@link BaggageProvider#onBranch(BranchListener)}). This class is intended for use by the {@link BaggageProvider}s and
 * simply acts as a utility class.
 */
class ListenerRegistry<B extends Baggage> {

    /**
     * Implementation notes: implemented as a linked list. Adding and removing handlers is synchronized, but recursing
     * through the handlers is not, which means it's possible that a handler will continue to be invoked even if it has
     * been removed.
     * 
     * I'm sure there's a better way of doing this
     */

    RegisteredTransitHandler head;

    public ListenerRegistry(Function<B, B> branch, BiFunction<B, B, B> join) {
        this.head = new RegisteredTransitHandler(x -> x, x -> x, branch, join);
    }

    /**
     * Invokes the {@link BaggageListeners#postNewInstance(Baggage)} methods for all registered handlers, and returns
     * the resulting {@link Baggage} object.
     * 
     * @param instance a newly created baggage instance, possibly null
     * @return a baggage instance that has potentially been updated by registered handlers
     */
    public B postNewInstance(B instance) {
        return head.postNewInstance.apply(instance);
    }

    /**
     * Invokes the {@link BaggageListeners#preDiscard(Baggage)} methods for all registered handlers, and returns the
     * resulting {@link Baggage} object.
     * 
     * @param baggage a baggage instance
     * @return a baggage instance that has potentially been updated by registered handlers
     */
    public B preDiscard(B baggage) {
        return head.preDiscard.apply(baggage);
    }

    /**
     * Invokes the {@link TransitLayer#join(Baggage, Baggage)} method that was provided to this
     * {@link TransitLayerCallbackRegistry} instance on construction.
     * 
     * The call to that join method is wrapped with calls to {@link BaggageListeners#join(Baggage, Baggage, BiFunction)}
     * for all registered handlers, which may decorate or change the default join behavior.
     * 
     * @param left a baggage instance, possibly null
     * @param right a baggage instance, possibly null
     * @return a baggage instance with merged contents from <code>left</code> and <code>right</code>
     */
    public B join(B left, B right) {
        return head.join.apply(left, right);
    }

    /**
     * Invokes the {@link TransitLayer#branch(Baggage)} method that was provided to this
     * {@link TransitLayerCallbackRegistry} instance on construction.
     * 
     * The call to that branch method is wrapped with calls to {@link BaggageListeners#branch(Baggage, Function)} for
     * all registered handlers, which might change or decorate the default branch behavior.
     * 
     * @param from a baggage instance
     * @return a baggage instance branched from <code>from</code>, possibly null
     */
    public B branch(B from) {
        return head.branch.apply(from);
    }

    /**
     * Registers a {@link BaggageListeners} to be invoked whenever any transit layer operation occurs
     * 
     * @param handler a handler to be invoked whenever any transit layer operation occurs
     * @return a {@link Closeable} that will remove the registration when {@link Closeable#close()} is called.
     */
    public synchronized Closeable add(BaggageListeners handler) {
        RegisteredTransitHandler registration = new RegisteredTransitHandler(handler);
        registration.next = head;
        head = registration;
        return () -> remove(registration);
    }

    /**
     * Removes a registered {@link BaggageListeners}
     */
    synchronized void remove(RegisteredTransitHandler registration) {
        if (head == registration) {
            // Special case: handler is the first one registered
            head = head.next;
        } else {
            // Recurse through linked list and remove if found
            RegisteredTransitHandler prev = head;
            while (prev.next != null) {
                if (prev.next == registration) {
                    prev.next = prev.next.next;
                    break;
                }
                prev = prev.next;
            }
        }
    }

    /**
     * Represents either a registered {@link BaggageListeners} or is the root behavior provided by a
     * {@link TransitLayer} on construction.
     */
    private class Registration {
        RegisteredTransitHandler next = null;

        final Function<B, B> postNewInstance;
        final Function<B, B> preDiscard;
        final Function<B, B> branch;
        final BiFunction<B, B, B> join;

        RegisteredTransitHandler(Function<B, B> postNewInstance, Function<B, B> preDiscard, Function<B, B> branch,
                                 BiFunction<B, B, B> join) {
            this.postNewInstance = postNewInstance;
            this.preDiscard = preDiscard;
            this.branch = branch;
            this.join = join;
        }

        RegisteredTransitHandler(BaggageListeners handler) {
            this.postNewInstance = (newInstance) -> handler.postNewInstance(next.postNewInstance.apply(newInstance));
            this.preDiscard = (toDiscard) -> next.preDiscard.apply(handler.preDiscard(toDiscard));
            this.branch = (from) -> handler.branch(from, next.branch);
            this.join = (left, right) -> handler.join(left, right, next.join);
        }
    }

}