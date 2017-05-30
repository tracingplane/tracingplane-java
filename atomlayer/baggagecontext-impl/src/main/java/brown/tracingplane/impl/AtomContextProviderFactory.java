package brown.tracingplane.impl;

import brown.tracingplane.BaggageContext;
import brown.tracingplane.BaggageProvider;
import brown.tracingplane.BaggageProviderFactory;

/**
 * {@link BaggageProviderFactory} for {@link AtomContextProvider}
 */
public class AtomContextProviderFactory implements BaggageProviderFactory {

    @Override
    public BaggageProvider<? extends BaggageContext> provider() {
        return new AtomContextProvider();
    }

}
