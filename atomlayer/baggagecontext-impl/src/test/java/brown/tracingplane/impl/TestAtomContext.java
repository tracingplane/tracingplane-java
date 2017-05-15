package brown.tracingplane.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import brown.tracingplane.BaggageContext;
import brown.tracingplane.BaggageProvider;
import brown.tracingplane.impl.AtomContext.RefCount;

public class TestAtomContext {

    static AtomContextProvider provider = new AtomContextProvider();

    @Test
    public void testFactory() {
        AtomContextProviderFactory factory = new AtomContextProviderFactory();
        BaggageProvider<? extends BaggageContext> provider = factory.provider();
        assertNotNull(provider);
        assertTrue(provider instanceof AtomContextProvider);
    }

    @Test
    public void testNullContext() {
        assertNull(provider.newInstance());
        assertNull(provider.branch(null));
        assertNull(provider.join(null, null));

        assertNotNull(provider.serialize(null));
        assertEquals(0, provider.serialize(null).length);
        assertNotNull(provider.serialize(null, 0));
        assertEquals(0, provider.serialize(null, 0).length);

        assertNull(provider.deserialize(null));
        assertNull(provider.deserialize(null, 0, 0));
        assertTrue(provider.isValid(null));

        BaggageContext invalidContext = new BaggageContext() {};
        assertFalse(provider.isValid(invalidContext));
    }

    private Random r;

    @Before
    public void initializeRandom() {
        r = new Random(10);
    }

    private ArrayList<ByteBuffer> genAtoms(int numAtoms, int maxAtomSize) {
        ArrayList<ByteBuffer> atoms = new ArrayList<>(numAtoms);
        for (int i = 0; i < numAtoms; i++) {
            int length = r.nextInt(maxAtomSize);
            byte[] bytes = new byte[length];
            r.nextBytes(bytes);
            atoms.add(ByteBuffer.wrap(bytes));
        }
        return atoms;
    }

    @Test
    public void testDiscard() {
        // Create context
        AtomContext ctx = new AtomContext(genAtoms(10, 10));
        assertNotNull(ctx);
        assertNotNull(ctx.atoms);
        assertEquals(1, ctx.atoms.count);
        assertNotNull(ctx.atoms.object);

        // Discard, check
        RefCount<List<ByteBuffer>> atoms = ctx.atoms;
        provider.discard(ctx);
        assertNull(ctx.atoms);
        assertEquals(0, atoms.count);
        assertNull(atoms.object);
    }

    @Test
    public void testBranchDiscard() {
        // Create context
        AtomContext ctx = new AtomContext(genAtoms(10, 10));
        AtomContext ctx_branched = provider.branch(ctx);

        // Different contexts, same atoms
        assertNotSame(ctx, ctx_branched);
        assertSame(ctx.atoms, ctx_branched.atoms);
        assertEquals(2, ctx.atoms.count);

        // Discard branched copy, check refcount, and that atoms were discarded
        provider.discard(ctx_branched);
        assertEquals(1, ctx.atoms.count);
        assertNull(ctx_branched.atoms);

        // Discard original
        RefCount<List<ByteBuffer>> atoms = ctx.atoms;
        provider.discard(ctx);
        assertNull(ctx.atoms);
        assertEquals(0, atoms.count);
        assertNull(atoms.object);
    }

    @Test
    public void testJoinReuse() {
        AtomContext ctx1 = new AtomContext(genAtoms(3, 10));
        AtomContext ctx2 = new AtomContext(genAtoms(4, 10));
        AtomContext ctx1_branched = provider.branch(ctx1);
        AtomContext ctx2_branched = provider.branch(ctx2);

        RefCount<List<ByteBuffer>> atoms1 = ctx1.atoms;
        RefCount<List<ByteBuffer>> atoms2 = ctx2.atoms;
        assertEquals(2, atoms1.count);
        assertEquals(2, atoms2.count);

        AtomContext ctx3 = provider.join(ctx1, ctx2);
        assertNotNull(ctx3);
        assertNotSame(atoms1, ctx3.atoms);
        assertNotSame(atoms2, ctx3.atoms);

        RefCount<List<ByteBuffer>> atoms3 = ctx3.atoms;
        assertEquals(1, atoms1.count);
        assertEquals(1, atoms2.count);
        assertEquals(1, atoms3.count);

        // Context 1 will have been reused, context 2 nulled out
        assertNull(ctx2.atoms);
        assertEquals(ctx1, ctx3);
    }

    @Test
    public void testSerialize() {
        AtomContext ctx1 = new AtomContext(genAtoms(3, 10));
        byte[] serialized = provider.serialize(ctx1);
        AtomContext ctx2 = provider.deserialize(ByteBuffer.wrap(serialized));

        assertNotSame(ctx1, ctx2);
        assertNotSame(ctx1.atoms, ctx2.atoms);
        assertEquals(1, ctx1.atoms.count);
        assertEquals(1, ctx2.atoms.count);
        assertEquals(ctx1.atoms.object, ctx2.atoms.object);
    }
    
    @Test
    public void testExclusive() {
        AtomContext ctx1 = new AtomContext(genAtoms(3, 10));
        
        AtomContext ctx2 = provider.branch(ctx1);

        RefCount<List<ByteBuffer>> atoms1 = ctx1.atoms;
        RefCount<List<ByteBuffer>> atoms2 = ctx2.atoms;
        List<ByteBuffer> atomsCtx1 = ctx1.atoms.object;
        List<ByteBuffer> atomsCtx2 = ctx2.atoms.object;

        assertSame(atoms1, atoms2);
        assertSame(atomsCtx1, atomsCtx2);
        assertEquals(2, ctx1.atoms.count);
        assertEquals(2, ctx2.atoms.count);
        
        ctx1.toExclusive();
        
        assertNotSame(atoms1, ctx1.atoms);
        assertNotSame(atomsCtx1, ctx1.atoms.object);
        assertEquals(atomsCtx1, ctx1.atoms.object);
        assertEquals(1, ctx1.atoms.count);
        assertEquals(1, ctx2.atoms.count);
        
    }

}
