package edu.brown.cs.systems.tracingplane.transit_layer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayerUtils.TransitAccessListener;

/**
 * <p>
 * Baggage is a general purpose request context. The purpose of Baggage is to provide a way to propagate arbitrary
 * request context alongside a request as the request traverses different thread, process, machine, and application
 * boundaries. This interface -- {@link Baggage} -- is the base unit of context in the tracing plane. Everything that is
 * propagated by the tracing plane ultimately inherits from this interface. Thus, when using Baggage in application
 * code, most variables will be of type Baggage, and most method calls will be to static methods in this interface.
 * </p>
 * 
 * <p>
 * This interface is the {@link TransitLayer} interface for Baggage. This interface has the core {@link TransitLayer}
 * static methods to enable you to create new Baggage instances; serialize and deserialize Baggage instances; create
 * copies to pass to new threads or attach to continuations; and merge multiple contexts from joining branches (eg, when
 * multiple threads join, when parent continuations complete and trigger a child continuation; when a message is
 * received from the network).
 * </p>
 * 
 * <p>
 * The sole purpose of this layer is to make sure that request context follows the path of request execution. When a new
 * thread is created to process a request, Baggage should be copied to the new thread. When a network request is made,
 * serialized Baggage should be included with the request. When a worker thread completes execution for a request and
 * moves on to the next one, it should drop the previous request's Baggage. In general, when using this layer, you
 * should be thinking more about "what is the logical extent of this request?" and less about
 * "I want to include X, Y, Z IDs with my request". Other layers -- e.g., {@code AtomLayer} and {@code BaggageLayer} --
 * provide implementations and interfaces for the <i>contents</i> of Baggage.
 * </p>
 * 
 * <p>
 * Manipulation of Baggage should be done via the static methods in this class -- eg, {@link #newInstance() newInstance}
 * , {@link #discard(Baggage) discard}, {@link #branch(Baggage) branch}, {@link #join(Baggage, Baggage) join},
 * {@link #serialize(Baggage) serialize}, {@link #deserialize(byte[], int, int) deserialize}. The methods of this class
 * can be divided into two groups. The first group of methods require you to explicitly provide Baggage instances as
 * method arguments. The second group of methods does not require explicit Baggage instances as arguments, instead
 * accessing a thread-local variable whose contents contain the current thread's Baggage.
 * </p>
 *
 */
public interface Baggage {

    /**
     * The {@link TransitLayer} implementation installed in the process.
     */
    public static final TransitLayer<?> transit = TransitLayerConfig.defaultTransitLayer();
    
    static final TransitAccessListener listener = new TransitLayerConfig().getTransitAccessListener();

    /**
     * <p>
     * This method does <b>not</b> affect the Baggage set for the current thread.
     * </p>
     * 
     * <p>
     * This method creates and returns a new, empty baggage. Be aware that most {@link TransitLayer} implementations
     * represent empty Baggage using null, so the return value of this method is quite likely to be null.
     * </p>
     * 
     * <p>
     * If you wish set the current thread's baggage to a new instance (and discard anything that was previously set),
     * either of the following approaches can be used instead:
     * </p>
     * <ul>
     * <li>Call {@link #discard()}, which will discard anything previously set, effectively setting the thread's current
     * baggage to a new empty instance, e.g.,<br>
     * {@code Baggage.discard();}</li>
     * <li>Pass the return value of {@link #newInstance()} to {@link #set(Baggage)}, which will overwrite the thread's
     * current baggage with the newly created instance, e.g.,<br>
     * {@code Baggage.set(Baggage.newInstance());}</li>
     * </ul>
     * 
     * @return An empty Baggage instance. The return value can be null, as null corresponds to the empty baggage.
     */
    public static Baggage newInstance() {
        return TransitLayerCompatibility.newInstance(transit);
    }

    /**
     * <p>
     * Gets the Baggage instance, if any, being stored for the current thread.
     * </p>
     * 
     * <p>
     * Be aware that this method does <b>not</b> branch the baggage instance, so in general this method should only be
     * used to, for example, inspect the contents of the baggage. You can, however, call {@link #branch(Baggage)}
     * manually if you wish to continue to use the Baggage.
     * </p>
     * 
     * @return a Baggage instance, possibly null
     */
    public static Baggage get() {
        boolean entered = listener.enter();
        Baggage baggage = ThreadLocalBaggage.get();
        if (entered) {
            listener.get();
            listener.exit();
        }
        return baggage;
    }

