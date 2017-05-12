package brown.tracingplane.impl;

import java.nio.ByteBuffer;
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

    AtomContext branch() {
        return new AtomContext(atoms);
    }

    AtomContext merge(AtomContext other) {
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

    int serializedSize() {
        int size = 0;
        for (ByteBuffer atom : atoms.object) {
            size += atom.remaining() + ProtobufVarint.sizeOf(atom.remaining());
        }
        return size;
    }

    List<ByteBuffer> atoms() {
        return atoms == null ? null : atoms.object;
    }

}
