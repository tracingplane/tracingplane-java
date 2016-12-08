package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

import java.nio.ByteBuffer;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagOptions;

public class BagOptionsSerialization {

    public static int serializedSize(BagOptions options) {
        return 0;
    }

    public static BagOptions parse(ByteBuffer buf) {
        return new BagOptions();
    }

    public static void write(BagOptions options, ByteBuffer buf) {
        // TODO: support options
    }

    /** Test whether the specified options equal the serialized buf */
    public static boolean equals(BagOptions options, ByteBuffer buf) {
        if (buf == null || buf.remaining() == 0) {
            return BagOptions.DEFAULT_OPTIONS.equals(options);
        } else {
            // TODO: check directly
            return parse(buf).equals(options);
        }
    }

    public static int compare(BagOptions options, ByteBuffer buf) {
        BagOptions bufOptions = buf == null ? BagOptions.DEFAULT_OPTIONS : parse(buf);
        return options.compareTo(bufOptions);
    }

}
