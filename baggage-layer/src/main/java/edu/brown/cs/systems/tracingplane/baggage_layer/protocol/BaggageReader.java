package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

import java.nio.ByteBuffer;
import java.util.ArrayList;
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
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixTypes.Level;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.AtomPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.HeaderPrefix;

/** TODO: documentation, tests, test exceptions and so forth */
public class BaggageReader {

    private static final Logger log = LoggerFactory.getLogger(BaggageReader.class);

    private final Iterator<ByteBuffer> it;
    private List<ByteBuffer> overflowAtoms = null;
    private final List<ByteBuffer> unprocessedAtoms = new ArrayList<>(Level.LEVELS);

    private boolean encounteredOverflow = false;
    private int currentLevel = -1;
    private final Deque<ByteBuffer> currentPath = Queues.newArrayDeque();

    private AtomPrefix nextAtomPrefix;
    private ByteBuffer nextAtom;

    public BaggageReader(Iterable<ByteBuffer> itbl) {
        this(itbl.iterator());
    }

    public BaggageReader(Iterator<ByteBuffer> it) {
        this.it = it;
        advanceNext();
    }

    /** Advances to the next atom, also processing overflow markers.
     * 
     * @return true if there are more atoms; false if we have exhausted the input iterator */
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

    /** Assumes the current atom is a header and enters the child bag, incrementing the current level and adding the
     * header to the current path */
    private void enterNextBag() {
        // Save the header
        rewindAtom();
        currentLevel = nextAtomPrefix.level(currentLevel);
        currentPath.addLast(nextAtom);
        unprocessedAtoms.add(nextAtom);

        // Move to next atom
        advanceNext();
    }

    /** Skips and drops remaining data atoms in this bag.
     * 
     * Advances to either the a child bag, or the end of bag if there are no children. */
    private void skipData() {
        while (hasData()) {
            advanceNext();
        }
    }

    /** If the current atom is a child, iterates over atoms for the child. Atoms are treated as unprocessed and added to
     * the list of unprocessed atoms. */
    private void skipChild() {
        enterNextBag();
        while (hasData()) {
            rewindAtom();
            unprocessedAtoms.add(nextAtom);
            advanceNext();
        }
        while (hasChild()) {
            skipChild();
        }
        exitCurrentBag();
    }

    private void exitCurrentBag() {
        // Pop down a level
        ByteBuffer previousHeaderAtom = currentPath.pollLast();

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

    /** Checks to see whether there are more atoms at the current level.
     * 
     * This is always true, unless the next atom is a prefix for a header at a level <= currentLevel, or we run out of
     * atoms.
     * 
     * When this method returns false, {@link exitBag} should be called to return to the parent bag. */
    public boolean hasNext() {
        return nextAtom != null && nextAtomPrefix.level(currentLevel) > currentLevel;
    }

    /** @return true if the next atom is a data atom; false otherwise */
    public boolean hasData() {
        return nextAtom != null && nextAtomPrefix.isData();
    }

    /** Check to see whether the current atom is the header for a child bag. This method can return false if the current
     * atom is a data atom, or if the current atom is a header for a bag belonging to a parent
     * 
     * @return true if the current atom is the header for a child bag; false otherwise */
    public boolean hasChild() {
        return nextAtom != null && nextAtomPrefix.isHeader() && nextAtomPrefix.level(currentLevel) > currentLevel;
    }

    /** @return the next atom if it is a data atom; null otherwise */
    public ByteBuffer nextData() {
        if (hasData()) {
            ByteBuffer currentAtom = nextAtom;
            advanceNext();
            return currentAtom;
        }
        return null;
    }

    /** Advances through the atoms until either the specified bag is encountered, or we encounter a bag that is
     * lexicographically greater than the specified bag.
     * 
     * That is, if we encounter the bag, we enter the bag and advance to the next atom and return true.
     * 
     * If this method returns false, we are at the first bag after where the expected bag would be */
    public boolean enter(BagKey expect) {
        // If the data atoms of the current bag aren't exhausted, drop them
        skipData();

        // Get the prefix we are looking for
        AtomPrefix expectedPrefix = expect.atomPrefix(currentLevel + 1);

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
            } else if (comparison > 0) {
                // Didn't find the bag, reached a bag past it
                return false;
            } else {
                // Skip the bag then continue to check the next one
                skipChild();
                continue;
            }
        }

        // Exhausted the current bag without finding the child
        return false;
    }

    /** If there are no more children in the current bag, returns null.
     * 
     * Otherwise, advances to the next child, enters it, and returns its key. */
    public BagKey enter() {
        // If the data atoms of the current bag aren't exhausted, drop them
        skipData();

        while (hasChild()) {
            try {
                BagKey key = HeaderSerialization.parse((HeaderPrefix) nextAtomPrefix, nextAtom);
                enterNextBag();
                return key;
            } catch (BaggageLayerException e) {
                log.error(String.format("Unable to parse bag key for header %s %s", nextAtomPrefix, nextAtom), e);
                skipChild();
                continue;
            }
        }

        return null;
    }

    /** Indicate the current child has finished parsing. This method will drop any remaining data and/or child atoms
     * that were not handled.
     * 
     * After this call finishes, the next atom is expected to be a bag header of either a sibling, or a sibling of some
     * parent, grandparent, or other ancester. */
    public void exit() {
        // Skip any data atoms remaining in the bag
        skipData();

        // Treat remaining child bags as unprocessed
        while (hasChild()) {
            skipChild();
        }

        // Exit the bag
        exitCurrentBag();
    }

    /** Indicate that atom parsing has completed.
     * 
     * Remaining data atoms will be discarded and remaining children will be treated as unprocessed */
    public void finish() {
        // Exit any bags that were left open
        while (currentLevel != -1) {
            exit();
        }

        // Finish child bags at the root level
        while (hasChild()) {
            skipChild();
        }
    }

    /** @return true if an overflow marker has been encountered parsing up to this point */
    public boolean didOverflow() {
        return encounteredOverflow;
    }

    /** If an overflow marker was encountered, we keep track of where the first occurrence of the overflow marker was
     * encountered.
     * 
     * This consists of a path of atom headers followed by the overflow marker.
     * 
     * These atoms can be re-merged with baggage atoms to re-add the overflow marker in the correct position.
     * 
     * @return a list of atoms if overflow was encountered, otherwise null */
    public List<ByteBuffer> overflowAtoms() {
        return overflowAtoms;
    }

    /** If some bags went unprocessed, their atoms are saved.
     * 
     * These atoms comprise the unprocessed data elements plus the appropriate headers.
     * 
     * @return a list of atoms if there were unprocessed atoms, otherwise null */
    public List<ByteBuffer> unprocessedAtoms() {
        return unprocessedAtoms.isEmpty() ? null : unprocessedAtoms;
    }

}
