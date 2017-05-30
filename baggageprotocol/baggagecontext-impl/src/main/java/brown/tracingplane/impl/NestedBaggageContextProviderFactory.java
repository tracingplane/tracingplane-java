package brown.tracingplane.impl;

import brown.tracingplane.BaggageContext;
import brown.tracingplane.BaggageProvider;
import brown.tracingplane.BaggageProviderFactory;

/**
 * {@link BaggageProviderFactory} for {@link NestedBaggageContextProvider}
 */
public class NestedBaggageContextProviderFactory implements BaggageProviderFactory {

    @Override
    public BaggageProvider<? extends BaggageContext> provider() {
        return new NestedBaggageContextProvider();
    }

}
