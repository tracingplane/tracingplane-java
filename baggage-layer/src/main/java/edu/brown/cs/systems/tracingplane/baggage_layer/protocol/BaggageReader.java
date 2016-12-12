package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Queues;
import edu.brown.cs.systems.tracingplane.atom_layer.BaggageAtoms;
import edu.brown.cs.systems.tracingplane.atom_layer.types.Lexicographic;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.baggage_layer.BaggageLayerException;
import edu.brown.cs.systems.tracingplane.baggage_layer.BaggageLayerException.BaggageLayerRuntimeException;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixTypes.Level;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.AtomPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.HeaderPrefix;

/** TODO: documentation, tests, test exceptions and so forth */
public class BaggageReader {

    private static final Logger log = LoggerFactory.getLogger(BaggageReader.class);

    private final Iterator<ByteBuffer> it;
    private List<ByteBuffer> overflowAtoms = null;
    private final List<ByteBuffer> unprocessedAtoms = new ArrayList<>(Level.LEVELS);

    private boolean didReadData = false;
    private boolean encounteredOverflow = false;
    private int currentLevel = -1;
    private final Deque<ByteBuffer> currentPath = Queues.newArrayDeque();

    private AtomPrefix nextAtomPrefix;
    private ByteBuffer nextAtom;

    private BaggageReader(Iterator<ByteBuffer> it) {
        this.it = it;
        advanceNext();
    }

    public static BaggageReader create(Iterable<ByteBuffer> itbl) {
        if (itbl == null) {
            return create(Collections.emptyIterator());
        } else {
            return create(itbl.iterator());
        }
    }

    public static BaggageReader create(Iterator<ByteBuffer> it) {
        if (it == null) {
            return new BaggageReader(Collections.emptyIterator());
        } else {
            return new BaggageReader(it);
        }
    }

    public static BaggageReader create(Iterator<ByteBuffer> first, Iterator<ByteBuffer> second,
                                       Iterator<ByteBuffer> more) {
        return new BaggageReader(Lexicographic.merge(first, second, more));
    }

    /**
     * Advances to the next atom, also processing overflow markers.
     * 
     * @return true if there are more atoms; false if we have exhausted the input iterator
     */
    private boolean advanceNext() {
        while (it.hasNext()) {
            nextAtom = it.next();
            if (BaggageAtoms.OVERFLOW_MARKER.equals(nextAtom)) {
                if (!encounteredOverflow) {
                    encounteredOverflow = true;
                    overflowAtoms = new ArrayList<>(currentPath.size() + 1);
                    overflowAtoms.addAll(currentPath);
                    overflowAtoms.add(BaggageAtoms.OVERFLOW_MARKER);
                }
            } else {
                nextAtomPrefix = AtomPrefixes.get(nextAtom.get());
                return true;
            }
        }
        nextAtom = null;
        nextAtomPrefix = null;
        return false;
    }

    private void rewindAtom() {
        nextAtom.position(nextAtom.position() - 1);
    }

    private void advanceToNextBag() {
        if (didReadData) {
            dropData();
        } else {
            keepData();
        }
    }

    /**
     * Assumes the current atom is a header and enters the child bag, incrementing the current level and adding the
     * header to the current path
     */
    private void enterNextBag() {
        // Save the header
        rewindAtom();
        currentLevel = nextAtomPrefix.level(currentLevel);
        currentPath.addLast(nextAtom);
        unprocessedAtoms.add(nextAtom);

        // Move to next atom
        advanceNext();
        didReadData = false;
    }

    private void exitCurrentBag() {
        // Pop down a level
        ByteBuffer previousHeaderAtom = currentPath.pollLast();
        if (previousHeaderAtom == null) {
            throw new BaggageLayerRuntimeException("Called exit without a corresponding call to enter");
        }

        // If the child processed all of its atoms then trim unprocessed
        int endIndex = unprocessedAtoms.size() - 1;
        if (unprocessedAtoms.get(endIndex) == previousHeaderAtom) {
            unprocessedAtoms.remove(endIndex);
        }

        // Set the level to the parent header's level
        ByteBuffer parentHeader = currentPath.peekLast();
        if (parentHeader == null) {
            currentLevel = -1;
        } else {
            currentLevel = Level.valueOf(parentHeader.get(parentHeader.position()));
        }
    }

