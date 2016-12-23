# Baggage Layer #

The Baggage Layer specifies and implements the **Baggage Protocol**.  The Baggage Protocol specifies the data format and layout for atoms such that:

* Different tracing applications can put atoms in the baggage and get them back without interference from other tracing applications
* Tracing applications can utilize a variety of data types including primitives, sets, maps, counters, clocks, and **any state-based conflict-free replicated datatype**.
* If overflow occurs, we know exactly which tracing applications were affect and which were not.  This means we can also implement *inexact* datatypes such as approximate counters.

There are several important components to the Baggage Protocol outlined here.

### Atom Prefixes ###

The first byte of all Baggage Protocol atoms is the atom's *prefix* (similar to an IP packet header).  The first bit of a prefix is the atom type: 0 is a data atom and 1 is a header atom.  The overflow atom is also supported.

* **Data Atoms** Data atoms do not currently use the remaining bits of the prefix.  The subsequent bytes of a data atom are just an arbitrary payload that is not interpreted by the baggage layer.
* **Header Atoms** Header atoms use the remaining bits of the prefix as described in the next section.  The subsequent bytes of a header atom are used as a *key*.

### Maps ###

The very first bit of any data atom is a 0, therefore data atoms are **always** lexicographically less than header atoms.  This gives us the basis for implementing maps.  To create a mapping for a key to a value, simply create a header atom containing the key, then a data atom containing the value.  For example, suppose we want to map the String key "a" to the byte value 77 -- ie, we want a map that looks like:

	{
	  "a": 77
	}

To do this we would take the binary representations of "a" (`01100001`) and 77 (`01001101`)
 and prefix them with a header atom (`10000000`) and a data atom (`00000000`) respectively:

* header("a") =  `10000000 01100001`
* data(77) = `00000000 01001101`

The baggage atoms for this mapping are therefore:

* A = [ header("a"), data(77) ] = [`10000000 01100001`,`00000000 01001101`]

Now suppose there exists some other baggage B that does not contain a mapping for a, but does contain a mapping of "b" to 30:

	{
	  "b": 30
    }


* header("b") = `10000000 01100010`
* data(30) = `00000000 00011110`
* B = [ header("b"), data(30) ] = [`10000000 01100010`,`00000000 00011110`]
	
The lexicographic merge of A and B's atoms are:

* merge(A, B) = [`10000000 01100001`, `00000000 01001101`, `10000000 01100010`, `00000000 00011110`]
* merge(A, B) = [ header("a"), data(77), header("b"), data(30) ]

That is, the merged atoms represent the mappings of A and B:

	{
	  "a": 77,
	  "b": 30
	}

Now suppose there exists some other baggage C that contains a *different* mapping for "a":

	{
	  "a": 20,
	}

* header("a") =  `10000000 01100001`
* data(20) = `00000000 00010100`
* C = [ header("a"), data(20) ] = [`10000000 01100001`, `00000000 00010100`]

The lexicographic merge of A and C's atoms are:

* merge(A, C) = [`10000000 01100001`, `00000000 00010100`, `00000000 01001101`]
* merge(A, C) = [ header("a"), data(20), data(77) ]

That is, they represent:

	{
	  "a": 30, 77
	}

In the above, both mappings for "a" are retained.

Finally, let's look at a slightly more complicated example, where D and E have some mappings in common and some different mappings:

	D = {
	  "a": 22
	  "b": 30, 50
	  "d": 12
	}
	
	E = {
	  "b": 12, 50
	  "c": 80
	  "d": 12
	}

* header("a") =  `10000000 01100001`
* header("b") = `10000000 01100010`
* header("c") = `10000000 01100011`
* header("d") = `10000000 01100100`
* data(12) = `00000000 00001100`
* data(22) = `00000000 00010110`
* data(30) = `00000000 00011110`
* data(50) = `00000000 00110010`
* data(80) = `00000000 01010000`

* D = [ header("a"), data(22), header("b"), data(30), data(50), header("d"), data(12) ]
* D = [`10000000 01100010`, `00000000 00010110`, `10000000 01100010`, `00000000 00011110`, `00000000 00110010`, `10000000 01100100`, `00000000 00001100`]

* E = [ header("b"), data(12), data(50), header("c"), data(80), header("d"), data(12) ]
* E = [`10000000 01100010`, `00000000 00001100`, `00000000 00110010`, `10000000 01100011`, `00000000 01010000`, `10000000 01100100`, `00000000 00001100`]

* merge(D, E) = [`10000000 01100010`, `00000000 00010110`, `10000000 01100010`, `00000000 00001100`, `00000000 00011110`, `00000000 00110010`, `10000000 01100011`, `00000000 01010000`, `10000000 01100100`, `00000000 00001100`]
* merge(D, E) = [ header("a"), data(22), header("b"), data(12), data(30), data(50), header("c"), data(80), header("d"), data(12) ]

		merge(D, E) = {
		  "a": 22
		  "b": 12, 30, 50
		  "c": 80
		  "d": 12
		}
		
There are several things to note in this example:

* If both baggages have the same mapping for a key (eg, both D and E map "b" to 50), the exists only once in the output baggage -- it is not duplicated.  This means that an execution with lots of branching and joining, but few baggage modifications, will not experience an explosion in baggage size from repeatedly duplicated values.
* The same atom can exist multiple times in the atom representation.  For example, both "b" and "d" have the value 12, so the data atom for 12 exists twice.  

### Bags ###



### Lexicographically Comparable Variable-Length Integers (LexVarInts) ###

### Trees ###

### Overflow ###

### Further ###

Max, min, sum, avg, first, last, etc.  transient fields in baggagebuffers.  Inline bags
