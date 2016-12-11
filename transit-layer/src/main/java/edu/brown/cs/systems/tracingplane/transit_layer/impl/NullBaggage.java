package edu.brown.cs.systems.tracingplane.transit_layer.impl;

import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;

/**
 * Used by {@link NullTransitLayer} if no transit layer is installed.
 */
public class NullBaggage implements Baggage {

    public static final NullBaggage INSTANCE = new NullBaggage();

    private NullBaggage() {}

}
