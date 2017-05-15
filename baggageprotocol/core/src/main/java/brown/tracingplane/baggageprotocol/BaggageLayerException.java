package brown.tracingplane.baggageprotocol;

import brown.tracingplane.atomlayer.AtomLayerException;

public class BaggageLayerException extends Exception {

    private static final long serialVersionUID = -7124041854080833387L;

    public BaggageLayerException(String message) {
        super(message);
    }

    public BaggageLayerException(String message, AtomLayerException e) {
        super(message, e);
    }

    public static class BaggageLayerRuntimeException extends RuntimeException {

        private static final long serialVersionUID = 2072227973209173448L;

        public BaggageLayerRuntimeException(String message) {
            super(message);
        }

    }

}
