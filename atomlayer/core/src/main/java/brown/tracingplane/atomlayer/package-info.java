/**
 * <p>
 * The Atom Layer provides an underlying representation for {@link BaggageContext} based on <i>atoms</i>. Other layers
 * of the tracing plane use atoms to construct data structures like sets and maps. Since the Atom Layer is an inner
 * layer of the Tracing Plane, it is not expected for users to have to deal with this layer directly.
 * </p>
 * 
 * <p>
 * However, the Atom Layer <i>does</i> provide a minimal {@link BaggageContext} implementation that you can use to
 * propagate contexts. This implementation lets you propagate contexts, but you cannot update them or access their
 * content.
 * </p>
 * 
 * <p>
 * The atom layer is based on the notion of atoms: an atom is an arbitrary array of zero or more bytes; and a
 * {@link BaggageContext} is an arbitrary array of atoms, by default empty. The interpretation of atoms is up to the
 * clients calling down to the atom layer, and an atom is indivisible by the atom layer. The ordering of atoms is also
 * up to clients; however, the atom layer's {@link BaggageProvider#join(BaggageContext, BaggageContext)} implementation
 * can affect the position of atoms.
 * </p>
 */
package brown.tracingplane.atomlayer;
