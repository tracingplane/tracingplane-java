# Tracing Plane - Static API

The Tracing Plane provides a static API `brown.tracingplane.Baggage` for manipulating `BaggageContext` instances.  This static API is the main entry point for manipulating contexts directly.  Supported methods are:

* `newInstance()` -- creates a new, empty `BaggageContext`; possibly a `null` value
* `branch(BaggageContext)` -- creates a duplicate `BaggageContext`.  Typically this just creates a copy.  Changes made to the branched `BaggageContext` will not be visible in the original, and vice versa.
* `join(BaggageContext, BaggageContext)` -- merges the values of two `BaggageContext` instances.  If there is no conflicting data within the `BaggageContexts`, this resembles a union of their contents.  However, if they both contain, e.g., values mapped to the same key, and those values differ, then the `BaggageProvider` must implement some sort of conflict resolution.
* `serialize(BaggageContext)` -- serializes the `BaggageContext` to a binary representation.  For a string-based representation, either use a `BaggageProvider`-specified representation, or `base64` encode the binary representation
* `deserialize(BaggageContext)` -- corresponding deserialization method
* `trim` -- trim is a special operation, that is exposed as `serialize(BaggageContext, maximumSerializedLength)`.  `BaggageProvider` is expected to provide a serialization method that drops data if it exceeds a certain length threshold.

Example usage in code is, for example, to pass a `BaggageContext` to a new thread:

    BaggageContext currentContext;
    MyThread newThread = new MyThread(Baggage.branch(currentContext));
   
Or to serialize a context for inclusion in network calls:

	BaggageContext currentContext;
	out.write(Baggage.serialize(currentContext));

The Tracing Plane also provides an `ActiveBaggage` API that mirrors these method calls.

Use of the `Baggage` static API depends on a `BaggageProvider` being configured.  Typically, this is done automatically by the Tracing Plane distribution that you use.  If this is not the case, or if you wish to override the `BaggageProvider` implementation being used, then you can set the `baggage.provider` property to the `BaggageProviderFactory` of your choice, e.g.,

	-Dbaggage.provider=brown.tracingplane.impl.NoOpBaggageContextProviderFactory

or in the typesafe `application.conf`:

	baggage.provider = "brown.tracingplane.impl.NoOpBaggageContextProviderFactory"