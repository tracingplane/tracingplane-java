package edu.brown.cs.systems.tracingplane.baggage_buffers.api;

public interface Joiner<T> {

    public T join(T first, T second);

}
