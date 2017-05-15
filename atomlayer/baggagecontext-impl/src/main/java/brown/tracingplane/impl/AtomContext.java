package brown.tracingplane.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import brown.tracingplane.BaggageContext;
import brown.tracingplane.lexicographic.Lexicographic;
import brown.tracingplane.lexicographic.ProtobufVarint;

/**
 * <p>
 * An implementation of {@link BaggageContext} based on atoms and lexicographic merge. {@link AtomContext} represents
 * the minimal logic necessary to propagate {@link BaggageContext}s and participate in the tracing plane.
 * </p>
 */
public class AtomContext implements BaggageContext {

    /**
     * Simple implementation of ref counting
     */
    static class RefCount<T> {
        T object;
        volatile int count = 0;
        @SuppressWarnings("rawtypes")
        static final AtomicIntegerFieldUpdater<RefCount> reffer =
                AtomicIntegerFieldUpdater.newUpdater(RefCount.class, "count");

        public RefCount(T object) {
            this.object = object;
        }

        public void ref() {
            reffer.incrementAndGet(this);
        }

        void deref() {
            if (reffer.decrementAndGet(this) == 0) {
                object = null;
            }
        }

        boolean exclusive() {
            return count == 1;
        }
    }

    RefCount<List<ByteBuffer>> atoms;

    AtomContext() {}

    AtomContext(List<ByteBuffer> atoms) {
        this(new RefCount<>(atoms));
    }

    AtomContext(RefCount<List<ByteBuffer>> atoms) {
        this.atoms = atoms;
        this.atoms.ref();
    }

    void discard() {
        atoms.deref();
        atoms = null;
    }

    public AtomContext branch() {
        return new AtomContext(atoms);
    }

    public AtomContext merge(AtomContext other) {
        if (other == null || other.atoms == null || other.atoms.object == null) {
            return this;
        }

        if (atoms == null || atoms.object == null) {
            return this;
        }

        if (atoms.exclusive()) {
            atoms.object = Lexicographic.merge(atoms.object, other.atoms.object);
            other.discard();
            return this;
        }

        if (other.atoms.exclusive()) {
            return other.merge(this);
        }

        List<ByteBuffer> merged = Lexicographic.merge(atoms.object, other.atoms.object);
        discard();
        other.discard();
        atoms = new RefCount<>(merged);
        atoms.ref();
        return this;
    }

    /**
     * If others hold references to our atoms, duplicates the atoms.
     */
    void toExclusive() {
        if (atoms != null && atoms.object != null && !atoms.exclusive()) {
            RefCount<List<ByteBuffer>> newAtoms = new RefCount<>(new ArrayList<>(atoms.object));
            atoms.deref();
            newAtoms.ref();
            atoms = newAtoms;
        }
    }

    public int serializedSize() {
        int size = 0;
        for (ByteBuffer atom : atoms.object) {
            size += atom.remaining() + ProtobufVarint.sizeOf(atom.remaining());
        }
        return size;
    }

    /**
     * <p>
     * Returns the {@link List} that backs this {@link AtomContext} object. This method is only for observing the atoms,
     * not for updating them.
     * </p>
     * 
     * @return the atoms of this {@link AtomContext}
     */
    public List<ByteBuffer> getUnmodifiableAtoms() {
        return atoms == null ? null : atoms.object == null ? null : Collections.unmodifiableList(atoms.object);
    }

    /**
     * <p>
     * Returns the {@link List} that backs this {@link AtomContext} object, so modifications to the list will be
     * reflected by this context and vice versa. {@link AtomContext} uses reference counting as an optimization, so a
     * call to this method might result in duplicating atoms if others currently hold a reference to them.
     * </p>
     * 
     * @return the atoms of this {@link AtomContext}
     */
    public List<ByteBuffer> getModifiableAtoms() {
        toExclusive();
        return atoms();
    }

    List<ByteBuffer> atoms() {
        return atoms == null ? null : atoms.object;
    }

}
