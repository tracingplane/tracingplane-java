package brown.tracingplane.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import brown.tracingplane.BaggageContext;
import brown.tracingplane.atomlayer.ByteBuffers;
import brown.tracingplane.atomlayer.Lexicographic;
import brown.tracingplane.baggageprotocol.BagKey;
import brown.tracingplane.baggageprotocol.BaggageReader;
import brown.tracingplane.baggageprotocol.BaggageWriter;

/**
 * <p>
 * An implementation of {@link BaggageContext} that interprets atoms using the baggage protocol, which specifies a data
 * layout for nested bags.
 * </p>
 */
public class NestedBaggageContext implements BaggageContext {

    public boolean didOverflow = false;
    public List<ByteBuffer> dataAtoms = new ArrayList<>();
    public Map<BagKey, NestedBaggageContext> children = new TreeMap<>();

    static NestedBaggageContext parse(BaggageReader reader) {
        if (!reader.hasNext()) {
            return null;
        }
        NestedBaggageContext bag = new NestedBaggageContext();
        while (reader.hasData()) {
            bag.dataAtoms.add(reader.nextData());
        }
        if (reader.didOverflow()) {
            bag.didOverflow = true;
        }
        while (reader.hasChild()) {
            BagKey childKey = reader.enter();
            NestedBaggageContext childBag = parse(reader);
            if (childBag != null) {
                bag.children.put(childKey, childBag);
            }
            reader.exit();
        }
        return bag;
    }

    void serialize(BaggageWriter writer) {
        writer.didOverflowHere(didOverflow);
        for (ByteBuffer atom : dataAtoms) {
            ByteBuffer newAtom = writer.newDataAtom(atom.remaining());
            ByteBuffers.copyTo(atom, newAtom);
        }
        for (BagKey childKey : children.keySet()) {
            writer.enter(childKey);
            children.get(childKey).serialize(writer);
            writer.exit();
        }
    }

    public NestedBaggageContext branch() {
        NestedBaggageContext copy = new NestedBaggageContext();
        copy.didOverflow = didOverflow;
        copy.dataAtoms.addAll(dataAtoms);
        for (BagKey childKey : children.keySet()) {
            copy.children.put(childKey, children.get(childKey).branch());
        }
        return copy;
    }

    public void mergeWith(NestedBaggageContext right) {
        if (right == null) {
            return;
        }

        this.didOverflow |= right.didOverflow;

        if (this.dataAtoms == null) {
            this.dataAtoms = right.dataAtoms;
        } else if (right.dataAtoms != null) {
            this.dataAtoms = Lexicographic.merge(this.dataAtoms, right.dataAtoms);
        }

        if (this.children == null) {
            this.children = right.children;
        } else if (right.children != null) {
            for (BagKey childKey : right.children.keySet()) {
                NestedBaggageContext leftChild = this.children.get(childKey);
                NestedBaggageContext rightChild = right.children.get(childKey);
                if (leftChild != null) {
                    leftChild.mergeWith(rightChild);
                } else {
                    this.children.put(childKey, rightChild);
                }
            }
        }
    }

}
