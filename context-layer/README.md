

==== Object Representation ====

Our implementation considers the following three use cases of baggage:
1. a system is tracingplane enabled but nothing is being propagated
2. a system is tracingplane enabled and some non-changing contents are being propagated (eg, activity or tenant IDs)
3. a system is tracingplane enabled and is actively interacting with the baggage

We prioritise the use cases in the order presented above.  First, we optimize for the case where nothing is being propagated.  In this case, we minimize overhead by creating no objects and just propagating 'null' as baggage values.  Thus, we treat null as synonymous with empty baggage.

Second, we optimize for the case where executions have some identifiers propagated in their baggage, but these identifiers do not change.  In this case, we minimize overhead by using the same references to baggage contents (ie, not creating multiple identical baggage instances).

Finally, for the third case where executions actively interact with baggage, we create objects where necessary but use reference counting to avoid unnecessary object creation.


==== Serialization ====

If baggage is null, is empty, or has no serializable contents, then the serialized version of that baggage will be null.  As with baggage objects, null serialized baggage is synonymous with empty baggage.