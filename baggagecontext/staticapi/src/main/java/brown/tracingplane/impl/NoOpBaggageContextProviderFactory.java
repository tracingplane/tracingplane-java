package brown.tracingplane.impl;

import brown.tracingplane.BaggageContext;
import brown.tracingplane.BaggageProvider;
import brown.tracingplane.BaggageProviderFactory;

/**
 * A {@link BaggageProvider} implementation that always just returns null.
 */
public class NoOpBaggageContextProviderFactory implements BaggageProviderFactory {

    @Override
    public BaggageProvider<? extends BaggageContext> provider() {
        return new NoOpBaggageContextProvider();
    }

}
