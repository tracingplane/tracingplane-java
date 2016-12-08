package edu.brown.cs.systems.tracingplane.baggage_layer;

import java.nio.ByteBuffer;
import edu.brown.cs.systems.tracingplane.atom_layer.BaggageAtoms;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.DataPrefix;

//TODO: description and method documentation
public interface BaggageContents extends BaggageAtoms {

    public static final ByteBuffer TRIMMARKER_ATOM = ByteBuffer.wrap(new byte[] { DataPrefix.prefix });
    public static final ByteBuffer TRIMMARKER_CONTENTS = (ByteBuffer) TRIMMARKER_ATOM.duplicate().position(1);

    public static final BaggageLayer<?> baggageLayer = BaggageLayerFactory.createDefaultBaggageLayer();

    // TODO: static methods here for accessing data in bags, plus methods for accessing thread local baggage contents

}
