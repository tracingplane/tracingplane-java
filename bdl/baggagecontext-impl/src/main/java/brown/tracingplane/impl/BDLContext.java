package brown.tracingplane.impl;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Lists;
import brown.tracingplane.BaggageContext;
import brown.tracingplane.BaggageListener;
import brown.tracingplane.atomlayer.AtomLayerOverflow;
import brown.tracingplane.atomlayer.Lexicographic;
import brown.tracingplane.atomlayer.StringUtils;
import brown.tracingplane.baggageprotocol.BagKey;
import brown.tracingplane.baggageprotocol.BaggageReader;
import brown.tracingplane.baggageprotocol.BaggageWriter;
import brown.tracingplane.bdl.Bag;
import brown.tracingplane.impl.BaggageHandlerRegistry.Registrations;

/**
 * <p>
 * The full-featured {@link BaggageContext} implementation used by the tracing plane using atoms and the baggage
 * protocol, intended for use in conjunction with BDL-compiled accessor classes.
 * </p>
 * 
 * <p>
 * The typical way to store and manipulate data within {@link BDLContext} instances is to compile a BDL delcaration
 * using the BDL compiler. The generated objects will provide accessor methods for getting data out of
 * {@link BDLContext} instances.
 * </p>
 * 
 * <p>
 * Note: BDL-compiled objects must be registered with a static bag number in order to be used. In concept, this is
 * similar to how protocols get mapped to ports (e.g., SSH maps to port 22, etc.). To register a bag, either specify it
 * as a command line configuration value:
 * 
 * <pre>
 * -Dbag.22=my.compiled.object.MyObject
 * </pre>
 * 
 * or in your <code>application.conf</code>:
 * 
 * <pre>
 * bag.22 = "my.compiled.object.MyObject"
 * </pre>
 * 
 * or statically in code:
 * 
 * <pre>
 * BaggageHandlerRegistry.add(BagKey.indexed(22), my.compiled.object.MyObject.Handler.instance);
 * </pre>
 * </p>
 * 
 * <p>
 * If you don't register a handler to a bag number, then any data found within a {@link BDLContext} will be kept in
 * atom-form. It will be correctly propagated, however, it will not be accessible within this process in object form.
 * </p>
 * 
 * <p>
 * All data within a {@link BDLContext} has an underlying atom-based representation, and the serialization methods in
 * {@link BDLContextProvider} transform contexts to and from this atom-based representation. In memory,
 * {@link BDLContext} data is maintained in object representation.
 * </p>
 * 
 * <p>
 * This class provides some static methods for registering {@link BaggageListener} instances.
 * </p>
 * 
 * <p>
 * This class also provides some utility methods for attaching objects to {@link BDLContext} instances. Attached objects
 * do not propagate across network communication; it is purely to piggyback additional state
 * </p>
 * 
 */
public class BDLContext implements BaggageContext {

    static final Logger log = LoggerFactory.getLogger(BDLContext.class);

    /**
     * Implementation notes:
     * 
     * Handlers are statically registered and are initially populated from the config values. Further handlers can be
     * added at runtime but they will only take effect for baggage that is parsed in future (e.g., there might be some
     * baggage floating around in this process with atoms that could be parsed by the new handler, but we make no
     * attempt to do so).
     * 
     * Note: have not actually implemented the ability to add more handlers at runtime.
     * 
     * The bags variable stores non-null objects that we have parsed.
     * 
     * In future it would be nice to make the following improvement:
     * 
     * Currently, for prototype / speed of development, baggage buffers does not generate field accessors (ie, it
     * returns an object and you directly set the fields on the object). As a result, we do not get information about
     * whether a user is just reading fields of an object or if they are actually modifying the fields. If they are only
     * reading the fields, we could do referencing counting and avoid copying objects when branching. If we have
     * accessors to access the data, this makes it easy to track. It also means when we modify a field, if there are
     * multiple references to the baggage, we need to make a copy, but would only need to copy the path to that field
     * rather than the whole baggage.
     * 
     */

    Map<BagKey, Bag> bags = null;
    List<ByteBuffer> overflowAtoms = null;
    List<ByteBuffer> unprocessedAtoms = null;
    Map<Object, Object> attachments = null;

    /** Get the value mapped to a key, or null if no mapping */
    public Bag get(BagKey key) {
        return bags == null ? null : bags.get(key);
    }

    /** Remove the mapping for the specified key. Returns this object */
    public BDLContext remove(BagKey key) {
        if (bags != null) {
            bags.remove(key);
        }
        return this;
    }

    /** Set the value for a key. Returns this object */
    public BDLContext put(BagKey key, Bag value) {
        if (value == null) {
            remove(key);
        } else {
            if (bags == null) {
                bags = new TreeMap<>();
            }
            bags.put(key, value);
        }
        return this;
    }

    /**
     * Gets the attachment mapped to the specified key, if there is one; returns null otherwise.
     */
    public Object getAttachment(Object key) {
        if (key != null && attachments != null) {
            return attachments.get(key);
        }
        return null;
    }

