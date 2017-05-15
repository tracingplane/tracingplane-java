package brown.tracingplane.baggageprotocol;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import com.google.common.collect.Lists;
import brown.tracingplane.atomlayer.ByteBuffers;
import brown.tracingplane.atomlayer.Lexicographic;
import brown.tracingplane.baggageprotocol.AtomPrefixes.DataPrefix;

/**
 * Used for writing out baggage atoms that adhere to the baggage protocol
 * 
 * TODO: comments and documentation
 */
public class BaggageWriter implements ElementWriter {

    private int currentLevel = -1;
    private int currentBagBeginIndex = 0;
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

    public static BaggageWriter createAndMergeWith(Iterator<ByteBuffer> a0, Iterator<ByteBuffer> a1) {
        return new BaggageWriter(Lexicographic.merge(a0, a1));
    }

    public BaggageWriter enter(BagKey field) {
        currentLevel++;
        if (field instanceof BagKey.Indexed) {
            writeHeader(currentLevel, (BagKey.Indexed) field);
        } else if (field instanceof BagKey.Keyed) {
            writeHeader(currentLevel, (BagKey.Keyed) field);
        }
        currentBagBeginIndex = atoms.size();
        return this;
    }

    public BaggageWriter exit() {
        currentLevel--;
        flush();

        // If the bag has no data, drop the bag
        if (isHeader(atoms.get(atoms.size() - 1))) {
            atoms.remove(atoms.size() - 1);
        }

        currentBagBeginIndex = atoms.size();
        return this;
    }

    private boolean isHeader(ByteBuffer atom) {
        if (atom.remaining() > 0) {
            return AtomPrefixes.get(atom.get(atom.position())).isHeader();
        } else {
            return false;
        }
    }

    private void writeHeader(int level, BagKey.Indexed bagKey) {
        ByteBuffer buf = backing.newAtom(HeaderSerialization.serializedSize(bagKey));
        HeaderSerialization.writeAtom(buf, bagKey, level);
        backing.finish();
    }

    private void writeHeader(int level, BagKey.Keyed bagKey) {
        ByteBuffer buf = backing.newAtom(HeaderSerialization.serializedSize(bagKey));
        HeaderSerialization.writeAtom(buf, bagKey, level);
        backing.finish();

    }

    public BaggageWriter didOverflowHere(boolean didOverflow) {
        if (didOverflow && !wroteOverflow) {
            flush();
            addAtom(BaggageProtocol.OVERFLOW_MARKER);
        }
        return this;
    }

    public BaggageWriter didTrimHere(boolean didTrim) {
        if (didTrim) {
            flush();
            addAtom(BaggageProtocol.TRIM_MARKER);
        }
        return this;
    }

    /**
     * Creates a byte buffer with {@code expectedSize} bytes of free space. The atom can be written to as normal. Not
     * all of the free space must be filled
     * 
     * @param expectedSize the amount of space needed
     * @return a ByteBuffer with at least {@code expectedSize} bytes remaining
     */
    public ByteBuffer newDataAtom(int expectedSize) {
        ByteBuffer buf = backing.newAtom(expectedSize + 1);
        buf.put(DataPrefix.prefix);
        return buf;
    }

    /**
     * Write a data atom with the provided buf content
     * 
     * @param buf the payload of the data atom to write
     * @return this BaggageWriter instance
     */
    public BaggageWriter writeBytes(ByteBuffer buf) {
        flush();
        if (buf.position() > 0 && buf.get(buf.position() - 1) == DataPrefix.prefix) {
            buf.position(buf.position() - 1);
            addAtom(buf);
        } else {
            addAtom(ByteBuffers.copyWithPrefix(DataPrefix.prefix, buf));
        }
        return this;
    }

    /** Sort any data written between the start of the current bag and now */
    public void sortData() {
        flush();
        Collections.sort(atoms.subList(currentBagBeginIndex, atoms.size()));
    }

    /** Ensure that any buffers created with newDataAtom are finished. */
    public void flush() {
        backing.finish();
    }

    /**
     * Indicate that writing to this writer has finished, and if the writer was also merging with other atoms, they
     * should be finished
     */
    public void finish() {
        flush();
        while (nextAtomToMerge != null) {
            doAddAtom(nextAtomToMerge);
            nextAtomToMerge = atomsToMerge.hasNext() ? atomsToMerge.next() : null;
        }
    }

    /**
     * <p>
     * Get the atoms that were written to this BaggageWriter
     * </p>
     * 
     * <p>
     * This method should not be called while writing is in progress
     * </p>
     * 
     * @return the atoms that were written to this BaggageWriter.
     */
    public List<ByteBuffer> atoms() {
        finish();
        return atoms;
    }

    private void addAtom(ByteBuffer atom) {
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

    private void doAddAtom(ByteBuffer atom) {
        if (wroteOverflow && BaggageProtocol.OVERFLOW_MARKER.equals(atom)) {
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
            current = backingBuffer.slice();
            current.limit(expectedSize);
            backingBuffer.position(backingBuffer.position() + expectedSize);
            return current;
        }

        void finish() {
            if (current != null) {
                int unused = current.remaining();
                current.flip();
                backingBuffer.position(backingBuffer.position() - unused);
                addAtom(current);
                current = null;
            }
        }

    }

}