    /**
     * Checks to see whether there are more atoms at the current level.
     * 
     * This is always true, unless the next atom is a prefix for a header at a level {@code <= currentLevel}, or we run
     * out of atoms.
     * 
     * When this method returns false, {@link #exit()} should be called to return to the parent bag.
     * 
     * @return true if there are more data elements or child bags to read; false if this bag is exhausted
     */
    public boolean hasNext() {
        return nextAtom != null && nextAtomPrefix.level(currentLevel) > currentLevel;
    }

    /**
     * @return true if the next atom is a data atom; false otherwise
     */
    public boolean hasData() {
        return nextAtom != null && nextAtomPrefix.isData();
    }

    /**
     * Check to see whether the current atom is the header for a child bag. This method can return false if the current
     * atom is a data atom, or if the current atom is a header for a bag belonging to a parent
     * 
     * @return true if the current atom is the header for a child bag; false otherwise
     */
    public boolean hasChild() {
        return nextAtom != null && nextAtomPrefix.isHeader() && nextAtomPrefix.level(currentLevel) > currentLevel;
    }

    /**
     * @return the next atom if it is a data atom; null otherwise
     */
    public ByteBuffer nextData() {
        if (hasData()) {
            ByteBuffer currentAtom = nextAtom;
            advanceNext();
            didReadData = true;
            return currentAtom;
        }
        return null;
    }

    /**
     * <p>
     * Drop remaining data atoms between here and the next child bag / end of bag. This means they will not be added to
     * the list of unprocessed elements. The method {@link #keepData()} lets you keep remaining data atoms as unprocessed
     * elements instead if desired.
     * </p>
     * 
     * <p>
     * The default behavior for data atoms is as follows:
     * </p>
     * <ul>
     * <li>If you do not attempt to read any data from a bag (eg, by jumping straight to a child bag), then all data is
     * automatically added to the list of unprocessed elements.</li>
     * <li>If you read some data from a bag, but not all of it, then the remaining data in the bag is discarded and not
     * added as unprocessed</li>
     * </ul>
     * 
     * The methods {@link #dropData()} and {@link #keepData()} let the user control this behavior
     */
    public void dropData() {
        while (hasData()) {
            advanceNext();
        }
    }

    /**
     * <p>
     * Drop all remaining data atoms and child atoms in this bag, advancing to the next sibling / parent bag. None of
     * the atoms will be added to the list of unprocessed elements.
     * </p>
     * <p>
     * In general this method should not be used, as it is likely to violate the 'ignore and propagate' principle.
     * </p>
     */
    public void dropDataAndChildren() {
        dropData();
        while (hasChild()) {
            enterNextBag();
            dropDataAndChildren();
            exitCurrentBag();
        }
    }

    /**
     * <p>
     * Add all remaining data atoms between here and the next child bag / end of bag to the list of unprocessed
     * elements. The method {@link #dropData()} lets you instead drop the data items if desired.
     * </p>
     * 
     * <p>
     * The default behavior for data atoms is as follows:
     * </p>
     * <ul>
     * <li>If you do not attempt to read any data from a bag (eg, by jumping straight to a child bag), then all data is
     * automatically added to the list of unprocessed elements.</li>
     * <li>If you read some data from a bag, but not all of it, then the remaining data in the bag is discarded and not
     * added as unprocessed</li>
     * </ul>
     * 
     * <p>
     * The methods {@link #dropData()} and {@link #keepData()} let the user control this behavior
     * </p>
     */
    public void keepData() {
        while (hasData()) {
            rewindAtom();
            unprocessedAtoms.add(nextAtom);
            advanceNext();
        }
    }

    /**
     * <p>
     * Add all remaining data atoms and children of this bag to the list of unprocessed elements. The method (@link
     * dropBag()} lets you instead drop the bag if desired.
     * </p>
     * 
     * The default behavior is to keep all child bags that are not processed, so a call to {@link #keepDataAndChildren()}
     * is not necessary
     */
    public void keepDataAndChildren() {
        keepData();
        while (hasChild()) {
            enterNextBag();
            keepDataAndChildren();
            exitCurrentBag();
        }
    }