    /**
     * Removes the object mapped to the specified key. Attachments are not to be confused with bags; attachments are
     * just additional application-level objects that can be attached to {@link BDLContexts}, that are not included with
     * the context during serialization.
     */
    public BDLContext detach(Object key) {
        if (key != null) {
            if (attachments != null) {
                attachments.remove(key);
            }
        }
        return this;
    }

    /**
     * Maps the provided key to the provided value. The key-value pair will be carried in this {@link BDLContext} while
     * it is propagated within the current process. The k-v pair will not be included when the {@link BDLContext} is
     * serialized. Attachments are not to be confused with bags; attachments are just additional application-level
     * objects that can be attached to {@link BDLContexts}, that are not included with the context during serialization.
     */
    public BDLContext attach(Object key, Object value) {
        if (key != null) {
            if (value == null) {
                detach(key);
            } else {
                if (attachments == null) {
                    attachments = new HashMap<>(); // this could be done in the style of Go's context.context package,
                                                   // with a linked list
                }
                attachments.put(key, value);
            }
        }
        return this;
    }

    /**
     * Removes all attachments from this {@link BDLContext}.
     */
    public void clearAttachments() {
        attachments = null;
    }

    BaggageWriter serialize() {
        BaggageWriter writer = BaggageWriter.createAndMergeWith(overflowAtoms, unprocessedAtoms);

        for (BagKey key : bags.keySet()) {
            Bag bag = bags.get(key);
            if (bag != null) {
                writer.enter(key);
                bag.handler().serialize(writer, bag);
                writer.exit();
            }
        }

        return writer;
    }

    BDLContext mergeWith(BDLContext second) {
        if (second == null) return this;

        if (bags == null) {
            bags = second.bags;
        } else if (second.bags != null) {
            for (BagKey key : second.bags.keySet()) {
                Bag firstBag = bags.get(key);
                if (firstBag == null) {
                    bags.put(key, second.bags.get(key));
                } else {
                    bags.put(key, firstBag.handler().join(firstBag, second.bags.get(key)));
                }
            }
        }

        overflowAtoms = AtomLayerOverflow.mergeOverflowAtoms(overflowAtoms, second.overflowAtoms);
        unprocessedAtoms = Lexicographic.merge(unprocessedAtoms, second.unprocessedAtoms);
        // Ignore attachments of second
        return this;
    }

    BDLContext branch() {
        BDLContext other = new BDLContext();
        if (bags != null) {
            other.bags = new TreeMap<>();
            for (BagKey key : bags.keySet()) {
                Bag bag = bags.get(key);
                other.bags.put(key, bag.handler().branch(bag));
            }
        }
        other.overflowAtoms = overflowAtoms;
        other.unprocessedAtoms = unprocessedAtoms;

        if (attachments != null) {
            other.attachments = new HashMap<>(attachments);
        }

        return other;
    }

    /** Parse an instance of BDLContext from the provided atoms */
    static BDLContext parseFrom(BaggageHandlerRegistry registry, BaggageReader reader) {
        if (reader == null) {
            return null;
        }

        // Parse the contents that we have handlers for
        BDLContext bbcontents = null;
        Registrations reg = registry.registrations;
        for (int i = 0, len = reg.keys.length; i < len; i++) {
            BagKey key = reg.keys[i];
            if (reader.enter(key)) {
                Bag parsed = reg.handlers[i].parse(reader);
                if (parsed == null) {
                    continue;
                }
                if (bbcontents == null) {
                    bbcontents = new BDLContext();
                }
                bbcontents.put(key, parsed);
                reader.exit();
            }
        }

        // Save path to overflow atom if it overflowed
        List<ByteBuffer> overflowAtoms = reader.overflowAtoms();
        if (overflowAtoms != null) {
            if (bbcontents == null) {
                bbcontents = new BDLContext();
            }
            bbcontents.overflowAtoms = overflowAtoms;
        }

        // Save atoms that weren't processed
        List<ByteBuffer> unprocessedAtoms = reader.unprocessedAtoms();
        if (unprocessedAtoms != null) {
            if (bbcontents == null) {
                bbcontents = new BDLContext();
            }
            bbcontents.unprocessedAtoms = unprocessedAtoms;
        }

        return bbcontents;
    }

    @Override
    public String toString() {
        List<String> lines = Lists.newArrayList();
        if (bags != null) {
            for (BagKey key : bags.keySet()) {
                Bag bag = bags.get(key);
                lines.add(String.format("%s: %s", key, bag));
            }
        }
        if (attachments != null) {
            for (Object key : attachments.keySet()) {
                Object value = attachments.get(key);
                lines.add(String.format("attachment %s(%s) = %s(%s)", key.getClass().getSimpleName(), key,
                                        value.getClass().getSimpleName(), value));
            }
        }
        return StringUtils.join(lines, "\n");
    }

}
