package edu.brown.cs.systems.tracingplane.baggage_layer.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import edu.brown.cs.systems.tracingplane.atom_layer.types.Lexicographic;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.baggage_layer.BaggageContents;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageReader;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageWriter;

/**
 * <p>
 * An implementation of baggage that does not interpret any of the baggage contents. As a result it stores data in maps.
 * This is just the default implementation if baggagebuffers aren't used; it is not an efficient implementation because
 * branching entails duplicating the maps and lists storing data.
 * </p>
 */
public class GenericBaggageContents implements BaggageContents {

    public boolean dataDidOverflow = false;
    public boolean dataWasTrimmed = false;
    public boolean childDidOverflow = false;
    public boolean childWasTrimmed = false;
    public List<ByteBuffer> data = null;
    private Map<BagKey, GenericBaggageContents> children = null;

    public GenericBaggageContents branch() {
        GenericBaggageContents copy = new GenericBaggageContents();
        copy.dataDidOverflow = dataDidOverflow;
        copy.dataWasTrimmed = dataWasTrimmed;
        copy.childDidOverflow = childDidOverflow;
        copy.childWasTrimmed = childWasTrimmed;
        if (data != null) {
            copy.data = new ArrayList<>(data);
        }
        if (children != null) {
            copy.children = Maps.newTreeMap();
            for (BagKey key : children.keySet()) {
                copy.children.put(key, children.get(key).branch());
            }
        }
        return copy;
    }

    public GenericBaggageContents mergeWith(GenericBaggageContents other) {
        this.dataDidOverflow |= other.dataDidOverflow;
        this.dataWasTrimmed |= other.dataWasTrimmed;
        this.childDidOverflow |= other.childDidOverflow;
        this.childWasTrimmed |= other.childWasTrimmed;
        this.data = Lexicographic.merge(this.data, other.data);
        if (this.children == null) {
            this.children = other.children;
        } else if (other.children != null) {
            for (BagKey key : other.children.keySet()) {
                if (children.containsKey(key)) {
                    children.get(key).mergeWith(other.children.get(key));
                } else {
                    children.put(key, other.children.get(key));
                }
            }
        }
        return this;
    }

    public void addData(ByteBuffer dataItem) {
        if (dataItem != null) {
            if (data == null) {
                data = new ArrayList<>();
            }
            data.add(dataItem);
        }
    }

    public void addChild(BagKey key, GenericBaggageContents child) {
        if (child == null) {
            removeChild(key);
        } else {
            if (children == null) {
                children = new TreeMap<>();
            }
            children.put(key, child);
            childWasTrimmed |= child.dataWasTrimmed || child.childWasTrimmed;
        }
    }

    public GenericBaggageContents getChild(BagKey key) {
        return children == null ? null : children.get(key);
    }

    public void removeChild(BagKey key) {
        if (children != null) {
            children.remove(key);
        }
    }

    public void serializeTo(BaggageWriter writer) {
        writer.didTrimHere(dataWasTrimmed);

        if (data != null) {
            for (ByteBuffer data : data) {
                writer.writeBytes(data);
            }
        }

        if (children != null) {
            List<BagKey> childKeys = Lists.newArrayList(children.keySet());
            for (BagKey key : children.keySet()) {
                writer.enter(key);
                children.get(key).serializeTo(writer);
                writer.exit();
            }
        }
    }

    public static GenericBaggageContents parseFrom(BaggageReader reader) {
        if (!reader.hasNext()) {
            return null;
        }

        GenericBaggageContents baggage = new GenericBaggageContents();

        // Data
        ByteBuffer nextData = null;
        while ((nextData = reader.nextData()) != null) {
            if (BaggageContents.TRIMMARKER_CONTENTS.equals(nextData)) {
                baggage.dataWasTrimmed = true;
            } else {
                baggage.addData(nextData);
            }
        }
        baggage.dataDidOverflow = reader.didOverflow();

        // Children
        BagKey nextChild = null;
        while ((nextChild = reader.enter()) != null) {
            baggage.addChild(nextChild, parseFrom(reader));
            reader.exit();
        }
        baggage.childDidOverflow = reader.didOverflow();

        return baggage;
    }

}
