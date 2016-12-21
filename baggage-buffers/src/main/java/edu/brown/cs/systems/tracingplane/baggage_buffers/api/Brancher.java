package edu.brown.cs.systems.tracingplane.baggage_buffers.api;

public interface Brancher<T> {

    public T branch(T from);

}
