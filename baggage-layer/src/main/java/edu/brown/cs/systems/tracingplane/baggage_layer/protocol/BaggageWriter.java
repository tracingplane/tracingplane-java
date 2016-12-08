package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import com.google.common.collect.Lists;
import edu.brown.cs.systems.tracingplane.atom_layer.BaggageAtoms;
import edu.brown.cs.systems.tracingplane.atom_layer.types.ByteBuffers;
import edu.brown.cs.systems.tracingplane.atom_layer.types.Lexicographic;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.baggage_layer.BaggageContents;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.DataPrefix;

/** Used for writing out baggage atoms that adhere to the baggage protocol TODO: comments and documentation */
public class BaggageWriter {

    private int currentLevel = -1;
    private boolean wroteOverflow = false;
    private final List<ByteBuffer> atoms = Lists.newArrayList();

    private final SharedBackingBuffer backing = new SharedBackingBuffer(1024);

    private ByteBuffer nextAtomToMerge = null;
    private final Iterator<ByteBuffer> atomsToMerge;

    private BaggageWriter(Iterator<ByteBuffer> mergeWith) {
        atomsToMerge = mergeWith;
        if (mergeWith != null && mergeWith.hasNext()) {
            nextAtomToMerge = atomsToMerge.next();
        }
    }

    public static BaggageWriter create() {
        return new BaggageWriter(null);
    }

    public static BaggageWriter createAndMergeWith(Iterable<ByteBuffer> atoms) {
        return createAndMergeWith(atoms == null ? null : atoms.iterator());
    }

    public static BaggageWriter createAndMergeWith(Iterator<ByteBuffer> atoms) {
        return new BaggageWriter(atoms);
    }

    public static BaggageWriter createAndMergeWith(Iterable<ByteBuffer> a0, Iterable<ByteBuffer> a1) {
        return createAndMergeWith(a0 == null ? null : a0.iterator(), a1 == null ? null : a1.iterator());
    }

    private static BaggageWriter createAndMergeWith(Iterator<ByteBuffer> a0, Iterator<ByteBuffer> a1) {
        return new BaggageWriter(Lexicographic.merge(a0, a1));
    }

    public void enter(BagKey field) {
        currentLevel++;
        if (field instanceof BagKey.Indexed) {
            writeHeader(currentLevel, (BagKey.Indexed) field);
        } else if (field instanceof BagKey.Keyed) {
            writeHeader(currentLevel, (BagKey.Keyed) field);
        }
    }

    public void exit() {
        currentLevel--;
    }

    private void writeHeader(int level, BagKey.Indexed bagKey) {
        ByteBuffer buf = backing.newAtom(HeaderSerialization.serializedSize(bagKey));
        HeaderSerialization.writeAtom(buf, bagKey, level);
    }

    private void writeHeader(int level, BagKey.Keyed bagKey) {
        ByteBuffer buf = backing.newAtom(HeaderSerialization.serializedSize(bagKey));
        HeaderSerialization.writeAtom(buf, bagKey, level);

    }

    public void didOverflowHere(boolean didOverflow) {
        if (didOverflow && !wroteOverflow) {
            addAtom(BaggageAtoms.OVERFLOW_MARKER);
        }
    }

    public void didTrimHere(boolean didTrim) {
        if (didTrim) {
            addAtom(BaggageContents.TRIMMARKER_ATOM);
        }
    }

    /**
     * Creates a byte buffer with {@code expectedSize} bytes of free space. The atom can be written to as normal. Not
     * all of the free space must be filled
     */
    public ByteBuffer newDataAtom(int expectedSize) {
        ByteBuffer buf = backing.newAtom(expectedSize + 1);
        buf.put(DataPrefix.prefix);
        return buf;
    }

    /** Write a data atom with the provided buf content */
    public void writeBytes(ByteBuffer buf) {
        // See if this is already prefixed
        if (buf.position() > 0 && buf.get(buf.position() - 1) == DataPrefix.prefix) {
            buf.position(buf.position() - 1);
            addAtom(buf);
        } else {
            addAtom(ByteBuffers.copyWithPrefix(DataPrefix.prefix, buf));
        }
    }

    /** Write a data atom with an integer value */
    public void writeInt(int value) {
        newDataAtom(Integer.BYTES).putInt(value);
    }

    /** Write a data atom with a long value */
    public void writeLong(long value) {
        newDataAtom(Long.BYTES).putLong(value);
    }

    /** Ensure that any buffers created with newDataAtom are finished. */
    public void flush() {
        backing.finish();
    }

    public List<ByteBuffer> atoms() {
        flush();
        return atoms;
    }

    void addAtom(ByteBuffer atom) {
        while (nextAtomToMerge != null) {
            int comparison = Lexicographic.compare(nextAtomToMerge, atom);
            if (comparison > 0) {
                doAddAtom(atom);
                return;
            } else if (comparison == 0) {
                doAddAtom(atom);
                nextAtomToMerge = atomsToMerge.hasNext() ? atomsToMerge.next() : null;
                return;
            } else {
                doAddAtom(nextAtomToMerge);
                nextAtomToMerge = atomsToMerge.hasNext() ? atomsToMerge.next() : null;
            }
        }
        doAddAtom(atom);
    }

    void doAddAtom(ByteBuffer atom) {
        if (wroteOverflow && BaggageAtoms.OVERFLOW_MARKER.equals(atom)) {
            return;
        }
        atoms.add(atom);
    }

    private class SharedBackingBuffer {
        final int backingBufferSize;

        ByteBuffer current;
        ByteBuffer backingBuffer;

        SharedBackingBuffer(int backingBufferSize) {
            this.backingBufferSize = backingBufferSize;
        }

        void ensureCapacity(int requiredCapacity) {
            if (backingBuffer == null || backingBuffer.remaining() < requiredCapacity) {
                backingBuffer = ByteBuffer.allocate(Math.max(backingBufferSize, requiredCapacity));
            }
        }

        ByteBuffer newAtom(int expectedSize) {
            finish();
            ensureCapacity(expectedSize);
            current = backingBuffer.duplicate();
            return current;
        }

        void finish() {
            if (current != null) {
                int currentStart = backingBuffer.position();
                int currentEnd = current.position();
                current.position(currentStart);
                current.limit(currentEnd);
                backingBuffer.position(currentEnd);
                addAtom(current);
                current = null;
            }
        }

    }

}