    /**
     * Advances through the atoms until either the specified bag is encountered, or we encounter a bag that is
     * lexicographically greater than the specified bag.
     * 
     * That is, if we encounter the bag, we enter the bag and advance to the next atom and return true.
     * 
     * If this method returns false, we are at the first bag after where the expected bag would be
     * 
     * @param expect the next BagKey to look for
     * @return true if the BagKey is found and the bag was entered; false otherwise
     */
    public boolean enter(BagKey expect) {
        advanceToNextBag();

        // Get the prefix we are looking for
        AtomPrefix expectedPrefix = expect.atomPrefix(currentLevel + 1, expect.options);

        while (hasChild()) {
            // Compare the prefix then the payload
            int comparison = nextAtomPrefix.compareTo(expectedPrefix);
            if (comparison == 0) {
                comparison = Lexicographic.compare(expect.atomPayload(), nextAtom);
            }

            if (comparison == 0) {
                // Found the bag!
                enterNextBag();
                return true;
            } else if (comparison < 0) {
                // Didn't find the bag, reached a bag past it
                return false;
            } else {
                // Skip the bag then continue to check the next one
                enterNextBag();
                keepDataAndChildren();
                exitCurrentBag();
                continue;
            }
        }

        // Exhausted the current bag without finding the child
        return false;
    }

    /**
     * If there are no more children in the current bag, returns null.
     * 
     * Otherwise, advances to the next child, enters it, and returns its key.
     * 
     * @return the key of the next child in this bag if there is one; null otherwise
     */
    public BagKey enter() {
        // If the data atoms of the current bag aren't exhausted, drop them
        advanceToNextBag();

        while (hasChild()) {
            try {
                BagKey key = HeaderSerialization.parse((HeaderPrefix) nextAtomPrefix, nextAtom);
                enterNextBag();
                return key;
            } catch (BaggageLayerException e) {
                log.error(String.format("Unable to parse bag key for header %s %s", nextAtomPrefix, nextAtom), e);
                enterNextBag();
                keepDataAndChildren();
                exitCurrentBag();
                continue;
            }
        }

        return null;
    }

    /**
     * Indicate the current child has finished parsing. This method will drop any remaining data and/or child atoms that
     * were not handled.
     * 
     * After this call finishes, the next atom is expected to be a bag header of either a sibling, or a sibling of some
     * parent, grandparent, or other ancester.
     */
    public void exit() {
        advanceToNextBag();
        keepDataAndChildren();
        exitCurrentBag();
    }

    /**
     * Indicate that atom parsing has completed.
     * 
     * Remaining data atoms will be discarded and remaining children will be treated as unprocessed
     */
    public void finish() {
        // Exit any bags that were left open
        while (currentLevel != -1) {
            exit();
        }

        // Finish child bags at the root level
        advanceToNextBag();
        keepDataAndChildren();
    }

    /**
     * @return true if an overflow marker has been encountered parsing up to this point
     */
    public boolean didOverflow() {
        return encounteredOverflow;
    }

    /**
     * If an overflow marker was encountered, we keep track of where the first occurrence of the overflow marker was
     * encountered.
     * 
     * This consists of a path of atom headers followed by the overflow marker.
     * 
     * These atoms can be re-merged with baggage atoms to re-add the overflow marker in the correct position.
     * 
     * This method should only be called at the end of parsing. Upon calling this method, all remaining atoms will be
     * traversed in an attempt to find trailing overflow markers. All remaining atoms are treated as unprocessed.
     * 
     * @return a list of atoms if overflow was encountered, otherwise null
     */
    public List<ByteBuffer> overflowAtoms() {
        finish();
        return overflowAtoms;
    }

    /**
     * If some bags went unprocessed, their atoms are saved.
     * 
     * These atoms comprise the unprocessed data elements plus the appropriate headers.
     * 
     * This method should only be called at the end of parsing. Upon calling this method, all remaining atoms will be
     * traversed in an attempt to find trailing overflow markers. All remaining atoms are treated as unprocessed.
     * 
     * @return a list of atoms if there were unprocessed atoms, otherwise null
     */
    public List<ByteBuffer> unprocessedAtoms() {
        finish();
        return unprocessedAtoms.isEmpty() ? null : unprocessedAtoms;
    }

}