    /**
     * <p>
     * Removes the baggage instance being stored by the current thread, and returns it.
     * </p>
     * 
     * <p>
     * No calls to {@link #branch(Baggage)} are necessary on the returned baggage after calling this method, because the
     * current thread will no longer have access to it.
     * </p>
     * 
     * @return a Baggage instance, possibly null.
     */
    public static Baggage take() {
        boolean entered = listener.enter();
        if (entered) {
            listener.take();
        }
        Baggage baggage = ThreadLocalBaggage.take();
        if (entered) {
            listener.exit();
        }
        return baggage;
    }

    /**
     * <p>
     * Set the baggage being stored by the current thread.
     * </p>
     * 
     * <p>
     * This method does <b>not</b> branch the baggage instance provided, so if you wish to continue to use
     * <code>baggage</code> after calling this method, you should branch it.
     * </p>
     * 
     * @param baggage a Baggage instance, possibly null
     */
    public static void set(Baggage baggage) {
        boolean entered = listener.enter();
        ThreadLocalBaggage.set(baggage);
        if (entered) {
            listener.set();
            listener.exit();
        }
    }

    /**
     * <p>
     * Discards the Baggage set in the current thread. Subsequent interactions with the current thread's baggage will
     * encounter a new empty instance.
     * </p>
     * 
     * <p>
     * If your system has worker threads that process requests in a loop, you should typically call {@link #discard()}
     * at the end of each loop, to ensure that the Baggage belonging to the previous request does not 'leak' to future
     * requests or to maintenance work that is unrelated to the request.
     * </p>
     * 
     * <p>
     * A call to this method is equivalent to {@code Baggage.discard(Baggage.take());}
     * </p>
     * 
     * <p>
     * If you wish to discard a specific Baggage instance and not the one set in the current thread, use
     * {@link #discard(Baggage)}.
     * </p>
     */
    public static void discard() {
        boolean entered = listener.enter();
        if (entered) {
            listener.discard();
        }
        ThreadLocalBaggage.discard();
        if (entered) {
            listener.exit();
        }
    }

    /**
     * 
     * <p>
     * Discards the provided baggage instance, rendering it no longer usable.
     * </p>
     * 
     * <p>
     * This method does <b>not</b> affect Baggage set for the current thread. To discard the current thread's Baggage,
     * use {@link #discard()}.
     * </p>
     * 
     * <p>
     * It is recommended, but not necessary, to call this method if you know that you will not be using this Baggage
     * instance again in future. This has a minor effect of preventing unnecessary Baggage contents copying if the
     * instance was branched and the branched copy is still in use.
     * </p>
     * 
     * @param baggage The Baggage instance to discard. After calling this method you should not use the baggage instance
     *            again.
     */
    public static void discard(Baggage baggage) {
        TransitLayerCompatibility.discard(transit, baggage);
    }

