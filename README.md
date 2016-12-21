= Tracing Plane =

The Tracing Plane is a layered design for context propagation in distributed systems.  This repository contains documentation, specification, and a reference implementation for this design.

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
