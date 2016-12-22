package edu.brown.cs.systems.tracingplane.baggage_buffers;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import edu.brown.cs.systems.tracingplane.atom_layer.protocol.AtomLayerOverflow;
import edu.brown.cs.systems.tracingplane.atom_layer.types.Lexicographic;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.Bag;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.baggage_layer.BaggageContents;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageReader;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageWriter;

/**
 * <p>
 * BaggageContents that supports multiple applications using baggage simultaneously. The data stored in instances of
 * BaggageBuffersContents can be accessed by the BaggageBuffers-generated APIs.
 * </p>
 * 
 * <p>
 * This class is only responsible for handling the root atoms (eg, at level 0). Users can register other baggage
 * handlers to keys, which will delegate handling behavior for that key to the registered handler. This class expects a
 * statically defined, global mapping of baggage handlers to keys. When interpreting some baggage atoms, this class will
 * look up baggage handlers for each <b>root</b> bag, then use the handler to parse the contents of that bag.
 * </p>
 * 
 * <p>
 * The mapping of baggage handlers to unique keys is similar in concept to the mapping of ports to protocols (e.g., SSH
 * is typically port 22, http is typically port 80). A process doesn't need to know the complete mapping of ports to
 * protocols -- only the protocols that it is going to use. Similarly, if we encounter a bag but do not recognize the
 * key, we will retain the bag's atoms but not process them further.
 * </p>
 * 
 * <p>
 * The mapping of baggage handlers to IDs can be specified in the config or they can be added at runtime. Once a handler
 * is added, all future baggage that is parsed will invoke the handler to create objects from atoms.
 * </p>
 * 
 */
public class BaggageBuffersContents implements BaggageContents {

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
    private Map<BagKey, Bag> bags = null;
    private List<ByteBuffer> overflowAtoms = null;
    private List<ByteBuffer> unprocessedAtoms = null;

    /** Get the value mapped to a key, or null if no mapping */
    public Bag get(BagKey key) {
        return bags == null ? null : bags.get(key);
    }

    /** Remove the mapping for the specified key. Returns this object */
    public BaggageBuffersContents remove(BagKey key) {
        if (bags != null) {
            bags.remove(key);
        }
        return this;
    }

    /** Set the value for a key.  Returns this object */
    public BaggageBuffersContents put(BagKey key, Bag value) {
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

    BaggageBuffersContents mergeWith(BaggageBuffersContents second) {
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
        return this;
    }

    BaggageBuffersContents branch() {
        BaggageBuffersContents other = new BaggageBuffersContents();
        if (bags != null) {
            other.bags = new TreeMap<>();
            for (BagKey key : bags.keySet()) {
                Bag bag = bags.get(key);
                other.bags.put(key, bag.handler().branch(bag));
            }
        }
        other.overflowAtoms = overflowAtoms;
        other.unprocessedAtoms = unprocessedAtoms;
        return other;
    }

    /** Parse an instance of BaggageBuffersContents from the provided atoms */
    static BaggageBuffersContents parseFrom(BaggageReader reader) {
        if (reader == null) {
            return null;
        }

        // Parse the contents that we have handlers for
        BaggageBuffersContents bbcontents = null;
        Registrations reg = Registrations.instance;
        for (int i = 0, len = reg.keys.length; i < len; i++) {
            BagKey key = reg.keys[i];
            if (reader.enter(key)) {
                Bag parsed = reg.handlers[i].parse(reader);
                if (parsed == null) {
                    continue;
                }
                if (bbcontents == null) {
                    bbcontents = new BaggageBuffersContents();
                }
                bbcontents.put(key, parsed);
                reader.exit();
            }
        }

        // Save path to overflow atom if it overflowed
        List<ByteBuffer> overflowAtoms = reader.overflowAtoms();
        if (overflowAtoms != null) {
            if (bbcontents == null) {
                bbcontents = new BaggageBuffersContents();
            }
            bbcontents.overflowAtoms = overflowAtoms;
        }

        // Save atoms that weren't processed
        List<ByteBuffer> unprocessedAtoms = reader.unprocessedAtoms();
        if (unprocessedAtoms != null) {
            if (bbcontents == null) {
                bbcontents = new BaggageBuffersContents();
            }
            bbcontents.unprocessedAtoms = unprocessedAtoms;
        }

        return bbcontents;
    }

}
