# Atom Layer #

As described in [Section 2.1.1](#211-transit-layer-for-system-developers), the Transit Layer abstracts the task of system instrumentation so that it only has to be done once.  To the transit layer, and to system developers using Transit Layer APIs, baggage is only ever an opaque object or byte array.  As a result, the transit layer **delegates** logic for the following two tasks:

  1. Dividing and combining contexts when executions branch and rejoin.  If baggage is just a cryptic array of bytes [ 0x08, 0xAF, ...], how are you supposed to take two different arrays and merge them into one?
  2. Enforcing capacity restrictions on baggage.  Again, if baggage is just a cryptic array of bytes, how can you ditch some of the bytes if the array is too big?

The **Atom Layer** provides a simple implementation of branch, join, serialize and trimming logic for the Transit Layer and is designed to support our principle goal: a **general purpose request context**.  That is, the atom layer must support the logic of many different tracing applications which can be quite varied -- for example, you might want to do a set-union of tags in census when two execution branches join, or take the greater of two values in pivot tracing.

We absolutely want to **avoid** having to inspect and interpret baggage contents on a per-application basis, for example, it is insufficient for the Atom Layer to just call up to each tracing application.  This would *kill* our ability to easily deploy new tracing applications, because every time we deploy a new tracing application we would have to update **every** system component with new rules for handling the contexts.  By analogy, it would be like expecting every router on the internet to be able to understand the contents of every packet being routed for every protocol.  

Instead, we want to be able to pass *any* contexts through our system and have them emerge in a coherent, consistent way.  This means no interpretation of the payload -- just a single representation and one set of rules for branching, joining, serialization, and trimming.

The **Atom Layer** provides this generic solution -- an underlying context representation that supports all use cases.  It represents baggage as an **array of atoms** where an atom is **an array of bytes**.  Atoms can have arbitrary length.  The Atom Layer implements operations as follows:

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

Since baggage is generic and dynamic, it is possible for baggage to continue accumulating data until it is very large in size.  For example, tracing application developers might get liberal with the tags they add to the baggage, causing baggage size to continuously grow until it is too large.  This is a problem for systems that have strict limits on the baggage size they are willing to propagate.

The atom layer implements trimming in a simple way -- to trim baggage to a specific size limit, atoms are dropped from the **end** of the baggage until eventually baggage is within the size limit.  Then the **Overflow Marker** is appended to the end of the baggage.  The Overflow Marker is a zero-length atom, used to indicate that Overflow occurred.  Overflow is the term we use for when Baggage must be trimmed because it is too large.

Since the Overflow Marker is a zero-length atom, it is less than every other atom (similar to how the empty string is less than all other strings).  This means that the overflow marker 'retains' its position when arrays of atoms are merged.  We can always infer based on the position of the overflow marker which atoms could have been trimmed and which definitely were not.

#### Further Reading ####

The Atom Layer [Javadoc](https://jonathanmace.github.io/tracingplane/doc/javadoc/edu/brown/cs/systems/tracingplane/atom_layer/AtomLayer.html) has a decent level of commenting.