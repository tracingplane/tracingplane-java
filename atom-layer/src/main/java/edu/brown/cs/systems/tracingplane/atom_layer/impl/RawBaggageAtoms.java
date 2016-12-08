package edu.brown.cs.systems.tracingplane.atom_layer.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import edu.brown.cs.systems.tracingplane.atom_layer.BaggageAtoms;
import edu.brown.cs.systems.tracingplane.atom_layer.types.Lexicographic;
import edu.brown.cs.systems.tracingplane.atom_layer.types.ProtobufVarint;

public class RawBaggageAtoms implements BaggageAtoms {

    static final AtomicIntegerFieldUpdater<SimpleBaggageContents> reffer =
            AtomicIntegerFieldUpdater.newUpdater(SimpleBaggageContents.class, "refcount");

    static class SimpleBaggageContents {
        volatile int refcount = 0;
        List<ByteBuffer> atoms;

        public SimpleBaggageContents() {
            this(new ArrayList<>());
        }

        public SimpleBaggageContents(List<ByteBuffer> atoms) {
            this.atoms = atoms;
        }

        void ref() {
            reffer.incrementAndGet(this);
        }

        void deref() {
            if (reffer.decrementAndGet(this) == 0) {
                atoms = null;
            }
        }

        boolean editableInPlace() {
            return refcount == 1;
        }
    }

    static final byte[] EMPTY_BYTES = new byte[0];

    SimpleBaggageContents contents;

    RawBaggageAtoms() {
        this(new SimpleBaggageContents());
    }

    RawBaggageAtoms(List<ByteBuffer> atoms) {
        this(new SimpleBaggageContents(atoms));
    }

    RawBaggageAtoms(SimpleBaggageContents contents) {
        this.contents = contents;
        this.contents.ref();
    }

    void discard() {
        this.contents.deref();
        this.contents = null;
    }

    int serializedSize() {
        int size = 0;
        for (ByteBuffer atom : contents.atoms) {
            size += atom.remaining() + ProtobufVarint.sizeOf(atom.remaining());
        }
        return size;
    }

    RawBaggageAtoms branch() {
        return new RawBaggageAtoms(contents);
    }

    RawBaggageAtoms mergeWith(RawBaggageAtoms other) {
        if (other == null) {
            return this;
        }
        if (contents.editableInPlace()) {
            contents.atoms = Lexicographic.merge(contents.atoms, other.contents.atoms);
            other.contents.deref();
            return this;
        } else if (other.contents.editableInPlace()) {
            return other.mergeWith(this);
        } else {
            List<ByteBuffer> merged = Lexicographic.merge(contents.atoms, other.contents.atoms);
            this.contents.deref();
            other.contents.deref();
            this.contents = new SimpleBaggageContents(merged);
            return this;
        }
    }

}
