# TracingPlane:AtomLayer/BaggageContext

This package implements a `BaggageContext` based solely on atoms and lexicographic merge.  It provides no interpretation of the atoms, but does provide an implementation of the main `BaggageProvider` methods -- specifically serialization, merge/join, and trim.

This atom-only `BaggageContext` should be used in systems that wish to propagate baggage, but have no need to inspect or modify its contents (e.g., if baggage is just passing through).  For this use case, none of the more heavyweight libraries (e.g., the baggage protocol and BDL client library) are necessary.