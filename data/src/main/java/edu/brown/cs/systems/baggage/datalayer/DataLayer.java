package edu.brown.cs.systems.baggage.datalayer;

public interface DataLayer<T> {

	/**
	 * Creates a new baggage instance based off the provided instance. The
	 * provided instance is still valid and might be modified by this operation.
	 * Might return the original baggage instance.
	 * 
	 * This operation is typically used when an execution is branching into
	 * multiple concurrent components; for example, a second thread is being
	 * started, or callbacks are being enqueued to a thread pool. Each branch of
	 * the execution should have its own baggage instance.
	 * 
	 * @param from a baggage instance
	 * @return a baggage instance
	 */
	public T branch(T from);

	/**
	 * Creates a new baggage instance based off two other instances.
	 * 
	 * This operation is typically used when two concurrent branches of an
	 * execution are joining; for example, if one thread calls join on another
	 * thread; or if concurrent parents of a task complete, enabling the child
	 * task to begin.
	 * 
	 * @param left a baggage instance
	 * @param right a baggage instance
	 * @return a baggage instance
	 */
	public T join(T left, T right);

}