    /**
     * <p>
     * Create a copy of the Baggage set in the current thread.
     * </p>
     * 
     * <p>
     * If a request is being processed concurrently -- for example, by multiple threads, or with multiple work items in
     * a thread pool -- then the concurrent branches should not share one Baggage instance. Instead, concurrent branches
     * should each have their own Baggage instance. This is done by calling {@link #branch()} prior to spinning off the
     * concurrent execution (e.g., before creating the thread or work item) and giving the thread or work item the
     * copied Baggage. Examples of when {@link #branch()} should be called include:
     * </p>
     * <ul>
     * <li>When we create a new thread, branch the current thread's Baggage so that the new thread has its own copy to
     * work with:
     * 
     * <pre>
     * Baggage branchedBaggage = Baggage.branch();
     * Thread t = new Thread() {
     *     public void run() {
     *         Baggage.set(branchedBaggage); 
     *         ...
     *         Baggage.discard();
     *     }
     * }
     * t.start();
     * </pre>
     * 
     * </li>
     * <li>When creating {@link Runnable} instances or similar work objects that will be passed to a {@link Executor} or
     * other thread pool, branch the current thread's Baggage so that each work object has its own copy to work with:
     * 
     * <pre>
     * Baggage branchedBaggage = Baggage.branch();
     * Runnable r = new Runnable() {
     *     public void run() {
     *         Baggage.set(branchedBaggage); 
     *         ...
     *         Baggage.discard();
     *     }
     * }
     * myExecutor.submit(r);
     * </pre>
     * 
     * </li>
     * <li>Making a remote network request, branch the current thread's Baggage then serialize the copy for inclusion in
     * the network request:
     * 
     * <pre>
     * byte[] baggageBytes = Baggage.serialize(Baggage.branch());
     * ... // make network request, include baggageBytes in header
     * </pre>
     * 
     * </li>
     * </ul>
     * 
     * <p>
     * If the concurrent executions later join (e.g., by calling {@link Thread#join()} or awaiting a {@link Future}),
     * their respective Baggage instances should be merged with {@link #join(Baggage)}.
     * </p>
     * 
     * <p>
     * If you wish to branch a specific Baggage instance and not the one set in the current thread, use
     * {@link #branch(Baggage)}.
     * </p>
     * 
     * @return a copy of the baggage that is set for the current thread. Note that the return value can be null (if the
     *         thread's baggage is empty).
     */
    public static Baggage branch() {
        boolean entered = listener.enter();
        if (entered) {
            listener.branch();
        }
        Baggage branched = TransitLayerCompatibility.branch(transit, ThreadLocalBaggage.get());
        if (entered) {
            listener.exit();
        }
        return branched;
    }

    /**
     * <p>
     * Create a copy of the provided Baggage instance.
     * </p>
     * 
     * <p>
     * This method does <b>not</b> affect Baggage set for the current thread. To create a copy of the current thread's
     * Baggage, use {@link #branch()}.
     * </p>
     * 
     * <p>
     * If a request is being processed concurrently -- for example, by multiple threads, or with multiple work items in
     * a thread pool -- then the concurrent branches should not share one Baggage instance. Instead, concurrent branches
     * should each have their own Baggage instance. This is done by calling {@link #branch()} prior to spinning off the
     * concurrent execution (e.g., before creating the thread or work item) and giving the thread or work item the
     * copied Baggage. Examples of when {@link #branch()} should be called include:
     * </p>
     * <ul>
     * <li>When we create a new thread, branch the current thread's Baggage so that the new thread has its own copy to
     * work with:
     * 
     * <pre>
     * Baggage b;
     * Thread t = new Thread() {
     *     public void run() {
     *         Baggage.set(Baggage.branch(b)); 
     *         ...
     *         Baggage.discard();
     *     }
     * }
     * </pre>
     * 
     * </li>
     * <li>When creating {@link Runnable} instances or similar work objects that will be passed to a {@link Executor} or
     * other thread pool, branch the current thread's Baggage so that each work object has its own copy to work with:
     * 
     * <pre>
     * Baggage b;
     * Runnable r = new Runnable() {
     *     public void run() {
     *         Baggage.set(Baggage.branch(b)); 
     *         ...
     *         Baggage.discard();
     *     }
     * }
     * </pre>
     * 
     * </li>
     * <li>Making a remote network request, branch the current thread's Baggage then serialize the copy for inclusion in
     * the network request:
     * 
     * <pre>
     * Baggage b;
     * byte[] baggageBytes = Baggage.serialize(Baggage.branch(b));
     * ... // make network request, include baggageBytes in header
     * </pre>
     * 
     * </li>
     * </ul>
     * 
     * <p>
     * If the concurrent executions later join (e.g., by calling {@link Thread#join()} or awaiting a {@link Future}),
     * their respective Baggage instances should be merged with {@link #join(Baggage, Baggage)}.
     * </p>
     * 
     * @param from a baggage instance to create a copy of, allowed to be null.
     * @return a copy of the baggage instance provided. Note that the return value can be null (if baggage is empty).
     *         This method also might return the same instance provided as argument -- however, the call to branch is
     *         still necessary to update internal reference counters.
     */
    public static Baggage branch(Baggage from) {
        return TransitLayerCompatibility.branch(transit, from);
    }

