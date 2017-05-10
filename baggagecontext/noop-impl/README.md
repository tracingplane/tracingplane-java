# No-Op BaggageContext Impl

Provides a no-op implementation of the `BaggageContext` and `BaggageProvider` interfaces.  Every propagation method just returns null.

Having a no-op baggage implementation is useful at instrumentation time, because you can instrument your systems and compile them without yet deciding on the actual `BaggageProvider` implementation you wish to use.