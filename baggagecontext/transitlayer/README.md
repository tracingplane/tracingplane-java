# TracingPlane:BaggageContext/TransitLayer

This package provides the following:

* The core interfaces for the Tracing Plane's `TransitLayer` -- to enhance the existing `BaggageContext` and `Baggage` interfaces that exist in `BaggageContext/API` and `BaggageContext/StaticAPI`.
* The out-of-the-box `TransitLayer` implementation, which is a NoOp and does nothing -- method calls are essentially ignored
* A `TransitLayer` implementation based on thread-local variables.  This is the recommended context propagation library to use with the tracing plane.  It is the default `TransitLayer` implementation in the Tracing Plane distribution jars.

## Transit Layer

The Tracing Plane provides an out-of-the-box context propagation library called the "Transit Layer".  The Transit Layer uses thread-local storage to store `BaggageContext` instances for threads, and provides a static API in `brown.tracingplane.ActiveBaggage` that invokes transit layer methods.  `ActiveBaggage` provides methods as follows:

* `set(BaggageContext)` -- set the active `BaggageContext` for this thread
* `get()` -- get the active `BaggageContext` for this thread
* `take()` -- remove and return the active `BaggageContext` for this thread
* `takeBytes()` -- remove, return, and serialize the active `BaggageContext` for this thread
* `discard()` -- discard the active `BaggageContext` for this thread

In addition to methods for manipulating the active baggage, the Transit Layer and `ActiveBaggage` interface provides methods mirroring those in the `Baggage` and `BaggageProvider` interfaces, to implicitly manipulate the current context:

* `branch()` -- creates and returns a duplicate of the currently active `BaggageContext`.
* `join(BaggageContext)` -- merges the values of some `BaggageContext` object into the active context.

The Transit Layer is configurable, and alternative implementations can be used other than the out-of-the-box thread-local implementation.  To provide a different implementation, simply implement the `TransitLayer` and `TransitLayerFactory` interfaces, then configure the factory class using the `baggage.transit` property, e.g.:

	-Dbaggage.transit=brown.tracingplane.impl.ThreadLocalTransitLayerFactory

or, using typesafe config `application.conf`:

	baggage.transit = "brown.tracingplane.impl.ThreadLocalTransitLayerFactory"