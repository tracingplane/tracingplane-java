# Atom Layer #

As described in [Section 2.1.1](#211-transit-layer-for-system-developers), the Transit Layer abstracts the task of system instrumentation so that it only has to be done once.  To the transit layer, and to system developers using Transit Layer APIs, baggage is only ever an opaque object or byte array.  As a result, the transit layer **delegates** logic for the following two tasks:

  1. Dividing and combining contexts when executions branch and rejoin.  If baggage is just a cryptic array of bytes [ 0x08, 0xAF, ...], how are you supposed to take two different arrays and merge them into one?
  2. Enforcing capacity restrictions on baggage.  Again, if baggage is just a cryptic array of bytes, how can you ditch some of the bytes if the array is too big?

However, the Atom Layer must perform these tasks while still being **general purpose**, ie, without having application-specific logic. We want to avoid having to inspect or interpret baggage contents on a per-application basis, as this would inhibit our ability to easily deploy new tracing applications: every time we deploy a new tracing application we would have to update **every** system component with new rules for handling the contexts.  By analogy, it would be like expecting every router on the internet to be able to understand the contents of every packet being routed for every protocol.

Instead, the Atom Layer provides a data format primitive -- **atoms** -- and implements general-purpose logic for branch, join, serialize and trim.  These primitives are exposed to the layer above for implementing their request context.  The primitives are general enough to support all many different use cases.

The Atom Layer provides the following properties

* General purpose request context that can be used to implement different data types depending on what kind of context is required by a tracing application.
* Propagation logic that doesn't require knowledge of the what is being propagated.

The Atom layer is the "narrow waist" of the tracing plane.  System components do not need to implement anything more than the atom layer, and they can support the context required by any tracing application.  System components do not need more complicated protocols or special merging logic. 

## Atom Layer Overview ##

### Representation

The Atom Layer represents baggage as an **array of atoms** where an atom is just an **array of bytes**.  For example, the following baggage has three atoms:

* binary: [`10010100 10010001`, `01010101`, ` `, `11110101 01010101 01010101`]
* hex: [ `94 91`, `55`, ` `, `F5 55 55`]

There are no constraints on the size, contents, or ordering of atoms.  As illustrated above, he empty byte array is a valid atom.

### Serialize length-prefixed

Serialization of atoms just length-prefixes them, eg:

* `02 94 91 01 55 00 03 F5 55 55`

We use Protocol Buffers-style variable length integers to encode the length prefixes (most atoms are small, so we want to use as few bytes as possible for their length).

### Branch by copying

When branching, each branch simply receives its own copy of the atoms to work with.  Updates made to the atoms by one branch are not seen by the other branch.

### Join using Lexicographic Comparison

Things get interesting when two branches of execution join.  Each branch of the execution will have its own atoms, which may be different.  For exampe, if A and B are the two different branches that are joining, we might have:

* A = [`94 91`, `55`, ` `, `F5 55 55`]
* B = [`94`, `55`, `F5 55 55`, `FF FF`]

We merge atoms using **lexicographic comparison** of atoms.  This Lexicographic merge is the cornerstone of the atom layer.  To merge two arrays, we iterate over both arrays performing element-wise comparison on the atoms.  For example, to merge A and B, the first comparison will be to compare `94 91` with `33`.

Lexicographic comparison compares the **raw bits** of atoms, starting from the left-most bit, until one element is found to be less than the other.  The lesser atom is added to the output and its iterator is advanced.  If the atoms are exactly the same then the atom is added to the output **once**, then **both** iterators advance.  If one atom is a prefix of the other atom then is the shorter atom is considered be lesser.

Lexicographic comparison is like doing an alphabetic comparison, but on bytes (e.g., `h` is less than `hell` is less than `hello`, just as `0` is less than `0001` is less than `00010`).  

In our previous example, we would perform the following comparisons:

1. Compare `94 91` to `94` and find that `94` is lesser.  Add `94` to the output and advance B's iterator.
2. Compare `94 91` to `55` and find that `55` is lesser.  Add `55` to the output and advance B's iterator.
3. Compare `94 91` to `F5 55 55` and find that `94 91` is lesser.  Add `94 91` to the output and advance A's iterator.
4. Compare `55` to `F5 55 55` and find that `55` is lesser.  Add `55` to the output and advance A's iterator.
5. Compare ` ` to `F5 55 55` and find that ` ` is lesser.  Add ` ` to the output and advance A's iterator.
6. Compare `F5 55 55` to `F5 55 55` and find that they are the same.  Add `F5 55 55` to the output and advance both A and B's iterators.
7. A is now exhausted; iterate through remaining elements of B and add them to the output.

The merged output we get is thus [`94`, `55`, `94 91`, `55`, ` `, `F5 55 55`, `FF FF`]

There are several interesting points to notice in this example:

1. The empty atom ` ` is less than all other atoms
2. When we encountered the duplicate `F5 55 55`, it was only added to the output once
3. Even though `55` existed in both atom arrays, we did not get the opportunity to compare them, and thus the output atoms contain `55` twice.

The final point is a **good** things.  It is the property that enables the Baggage Layer to implement data structures like maps and trees.

#### Example

Suppose we decide to always keep our atoms in sorted order.  Modifying the previous example, we would have:

* A = [` `, `55`, `94 91`, `F5 55 55`]
* B = [`55`, `94`, `F5 55 55`, `FF FF`]

The lexicographic merge of A and B would give us [` `, `55`, `94`, `94 91`, `F5 55 55`, `FF FF`].  Notice that the output is also sorted -- lexicographic merge preserves sort order.

#### Example

Consider the context propagated by X-Trace: a "taskId" which is an integer, and "parentEventIds" which is a set of integers.

We could devise the following scheme for laying out atoms for X-Trace's context: use an atom for "taskId" prefixed with the byte `00` and use one atom per element of "parentEventIds", each prefixed with the byte `01`.

So, if our "taskId" is 112 and we have "parentEventIds" of 10, 77, and 150, then our atoms would be [`00 00 00 00 70`, `01 00 00 00 0A`, `01 00 00 00 4D`, `01 00 00 00 96`].

Suppose our execution is joining with another branch of the same execution which has "taskId" of 112 and "parentEventIds" of 50 and 100, ie, [`00 00 00 00 70`, `01 00 00 00 32`, `01 00 00 00 64`].

Merging these two contexts would give us [`00 00 00 00 70`, `01 00 00 00 0A`, `01 00 00 00 32`, `01 00 00 00 4D`, `01 00 00 00 64`, `01 00 00 00 96`].  Interpreting this merged context, we still have taskId of 112, and the set union of parentEventIds: 10, 50, 77, 100, 150.

#### Summary

The Baggage Layer specifies a protocol for prefixing atoms and laying them out in order to implement different kinds of data structures.  See the [Baggage Layer](baggage-layer.md) documentation for more information.


### Trim by Dropping from Tail ####

Since baggage is generic and dynamic, it is possible for baggage to continue accumulating data until it is very large in size.  For example, tracing application developers might get liberal with the tags they add to the baggage, causing baggage size to continuously grow until it is too large.  This is a problem for systems that have strict limits on the baggage size they are willing to propagate.

The atom layer implements trimming in a simple way -- to trim baggage to a specific size limit, atoms are dropped from the **end** of the baggage until eventually baggage is within the size limit.  Then the **Overflow Marker** is appended to the end of the baggage.  The Overflow Marker is a zero-length atom, used to indicate that Overflow occurred.  Overflow is the term we use for when Baggage must be trimmed because it is too large.

Since the Overflow Marker is a zero-length atom, it is less than every other atom (similar to how the empty string is less than all other strings).  This means that the overflow marker 'retains' its position when arrays of atoms are merged.  We can always infer based on the position of the overflow marker which atoms could have been trimmed and which definitely were not.

For example, suppose we are again keeping our atoms sorted (ie, our atoms are a set)

* A = [`55`, `94 91`, `F5 55 55`]
* B = [`55`, `94`, ` `, `F5 55 55`, `FF FF`]

In this example, B contains the overflow marker.  The overflow marker tells us that somewhere in the past, B had to be trimmed.   We are able to infer the following:

* The only element of B less than or equal to 94 is 55.
* The elements of B greater than 94 are `F55 55 55` and `FF FF`
* B *may* have contained other elements greater than 94 (that were trimmed).

When we merge A and B, we retain these invariants:

* merge(A, B) = [`55`, `94`, ` `, `94 91`, `F5 55 55`, `FF FF`]

#### Further Reading ####

The Atom Layer [Javadoc](https://jonathanmace.github.io/tracingplane/doc/javadoc/edu/brown/cs/systems/tracingplane/atom_layer/AtomLayer.html) has a decent level of commenting.