package edu.brown.cs.systems.tracingplane.atom_layer.protocol;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import edu.brown.cs.systems.tracingplane.atom_layer.BaggageAtoms;

/**
 * Contains static methods for trimming baggage atoms to size extents.
 */
public class AtomLayerOverflow {

    public static final ByteBuffer OVERFLOW_MARKER = ByteBuffer.allocate(0);

    private AtomLayerOverflow() {}

    static final class TrimExtent {
        int atomCount = 0;
        int serializedSize = 0;
        boolean overflow = false;
    }

    static TrimExtent determineTrimExtent(List<ByteBuffer> atoms, int limit) {
        TrimExtent extent = new TrimExtent();
        if (limit <= 0) {
            extent.atomCount = atoms.size();
            extent.serializedSize = AtomLayerSerialization.serializedSize(atoms);
        } else {
            int overflowMarkerSize = AtomLayerSerialization.serializedSize(BaggageAtoms.OVERFLOW_MARKER);
            for (int i = 0; i < atoms.size(); i++) {
                int nextSize = extent.serializedSize + AtomLayerSerialization.serializedSize(atoms.get(i));
                if (nextSize <= limit - overflowMarkerSize) {
                    extent.atomCount++;
                    extent.serializedSize = nextSize;
                } else if (i == atoms.size() - 1 && nextSize <= limit) {
                    extent.atomCount++;
                    extent.serializedSize = nextSize;
                } else {
                    extent.serializedSize += overflowMarkerSize;
                    extent.overflow = true;
                    break;
                }
            }
        }
        return extent;
    }

    /**
     * <p>
     * If the serialized size of <code>atoms</code> is {@code <=limit} then this method returns <code>atoms</code> with
     * no modifications. Otherwise, this method returns the maximum prefix of <code>atoms</code> such that its
     * serialized size will be {@code <=limit}. The returned prefix will also have {@link #OVERFLOW_MARKER} appended to
     * the end. {@link #OVERFLOW_MARKER} is taken into account when calculating serialized size, so the return value of
     * this method is guaranteed to be serialized to {@code <=limit} bytes.
     * </p>
     * 
     * @param atoms a list of atoms, possibly null
     * @param limit the maximum serialized size of <code>atoms</code>
     * @return <code>atoms</code> if the serialized size of <code>atoms</code> is {@code <= limit}, otherwise a prefix
     *         of <code>atoms</code> {@link #OVERFLOW_MARKER}. Returns null if atoms is null
     */
    public static List<ByteBuffer> trimToSize(List<ByteBuffer> atoms, int limit) {
        if (atoms == null) {
            return null;
        }
        TrimExtent extent = determineTrimExtent(atoms, limit);
        if (extent.overflow) {
            List<ByteBuffer> subList = new ArrayList<>(extent.atomCount + 1);
            subList.addAll(atoms.subList(0, extent.atomCount));
            subList.add(BaggageAtoms.OVERFLOW_MARKER);
            return subList;
        } else {
            return atoms;
        }
    }

}
