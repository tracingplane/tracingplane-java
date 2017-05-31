# <img src="doc/figures/baggage.png" width="40"/> brown.tracingplane #

## 1 Quick Start ##

Documentation and tutorials for this project are located in the [Tracing Plane Wiki](https://github.com/tracingplane/tracingplane-java/wiki); see also the [Tracing Plane Javadocs](https://tracingplane.github.io/tracingplane-java/doc/)

## 2 Concepts ##

The Tracing Plane introduces two key abstractions: ***BaggageContexts*** and ***Execution-Flow Scoped Variables***.

### 2.1 BaggageContext ##

A `BaggageContext` is a general-purpose request context, intended to be used **within** and **across** distributed services.

For example, a request in a *microservices* environment might involve multiple services, which make calls across the network to each other.

For each request, a `BaggageContext` carries request metadata (things like request IDs, tags, etc.).  Its goal is to be passed alongside the request while it executes.

It's very useful to pass around `BaggageContext` objects at runtime.  They are used by a range of different debugging and monitoring tools, the classic example being distributed tracing (like [Zipkin](https://github.com/openzipkin/zipkin) and [OpenTracing](http://opentracing.io/) and [Dapper](https://research.google.com/pubs/archive/36356.pdf)).  However, there are also other cool examples, like [resource management](http://cs.brown.edu/people/jcmace/papers/mace15retro.pdf) and [dynamic monitoring](http://cs.brown.edu/people/jcmace/papers/mace15pivot.pdf).  We use the name ***tracing tools*** to refer to such tools.

### 2.2 Execution-Flow Scoped Variables

An execution-flow scoped variable is similar, in concept, to a thread-local variable.  However, instead of being dynamically scoped to threads, EFS variables are scoped to ***end-to-end requests***.

EFS variables *follow requests inline* as they execute.  The `TraceID` used by distributed tracing tools is an example of an execution-flow scoped variable.

Updates to an EFS variable occur *locally* to an EFS instance -- for example, if my request is doing several things concurrently, they might have different values for their respective EFS variables.  This relates to the notion of *causality* -- EFS variables follow execution's causality.  The `SpanID` used by distributed tracing tools is an EFS variable that demonstrates this -- several concurrent execution branches could be executing simultaneously, each with a different value for `SpanID`.

The Tracing Plane exposes EFS variables with an interface definition language, called BDL (Baggage Definition Language), and corresponding compiler.  BDL is similar to protocol buffers, and generates accessors that interface with `BaggageContext` instances and encapsulate all of the concurrency and propagation nuances that are easy to get wrong.

## 3 Project Information

### 3.1 Project Goals

It's actually *very hard* to get context propagation right and *very hard* to deploy new tracing tools in today's distributed systems:

* It's hard to instrument systems to pass around contexts, because it involves touching lots of little bits of code in many places.  **Instrumentation is hard -- only do it once**.
* It's hard to agree on context formats across system components, especially if they use different languages or frameworks **Agree on a general-purpose format -- bind specific tools to it later**
* It's hard to get the behavior of contexts right -- for example, if a request has a high degree of fan-in, how do you reconcile mismatched IDs or context values?  **Encapsulate well-defined propagation behavior for data types**

In general, we want a `BaggageContext` that can carry **any** tracing tool's data in a consistent way.

The *Tracing Plane* is a layered design for context propagation in distributed systems.  It involves a data serialization format, a protocol for interpreting data, an interface definition language (called BDL -- Baggage Definition Language), and compiler.

The tracing plane enables interoperability between systems and tracing applications.  It provides a "narrow waist" for tracing, analogous to the role of TCP/IP in networking.

### 3.2 Similar Projects

There are some similar projects, that we list here to make it more concrete what the Tracing Plane is:

* Go's [context package](https://golang.org/pkg/context/) provides request-scoped contexts that you pass around in Go programs.  Similarly, we propose `BaggageContext` objects be passed around in the same way (though our API is slightly different)
* Span contexts in [Zipkin](https://github.com/openzipkin/zipkin), [OpenTracing](http://opentracing.io/) and [Dapper](https://research.google.com/pubs/archive/36356.pdf) -- these pass around metadata (span and trace IDs) for distributed tracing.  The goal of `BaggageContext` is to provide a well-defined, concrete data format that these tracing tools would be able to use, to store their IDs.
* Instrumentation in [OpenTracing](http://opentracing.io/) is similar to instrumentation for the Tracing Plane.  One difference is that `BaggageContext` instances are *truly opaque* at instrumentation time, and are passed across all execution boundaries (including, for example, in request responses); whereas OpenTracing spans are conceptually tightly bound to the task of distributed tracing.

### 3.3 Project Status

This is an active research project at Brown University by [Jonathan Mace](http://cs.brown.edu/people/jcmace/) and [Prof. Rodrigo Fonseca](http://cs.brown.edu/~rfonseca/).  This work is supported in part by NSF award 1452712, and from generous gifts from Facebook and Google.

The Tracing Plane is motivated by many years of collective experience in end-to-end tracing and numerous tracing-related research projects including [X-Trace](https://www.usenix.org/legacy/event/nsdi07/tech/full_papers/fonseca/fonseca.pdf), [Quanto](https://www.usenix.org/legacy/event/osdi08/tech/full_papers/fonseca/fonseca.pdf), [Retro](http://cs.brown.edu/people/jcmace/papers/mace15retro.pdf), [Pivot Tracing](http://cs.brown.edu/people/jcmace/papers/mace15pivot.pdf).  You can also check out our research group's [GitHub](http://brownsys.github.io/tracing-framework/).

We currently provide a Java implementation of the Tracing Plane.  However, the Tracing Plane is not tightly coupled to Java, and is designed with interoperability in mind - between languages, systems, platforms, etc.

Keep an eye out for our research paper about Baggage, coming soon.

## 4 Other Useful Links ###

[Project Wiki](https://github.com/tracingplane/tracingplane-java/wiki)

[Tracing Plane Javadoc](https://tracingplane.github.io/tracingplane-java/doc/)

Example Zipkin / OpenTracing Tracers that are backed by BDL: [github.com/JonathanMace/tracingplane-opentracing](https://github.com/JonathanMace/tracingplane-opentracing)