    /**
     * <p>
     * Merges the baggage that is set in the current thread with the baggage instance provided. After this call, the
     * baggage set in the current thread will contain both its original contents and the contents of <code>other</code>.
     * </p>
     * 
     * <p>
     * <code>join</code> should be used if your application uses multiple threads or runnables to process each request.
     * When the concurrent branches of execution complete, use <code>join</code> to merge back together their contents.
     * Examples of when {@link #join(Baggage)} should be called include:
     * </p>
     * <ul>
     * <li>After a concurrent thread finishes, merge its Baggage after we call {@link Thread#join()}:
     * 
     * <pre>
     * class MyThread extends Thread {
     *     Baggage baggage;
     *     public void run() {
     *         ...
     *         this.baggage = Baggage.take();
     *     }
     * }
     * 
     * MyThread thread;
     * thread.start();
     * ...
     * thread.join();
     * Baggage.join(thread.baggage);
     * </pre>
     * 
     * </li>
     * <li>After a runnable or similar work object completes execution, merge its Baggage:
     * 
     * <pre>
     * {@code 
     * class MyRunnable extends Runnable {
     *     Baggage baggage;
     *     public void run() {
     *         ... 
     *         this.baggage = Baggage.take();
     *     }
     * }
     * 
     * ExecutorService executor;
     * MyRunnable runnable;
     * Future<?> future = executor.submit(runnable);
     * ...
     * future.get();
     * Baggage.join(runnable.baggage);}
     * </pre>
     * 
     * </li>
     * <li>After receiving a remove network request or response to a request, merge the Baggage included in the response
     * with the Baggage of the receiving thread:
     * 
     * <pre>
     * byte[] networkBaggageBytes;
     * Baggage.join(Baggage.deserialize(networkBaggageBytes));
     * </pre>
     * 
     * </li>
     * </ul>
     * 
     * <p>
     * After calling join, <code>other</code> should not be used any further. If you wish to continue to use
     * <code>other</code> then you should branch <code>other</code> first, e.g.,
     * {@code Baggage.join(Baggage.branch(other));}.
     * </p>
     * 
     * <p>
     * If you wish to join two Baggage instances and not the one set in the current thread, use
     * {@link #join(Baggage, Baggage)}.
     * </p>
     * 
     * @param other another Baggage instance, whose contents you wish to merge into the current thread's baggage.
     *            <code>other</code> may be null. After this call, <code>other</code> should not be used.
     */
    public static void join(Baggage other) {
        boolean entered = listener.enter();
        ThreadLocalBaggage.set(TransitLayerCompatibility.join(transit, ThreadLocalBaggage.take(), other));
        if (entered) {
            listener.join();
            listener.exit();
        }
    }

    /**
     * <p>
     * Merges the contents of two baggage instances into a single instance. The baggage returned by this call will
     * contain the contents of both <code>left</code> and <code>right</code>.
     * </p>
     * 
     * <p>
     * This method does <b>not</b> affect Baggage set for the current thread. To merge a baggage instance with the
     * current thread's Baggage, use {@link #join(Baggage)} instead.
     * </p>
     * 
     * <p>
     * <code>join</code> should be used if your application uses multiple threads or runnables to process each request.
     * When the concurrent branches of execution complete, use <code>join</code> to merge back together their contents.
     * Examples of when {@link #join(Baggage, Baggage)} should be called include:
     * </p>
     * <ul>
     * <li>After a concurrent thread finishes, merge its Baggage after we call {@link Thread#join()}:
     * 
     * <pre>
     * class MyThread extends Thread {
     *     Baggage baggage;
     *     public void run() {
     *         ...
     *         this.baggage = Baggage.take();
     *     }
     * }
     * 
     * MyThread threadA, threadB;
     * threadA.start();
     * threadB.start();
     * ...
     * threadA.join();
     * threadB.join();
     * Baggage merged = Baggage.join(threadA.baggage, threadB.baggage);
     * </pre>
     * 
     * </li>
     * <li>After a runnable or similar work object completes execution, merge its Baggage:
     * 
     * <pre>
     * {@code 
     * Baggage completedBaggage;
     * 
     * class MyRunnable extends Runnable {
     *     public void run() {
     *         ... 
     *         completedBaggage = Baggage.join(completedBaggage, Baggage.get());
     *     }
     * }
     * 
     * ExecutorService executor;
     * MyRunnable runnableA, runnableB, runnableC;
     * Future<?> future = executor.submit(runnableA);
     * Future<?> future = executor.submit(runnableB);
     * Future<?> future = executor.submit(runnableC);
     * ...
     * futureA.get();
     * futureB.get();
     * futureC.get();
     * Baggage.set(completedBaggage);}
     * </pre>
     * </ul>
     * 
     * <p>
     * After calling join, <code>left</code> and <code>right</code> should not be used any further. If you wish to
     * continue to use them then you should branch them first, e.g.,
     * {@code Baggage.join(Baggage.branch(left), Baggage.branch(right));}.
     * </p>
     * 
     * @param left a baggage instance, which may be null. After this call, <code>left</code> should not be used again.
     * @param right a baggage instance, which may be null. After this call, <code>right</code> should not be used again.
     * @return a baggage instance a baggage instances whose contents contain the contents of <code>left</code> merged
     *         with the contents of <code>right</code>
     */
    public static Baggage join(Baggage left, Baggage right) {
        return TransitLayerCompatibility.join(transit, left, right);
    }

