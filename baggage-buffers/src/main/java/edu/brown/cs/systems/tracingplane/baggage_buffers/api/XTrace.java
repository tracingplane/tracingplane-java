package edu.brown.cs.systems.tracingplane.baggage_buffers.api;

import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageReader;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageWriter;

public class XTrace implements Bag {

    @Override
    public BaggageHandler<XTrace> handler() {
        return new BaggageHandler<XTrace>() {

            @Override
            public XTrace parse(BaggageReader reader) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void serialize(BaggageWriter writer, XTrace instance) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public XTrace join(XTrace first, XTrace second) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public XTrace branch(XTrace from) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public boolean isInstance(Bag bag) {
                return bag instanceof XTrace;
            }
            
        };
    }

}
