# <img src="doc/figures/baggage.png" width="40"/> The Tracing Plane and Baggage

The Tracing Plane is a layered design for context propagation in distributed systems.  The tracing plane enables interoperability between systems and tracing applications.  It is designed to provide a simple "narrow waist" for tracing, much like how TCP/IP provides a narrow waist for the internet.

Baggage is our name for **general purpose request context** in distributed systems, and Baggage is implemented by the Tracing Plane.  Though many systems already have request contexts -- e.g., Go's [context package](https://golang.org/pkg/context/); Span contexts in [Zipkin](https://github.com/openzipkin/zipkin), [OpenTracing](http://opentracing.io/) and [Dapper](https://research.google.com/pubs/archive/36356.pdf); request tags in [Census](https://github.com/grpc/grpc/tree/master/src/core/ext/census); etc. -- none of them are *general purpose*.  What this means is that if I instrument my distributed system to pass around Zipkin span contents, then later wish to use Census,  I must **reinstrument everything** in order to pass around Census tags.  That *sucks*.

This repository contains our Java reference implementation for the Tracing Plane and Baggage.  This is an active research project at Brown University by [Jonathan Mace](http://cs.brown.edu/people/jcmace/) and [Prof. Rodrigo Fonseca](http://cs.brown.edu/~rfonseca/).  It is motivated by many years of collective experience in end-to-end tracing and numerous tracing-related research projects including [X-Trace](https://www.usenix.org/legacy/event/nsdi07/tech/full_papers/fonseca/fonseca.pdf), [Quanto](https://www.usenix.org/legacy/event/osdi08/tech/full_papers/fonseca/fonseca.pdf), [Retro](http://cs.brown.edu/people/jcmace/papers/mace15retro.pdf), [Pivot Tracing](http://cs.brown.edu/people/jcmace/papers/mace15pivot.pdf).  You can also check out our research group's [GitHub](http://brownsys.github.io/tracing-framework/).  Keep an eye out for our research paper on Baggage, which will appear later in 2017!

#### Table of Contents ####
* Overview of The Tracing Plane TODO
* I need more details TODO (FAQ for researchers, tracing application devs, system devs, and curious observers)
* Getting started - downloading, prerequisites, and building TODO
* Simple example - baggage buffers TODO
* Tutorial - instrument your system TODO
* Overview of APIs for each layer TODO
* Project Status TODO

## The Tracing Plane ##

The Tracing Plane has four layers, illustrated in green.  The text below describes each layer at a high level.  Later text describes the corresponding Java APIs for our Java implementations.

<img src="doc/figures/narrowwaist.png" alt="Narrow Waist" width="600"/>

### Transit Layer ###

The **Transit Layer** has just one purpose: abstract the task of system instrumentation so that it only has to be done once.  System instrumentation is the most laborious part of tracing.  You have to modify every system component to make sure request contexts are passed around -- for example, passed to new threads when they're created, included in continuations and thread pool queues, serialized to RPC headers, etc.

Many systems *already have this kind of instrumentation* -- they already pass around request IDs, span contexts, tags, or other metadata.  However, in every case we have ever seen, the metadata passed around is tightly bound to the system, making it difficult or impossible to easily extend it to add new fields or change its behavior.  

The Transit Layer is an **instrumentation abstraction** that makes no attempt to interpret the contents or meaning of the baggage being carried.  This lets you **reuse existing instrumentation** whenever you want to deploy a new tracing application.  Instrumentation reuse overcomes an *enormous* barrier to entry -- we cannot overstate how useful this is!

To the transit layer, baggage is only ever an opaque object or byte array.  As a result, the transit layer must **delegate** logic for the following two tasks:

  1. Dividing and combining contexts when executions branch and rejoin.  If baggage is just a cryptic array of bytes [ 0x08, 0xAF, ...], how are you supposed to take two different arrays and merge them into one?
  2. Enforcing capacity restrictions on baggage.  Again, if baggage is just a cryptic array of bytes, how can you ditch some of the bytes if the array is too big?

### Atom Layer ###

The **Atom Layer** provides the implementation of branch, join, serialize and trimming logic for the Transit Layer.  Bearing in mind our goal -- a **general purpose request context** -- the atom layer must support the logic of many different tracing applications. Those tracing applications can be quite varied -- for example, you might want to do a set-union of tags in census when two execution branches join, or take the greater of two values in pivot tracing.

We absolutely want to **avoid** having to inspect and interpret baggage contents on a per-application basis, for example, it is insufficient for the Atom Layer to just call up to each tracing application.  This would *kill* our ability to easily deploy new tracing applications, because every time we deploy a new tracing application we would have to update **every** system component with new rules for handling the contexts.  By analogy, it would be like expecting every router on the internet to be able to understand the contents of every packet being routed for every protocol.  

Instead, we want to be able to pass *any* contexts through our system and have them emerge in a coherent, consistent way.  This means no interpretation of the payload -- just a single representation and one set of rules for branching, joining, serialization, and trimming.

The **Atom Layer** provides this **generic** solution -- an underlying context representation that supports all use cases.  It represents baggage as an **array of atoms** where an atom is **an array of bytes**.  Atoms can have arbitrary length.  The atom layer provides the bare minimum specification to satisfy the requirements thus far:

* Serialization and Deserialization: length prefix the bytes of each atom
* Branch: each branch receives its a copy of the atoms with no modifications
* Join: merge the two arrays of atoms using **lexicographic comparison** (more below)
* Trim: drop atoms from the **end** of the array of atoms until size requirement is met; then append the **overflow marker** (more below)

#### Atom Layer: Lexicographic Merge ####

Lexicographic merge is the cornerstone of the atom layer.  Atom arrays are arbitrary -- they do not need to be sorted and there are no requirements on lengths.  It is important for the atom layer to maintain the atom ordering in arrays, however, especially when merging two arrays.  To merge two arrays, it traverses them performing atom-wise comparison.  To compare two atoms is done **lexicographically**: starting from the leftmost bit, the raw bits are compared until one is found to be less than the other.  The lesser atom is added to the output and its atom array is advanced.  If the two atoms are exactly the same then the atom is added to the output and *both* atom arrays advance.  If one atom is a prefix of the other atom, then the shorter atom is considered to be lesser.

Lexicographic comparison is like doing an alphabetic comparison, but on bytes (e.g., `h` is less than `hell` is less than `hello`, just as `0` is less than `0001` is less than `00010`).  (Note: in reality, atoms are constrained to multiples of 1 byte, so a 5-bit atom is actually not allowed; we just use it here for simple examples).  The following examples demonstrate lexicographic merge:

##### Example 1 #####

* A = [ `0010`, `1100`, `0101` ]
* B = [ `0`, `0001`, `1111` ]
* merge(A, B) = [ `0`, `0001`, `0010`, `1100`, `0101`, `1111` ]

##### Example 2 #####

* A = [ `0010`, `1100`, `0101`, `1111` ]
* B = [ `0`, `0001`, `110000`, `1111` ]
* merge(A, B) = [ `0`, `0001`, `0010`, `1100`, `0101`, `110000`, `1111` ]

##### Why Lexicographic Merge? #####

If A and B are both sorted, then lexicographic merge produces a sorted array as output.  Additionally, if an element exists in both A and B, then it won't be duplicated in the output.  This scheme, in essence, gives us the primitive of a set, with set union as the merge behavior.

* A = [ `0000`, `000111`, `1000`, `111` ]
* B = [ `000111`, `001`, `1100`, `111` ]
* merge(A, B) = [ `0000`, `000111`, `001`, `1000`, `1100`, `111` ]

Taking this one step further, we could implement a rudimentary map by saying that the first byte of every atom is the map key and the subsequent bytes are the map value.  This would actually be a multimap, since you would be able to map several values to the same key.

Going further still, remember that there is no requirement for the arrays to be sorted.  If small atoms follow large atoms, then we are guaranteed for them all to follow each other in the output.  For example:

* A = [ `1010`, `0000`, `000111`, `0111` ]
* B = [ `1011`, `000111`, `001`, `0111` ]
* merge(A, B) = [ `1010`, `0000`, `000111`, `0111`, `1011`, `000111`, `001`, `0111` ]

Notice in this example that even though some atoms exist in both A and B, such as `000111`, the output array contains `000111` twice.  Lexicographic merge only skips duplicates if they are directly compared.  In the example above, A's `000111` is never compared to B's `000111` so there is no opportunity for deduplication.  This is a **good** thing!  It enables the Baggage Layer later on to implement arbitrary tree-structured data.

#### Atom Layer: Overflow ####


### Baggage Layer ###


* The *Baggage Layer*: a protocol that specifies data formats for atoms that enables composition of contexts -- that is, multiple people can propagate different things concurrently. The protocol supports a variety of data types (primitives, sets, maps, counters, clocks, etc.).  The protocol is robust to *overflow* -- that is, if the serialized representation is too large, we can simply chop it to the length we desire.  Finally, the system (e.g., the transit layer) doesn't need to be able to interpret 

=== What problem are you trying to solve? ===

Modern distributed and cloud systems comprise many different interconnected components, such as storage, queueing, co-ordination, batch and real-time processing, and front-end services.  Common auxiliary tasks -- such as logging, monitoring, auditing, and performance management -- are now *much* more difficult than in a standalone component, because multiple different machines and processes might need to participate.

**Context propagation** is therefore a critical component of such systems.  This involves passing metadata between components, usually alongside a request as it executes.  For example, a front-end might assign each incoming request a unique *request ID*.  Then, the request ID is passed along with the request, through all of the components of the software stack, enabling us to tie together events from one component (e.g., when the request hits the database) to other components (e.g., which user initiated the request?)

=== Why is this problem hard? ===

1. Change is the norm -- components change all the time, hard to keep up, only a few involved
2. Lots of different tasks need e2e propagation and propagate different things
3. Executions aren't simple! Not linear, but *graphs*

=== What do we propose? ===

Ultimate goal: generic protocol for propagating context that:

1. enables multiple participants simultaneously and opaquely
2. dynamic and adaptable at runtime
3. handles graph structure without requiring knowledge of the data being carried
4. supports many different data types
