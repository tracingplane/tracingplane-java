package edu.brown.cs.systems.tracingplane.atom_layer.impl;

import edu.brown.cs.systems.tracingplane.atom_layer.AtomLayer;
import edu.brown.cs.systems.tracingplane.atom_layer.AtomLayerFactory;

public class RawAtomLayerFactory implements AtomLayerFactory {

    @Override
    public AtomLayer<?> newAtomLayer() {
        return new RawAtomLayer();
    }

}
