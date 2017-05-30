/**
 * <p>
 * Defines an encoding scheme for nested data structures using atoms.
 * </p>
 * 
 * <p>
 * The Baggage Protocol reads and writes {@link BaggageContext}s with a pre-order depth-first traversal. Starting from
 * the root of the tree, we first have data for that node, followed by child nodes. Child nodes can be addressed by
 * either statically defined indices, or by arbitrary-length key.
 * </p>
 * 
 * <p>
 * The {@link BaggageReader} and {@link BaggageWriter} classes are used for reading and writing atoms for
 * {@link BaggageContext} instances.
 * </p>
 * 
 * <p>
 * Details of the baggage protocol can be found on the project GitHub repository.
 * </p>
 */
package brown.tracingplane.baggageprotocol;
