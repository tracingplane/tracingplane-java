# Tracing Plane - Core

Defines two core Tracing Plane interfaces - `BaggageContext` and `BaggageProvider`.

A `BaggageContext` supports several fundamental propagation operations, that are implemented by its `BaggageProvider`.  They are:

* `newInstance()` -- creates a new, empty `BaggageContext`.  Depending on the `BaggageProvider`, it may choose to represent empty `BaggageContext` instances using null
* `branch(BaggageContext)` -- creates a duplicate `BaggageContext`.  Typically this just creates a copy.  Changes made to the branched `BaggageContext` will not be visible in the original, and vice versa.
* `join(BaggageContext, BaggageContext)` -- merges the values of two `BaggageContext` instances.  If there is no conflicting data within the `BaggageContexts`, this resembles a union of their contents.  However, if they both contain, e.g., values mapped to the same key, and those values differ, then the `BaggageProvider` must implement some sort of conflict resolution.
* `serialize(BaggageContext)` -- serializes the `BaggageContext` to a binary representation.  For a string-based representation, either use a `BaggageProvider`-specified representation, or `base64` encode the binary representation
* `deserialize(BaggageContext)` -- corresponding deserialization method
* `trim` -- trim is a special operation, that is exposed as `serialize(BaggageContext, maximumSerializedLength)`.  `BaggageProvider` is expected to provide a serialization method that drops data if it exceeds a certain length threshold.

The above methods only pertain to propagating `BaggageContext`.  There are no methods for accessing `BaggageContext` data or values.  The `BaggageProvider` is responsible for providing accessor interfaces.

With respect to the Tracing Plane, we provide a concise, efficient implementation of all of these methods using a representation based on *atoms*. 