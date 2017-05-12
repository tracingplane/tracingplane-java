# Tracing Plane - Static API

The static `BaggageContext` API provides the following:

* Enables registration of a `BaggageProvider` class using the `baggage.provider` property
* Provides static methods in the `brown.tracingplane.Baggage` class that proxy to the configured `BaggageProvider`.

By default, `baggage.provider` will not be set; if this is the case, a No-Op provider will be used that simply ignores all baggage.