    /**
     * <p>
     * Deserialize a serialized Baggage instance.
     * </p>
     * 
     * <p>
     * This method does <b>not</b> affect the baggage set for the current thread. If you wish to set or join the
     * deserialized baggage, you must call the corresponding methods, e.g.,<br>
     * {@code Baggage.set(Baggage.deserialize(serialized));} <br>
     * {@code Baggage.join(Baggage.deserialize(serialized));}
     * </p>
     * 
     * <p>
     * If the provided bytes cannot be deserialized (e.g., because they are invalid), then they will be treated as the
     * empty baggage and the return value of this method will be null. However, a return value of null does <b>not</b>
     * imply that the bytes are invalid.
     * </p>
     * 
     * @param serialized the serialized byte representation of Baggage.
     * @return the deserialized Baggage instance, possibly null
     */
    public static Baggage deserialize(byte[] serialized) {
        return deserialize(serialized, 0, serialized.length);
    }

    /**
     * <p>
     * Deserialize a serialized Baggage instance.
     * </p>
     * 
     * <p>
     * This method does <b>not</b> affect the baggage set for the current thread. If you wish to set or join the
     * deserialized baggage, you must call the corresponding methods, e.g.,<br>
     * {@code Baggage.set(Baggage.deserialize(serialized, offset, length));} <br>
     * {@code Baggage.join(Baggage.deserialize(serialized, offset, length));}
     * </p>
     * 
     * <p>
     * If the provided bytes cannot be deserialized (e.g., because they are invalid), then they will be treated as the
     * empty baggage and the return value of this method will be null. However, a return value of null does <b>not</b>
     * imply that the bytes are invalid.
     * </p>
     * 
     * @param serialized the serialized byte representation of Baggage.
     * @param offset offset into the byte array where the Baggage begins
     * @param length length of the baggage bytes
     * @return the deserialized Baggage instance, possibly null
     */
    public static Baggage deserialize(byte[] serialized, int offset, int length) {
        boolean entered = listener.enter();
        Baggage baggage = TransitLayerCompatibility.deserialize(transit, serialized, offset, length);
        if (entered) {
            listener.deserialized(baggage, length);
            listener.exit();
        }
        return baggage;
    }

    /**
     * 
     * <p>
     * Deserialize a serialized Baggage instance by reading it from the provided {@link InputStream}. It is expected
     * that the {@link TransitLayer} implementation will precede the baggage bytes with a length prefix.
     * </p>
     * 
     * <p>
     * This method does <b>not</b> affect the baggage set for the current thread. If you wish to set or join the
     * deserialized baggage, you must call the corresponding methods, e.g.,<br>
     * {@code Baggage.set(Baggage.readFrom(in));} <br>
     * {@code Baggage.join(Baggage.readFrom(in));}
     * </p>
     * 
     * <p>
     * If the provided bytes cannot be deserialized (e.g., because they are invalid), then they will be treated as the
     * empty baggage and the return value of this method will be null. However, a return value of null does <b>not</b>
     * imply that the bytes are invalid.
     * </p>
     * 
     * @param in the stream to read from
     * @return the deserialized Baggage instance, possibly null
     * @throws IOException if an exception occurs reading from {@code in}
     */
    public static Baggage readFrom(InputStream in) throws IOException {
        return TransitLayerCompatibility.readFrom(transit, in);
    }
    
