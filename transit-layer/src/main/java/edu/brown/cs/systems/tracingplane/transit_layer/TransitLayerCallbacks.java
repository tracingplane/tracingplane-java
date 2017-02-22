package edu.brown.cs.systems.tracingplane.transit_layer;

import java.io.Closeable;
import java.util.function.BiFunction;
import java.util.function.Function;

public class TransitLayerCallbacks {

    private TransitLayerCallbacks() {}

    /**
     * A transit handler receives callbacks when basic {@link Baggage} operations are invoked:
     * {@link TransitLayer#newInstance}, {@link TransitLayer#discard}, {@link TransitLayer#branch(Baggage)} and
     * {@link TransitLayer#join(Baggage, Baggage)}.
     * 
     * Handlers are installed system-wide and are always invoked even if baggage instances are null.
     */
    public interface TransitHandler {

        /**
         * This function is called immediately after {@link TransitLayer#newInstance()} is called. The newly created
         * {@link Baggage} instance is provided as the argument {@code newInstance}.
         * 
         * This method can modify the provided instance or create a new one
         * 
         * @param newInstance the just-now created {@link Baggage} instance
         * @return a baggage instance; by default returns {@code newInstance}
         */
        public default <B extends Baggage> B postNewInstance(B newInstance) {
            return newInstance;
        }

        /**
         * This function is called immediately before {@link TransitLayer#discard(Baggage)} is called. Unless it is
         * modified, this method should return the Baggage instance it received as its argument {@code toBeDiscarded}.
         * 
         * @param toBeDiscarded a baggage instance
         * @return a baggage instance; by default returns {@code toBeDiscarded}
         */
        public default <B extends Baggage> B preDiscard(B toBeDiscarded) {
            return toBeDiscarded;
        }

        /**
         * Intercepts calls to {@link TransitLayer#branch(Baggage)}.
         * 
         * This method is supplied with {@code wrapped}, which is the underlying implementation of {@code branch}. By
         * default a {@link TransitHandler} just proxies the default behavior; however, it can optionally perform
         * additional logic before or after, or override it completely.
         * 
         * @param from a baggage instance, possibly null
         * @param wrapped the default {@link TransitLayer#branch(Baggage)} function
         * @return a baggage instance branched from <code>from</code>, possibly null
         */
        public default <B extends Baggage> B branch(B from, Function<B, B> wrapped) {
            return wrapped.apply(from);
        }

        /**
         * Intercepts calls to {@link TransitLayer#join(Baggage, Baggage)}.
         * 
         * This method is supplied with {@code wrapped}, which is the underlying implementation of {@code join}. By
         * default a {@link TransitHandler} just proxies the default behavior; however, it can optionally perform
         * additional logic before or after, or override it completely.
         * 
         * @param left a {@link Baggage} instance, possibly null
         * @param right a {@link Baggage} instance, possibly null
         * @param next the default {@link TransitLayer#join(Baggage, Baggage)} function
         * @return a baggage instance with merged contents from <code>left</code> and <code>right</code>
         */
        public default <B extends Baggage> B join(B left, B right, BiFunction<B, B, B> wrapped) {
            return wrapped.apply(left, right);
        }

    }

    /**
     * Manages user-supplied {@link TransitHandler} that intercept invocations of {@link TransitLayer} functions
     * 
     * Users can register {@link TransitHandler} instances that specify additional logic to invoke whenever executions
     * branch and join.
     * 
     * This class will proxy those handler methods, wrapping the underlying {@link TransitLayer} call.
     * 
     * Users will typically not deal directly with this class, instead calling the static {@link TransitLayerCallbacks}
     * methods which will register callbacks with the default {@link TransitLayer} implementation.
     */
    public static class TransitLayerCallbackRegistry<B extends Baggage> {

        /**
         * Implementation notes: implemented as a linked list. Adding and removing handlers is synchronized, but
         * recursing through the handlers is not, which means it's possible that a handler will continue to be invoked
         * even if it has been removed.
         * 
         * I'm sure there's a better way of doing this
         */

        RegisteredTransitHandler head;

        public TransitLayerCallbackRegistry(Function<B, B> branch, BiFunction<B, B, B> join) {
            this.head = new RegisteredTransitHandler(x -> x, x -> x, branch, join);
        }

        /**
         * Invokes the {@link TransitHandler#postNewInstance(Baggage)} methods for all registered handlers, and returns
         * the resulting {@link Baggage} object.
         * 
         * @param instance a newly created baggage instance, possibly null
         * @return a baggage instance that has potentially been updated by registered handlers
         */
        public B postNewInstance(B instance) {
            return head.postNewInstance.apply(instance);
        }

        /**
         * Invokes the {@link TransitHandler#preDiscard(Baggage)} methods for all registered handlers, and returns the
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
         * The call to that join method is wrapped with calls to
         * {@link TransitHandler#join(Baggage, Baggage, BiFunction)} for all registered handlers, which may decorate or
         * change the default join behavior.
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
         * The call to that branch method is wrapped with calls to {@link TransitHandler#branch(Baggage, Function)} for
         * all registered handlers, which might change or decorate the default branch behavior.
         * 
         * @param from a baggage instance
         * @return a baggage instance branched from <code>from</code>, possibly null
         */
        public B branch(B from) {
            return head.branch.apply(from);
        }

        /**
         * Registers a {@link TransitHandler} to be invoked whenever any transit layer operation occurs
         * 
         * @param handler a handler to be invoked whenever any transit layer operation occurs
         * @return a {@link Closeable} that will remove the registration when {@link Closeable#close()} is called.
         */
        synchronized Closeable add(TransitHandler handler) {
            RegisteredTransitHandler registration = new RegisteredTransitHandler(handler);
            registration.next = head;
            head = registration;
            return () -> remove(registration);
        }

        /**
         * Removes a registered {@link TransitHandler}
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
         * Represents either a registered {@link TransitHandler} or is the root behavior provided by a
         * {@link TransitLayer} on construction.
         */
        private class RegisteredTransitHandler {
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

            RegisteredTransitHandler(TransitHandler handler) {
                this.postNewInstance =
                        (newInstance) -> handler.postNewInstance(next.postNewInstance.apply(newInstance));
                this.preDiscard = (toDiscard) -> next.preDiscard.apply(handler.preDiscard(toDiscard));
                this.branch = (from) -> handler.branch(from, next.branch);
                this.join = (left, right) -> handler.join(left, right, next.join);
            }
        }

    }
}