    static final int defaultTrimLength = new TransitLayerConfig().defaultTrim;

    /**
     * <p>
     * Serialize the baggage that is set for the current thread.
     * </p>
     * 
     * @return the serialized representation of the current thread's Baggage. The returned bytes might be null or the
     *         empty byte array, both of which are valid and can be used to indicate the empty Baggage.
     */
    public static byte[] serialize() {
        if (defaultTrimLength > 0) {
            return serialize(defaultTrimLength);
        }
        boolean entered = listener.enter();
        Baggage baggage = ThreadLocalBaggage.get();
        if (entered) {
            listener.serialize(baggage, defaultTrimLength);
        }
        byte[] serialized = TransitLayerCompatibility.serialize(transit, baggage);
        if (entered) {
            listener.exit();
        }
        return serialized;
    }

    /**
     * <p>
     * Serialize the baggage that is set for the current thread.
     * </p>
     * 
     * @return the serialized representation of the current thread's Baggage. The returned bytes might be null or the
     *         empty byte array, both of which are valid and can be used to indicate the empty Baggage.
     */
    public static byte[] serialize(int maxLength) {
        boolean entered = listener.enter();
        Baggage baggage = ThreadLocalBaggage.get();
        if (entered) {
            listener.serialize(baggage, maxLength);
        }
        byte[] serialized = TransitLayerCompatibility.serialize(transit, baggage, maxLength);
        if (entered) {
            listener.exit();
        }
        return serialized;
    }

    /**
     * <p>
     * Serialize a baggage instance to its byte representation.
     * </p>
     * 
     * <p>
     * After calling serialize, <code>baggage</code> <b>can</b> continue to be used. <code>serialize</code> implicitly
     * branches the baggage before serializing.
     * </p>
     * 
     * @param baggage a baggage instance to serialize, possibly null
     * @return the serialized byte representation of the baggage
     */
    public static byte[] serialize(Baggage baggage) {
        if (defaultTrimLength > 0) {
            return serialize(baggage, defaultTrimLength);
        }
        boolean entered = listener.enter();
        if (entered) {
            listener.serialize(baggage, defaultTrimLength);
        }
        byte[] serialized = TransitLayerCompatibility.serialize(transit, baggage);
        if (entered) {
            listener.exit();
        }
        return serialized;
    }

    /**
     * <p>
     * Serialize a baggage instance to its byte representation.
     * </p>
     * 
     * <p>
     * After calling serialize, <code>baggage</code> <b>can</b> continue to be used. <code>serialize</code> implicitly
     * branches the baggage before serializing.
     * </p>
     * 
     * @param baggage a baggage instance to serialize, possibly null
     * @return the serialized byte representation of the baggage
     */
    public static byte[] serialize(Baggage baggage, int maxLength) {
        boolean entered = listener.enter();
        if (entered) {
            listener.serialize(baggage, maxLength);
        }
        byte[] serialized = TransitLayerCompatibility.serialize(transit, baggage, maxLength);
        if (entered) {
            listener.exit();
        }
        return serialized;
    }

    /**
     * <p>
     * Write a baggage instance to an {@link OutputStream}. The {@link TransitLayer} will write the length of the
     * serialized representation then write the bytes.
     * </p>
     * 
     * <p>
     * After calling writeTo, <code>baggage</code> <b>can</b> continue to be used. <code>serialize</code> implicitly
     * branches the baggage before serializing.
     * </p>
     * 
     * @param out an outputstream to write the baggage to
     * @param baggage a baggage instance to write, possibly null. If null, this method will write a zero-length byte
     *            array.
     * @throws IOException propagated from <code>out</code>
     */
    public static void writeTo(OutputStream out, Baggage baggage) throws IOException {
        TransitLayerCompatibility.writeTo(transit, out, baggage);
    }
}
