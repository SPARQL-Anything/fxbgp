package io.github.sparqlanything.fxbgp.stream;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.junit.Test;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.BasicPattern;

import static io.github.sparqlanything.fxbgp.stream.BMAUtils.connected;
import static io.github.sparqlanything.fxbgp.stream.BMAUtils.singleRoot;
import static io.github.sparqlanything.fxbgp.stream.BMAUtils.varInPSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
public class BMAUtilsTest {
    @Test
    public void testSingleRoot_chain() {
        // ?s :p ?o . ?o :q ?z  →  only ?s is a root
        BasicPattern bp = new BasicPattern();
        Node s = NodeFactory.createVariable("s");
        Node o = NodeFactory.createVariable("o");
        Node z = NodeFactory.createVariable("z");
        Node p1 = NodeFactory.createURI("http://example.org/p");
        Node p2 = NodeFactory.createURI("http://example.org/q");
        bp.add(Triple.create(s, p1, o));
        bp.add(Triple.create(o, p2, z));
        assertTrue(singleRoot(bp));
    }

    @Test
    public void testSingleRoot_star() {
        // ?s :p1 ?o1 . ?s :p2 ?o2  →  only ?s is a root
        BasicPattern bp = new BasicPattern();
        Node s  = NodeFactory.createVariable("s");
        Node o1 = NodeFactory.createVariable("o1");
        Node o2 = NodeFactory.createVariable("o2");
        Node p1 = NodeFactory.createURI("http://example.org/p1");
        Node p2 = NodeFactory.createURI("http://example.org/p2");
        bp.add(Triple.create(s, p1, o1));
        bp.add(Triple.create(s, p2, o2));
        assertTrue(singleRoot(bp));
    }

    @Test
    public void testSingleRoot_twoRoots() {
        // ?s1 :p ?o . ?s2 :q ?o  →  both ?s1 and ?s2 are roots
        BasicPattern bp = new BasicPattern();
        Node s1 = NodeFactory.createVariable("s1");
        Node s2 = NodeFactory.createVariable("s2");
        Node o  = NodeFactory.createVariable("o");
        Node p1 = NodeFactory.createURI("http://example.org/p");
        Node p2 = NodeFactory.createURI("http://example.org/q");
        bp.add(Triple.create(s1, p1, o));
        bp.add(Triple.create(s2, p2, o));
        assertFalse(singleRoot(bp));
    }

    @Test
    public void testSingleRoot_cycle() {
        // ?s :p ?o . ?o :q ?s  →  every node is also an object, no root
        BasicPattern bp = new BasicPattern();
        Node s = NodeFactory.createVariable("s");
        Node o = NodeFactory.createVariable("o");
        Node p1 = NodeFactory.createURI("http://example.org/p");
        Node p2 = NodeFactory.createURI("http://example.org/q");
        bp.add(Triple.create(s, p1, o));
        bp.add(Triple.create(o, p2, s));
        assertFalse(singleRoot(bp));
    }

    @Test
    public void testConnected_chain() {
        // ?s :p ?o . ?o :q ?z  →  all nodes reachable from any starting point
        BasicPattern bp = new BasicPattern();
        Node s  = NodeFactory.createVariable("s");
        Node o  = NodeFactory.createVariable("o");
        Node z  = NodeFactory.createVariable("z");
        Node p1 = NodeFactory.createURI("http://example.org/p");
        Node p2 = NodeFactory.createURI("http://example.org/q");
        bp.add(Triple.create(s, p1, o));
        bp.add(Triple.create(o, p2, z));
        assertTrue(connected(bp));
    }

    @Test
    public void testConnected_star() {
        // ?s :p1 ?o1 . ?s :p2 ?o2  →  all nodes reachable via ?s
        BasicPattern bp = new BasicPattern();
        Node s  = NodeFactory.createVariable("s");
        Node o1 = NodeFactory.createVariable("o1");
        Node o2 = NodeFactory.createVariable("o2");
        Node p1 = NodeFactory.createURI("http://example.org/p1");
        Node p2 = NodeFactory.createURI("http://example.org/p2");
        bp.add(Triple.create(s, p1, o1));
        bp.add(Triple.create(s, p2, o2));
        assertTrue(connected(bp));
    }

    @Test
    public void testConnected_disconnected() {
        // ?s1 :p ?o1 . ?s2 :q ?o2  →  two isolated components
        BasicPattern bp = new BasicPattern();
        Node s1 = NodeFactory.createVariable("s1");
        Node o1 = NodeFactory.createVariable("o1");
        Node s2 = NodeFactory.createVariable("s2");
        Node o2 = NodeFactory.createVariable("o2");
        Node p1 = NodeFactory.createURI("http://example.org/p");
        Node p2 = NodeFactory.createURI("http://example.org/q");
        bp.add(Triple.create(s1, p1, o1));
        bp.add(Triple.create(s2, p2, o2));
        assertFalse(connected(bp));
    }

    @Test
    public void testConnected_bridge() {
        // ?s1 :p ?m . ?m :q ?s2  →  two pairs joined by shared ?m (undirected)
        BasicPattern bp = new BasicPattern();
        Node s1 = NodeFactory.createVariable("s1");
        Node m  = NodeFactory.createVariable("m");
        Node s2 = NodeFactory.createVariable("s2");
        Node p1 = NodeFactory.createURI("http://example.org/p");
        Node p2 = NodeFactory.createURI("http://example.org/q");
        bp.add(Triple.create(s1, p1, m));
        bp.add(Triple.create(s2, p2, m));
        assertTrue(connected(bp));
    }

    @Test
    public void testVarInPSize_noVarPredicate() {
        // ?s :p ?o  →  no variables in predicate position
        BasicPattern bp = new BasicPattern();
        Node s = NodeFactory.createVariable("s");
        Node o = NodeFactory.createVariable("o");
        Node p = NodeFactory.createURI("http://example.org/p");
        bp.add(Triple.create(s, p, o));
        assertEquals(0, varInPSize(bp));
    }

    @Test
    public void testVarInPSize_oneVarPredicate() {
        // ?s ?p ?o  →  one variable in predicate position
        BasicPattern bp = new BasicPattern();
        Node s  = NodeFactory.createVariable("s");
        Node p  = NodeFactory.createVariable("p");
        Node o  = NodeFactory.createVariable("o");
        bp.add(Triple.create(s, p, o));
        assertEquals(1, varInPSize(bp));
    }

    @Test
    public void testVarInPSize_twoVarPredicatesSameSubject() {
        // ?s ?p1 ?o1 . ?s ?p2 ?o2  →  both var-predicates share the same subject
        BasicPattern bp = new BasicPattern();
        Node s  = NodeFactory.createVariable("s");
        Node p1 = NodeFactory.createVariable("p1");
        Node p2 = NodeFactory.createVariable("p2");
        Node o1 = NodeFactory.createVariable("o1");
        Node o2 = NodeFactory.createVariable("o2");
        bp.add(Triple.create(s, p1, o1));
        bp.add(Triple.create(s, p2, o2));
        assertEquals(2, varInPSize(bp));
    }

    @Test
    public void testVarInPSize_twoVarPredicatesUnderAncestor() {
        // ?root :p ?mid . ?mid ?pv1 ?a . ?mid ?pv2 ?b
        // ?root is ancestor of ?mid; its subtree covers both ?pv1 and ?pv2
        BasicPattern bp = new BasicPattern();
        Node root = NodeFactory.createVariable("root");
        Node mid  = NodeFactory.createVariable("mid");
        Node a    = NodeFactory.createVariable("a");
        Node b    = NodeFactory.createVariable("b");
        Node p    = NodeFactory.createURI("http://example.org/p");
        Node pv1  = NodeFactory.createVariable("pv1");
        Node pv2  = NodeFactory.createVariable("pv2");
        bp.add(Triple.create(root, p,   mid));
        bp.add(Triple.create(mid,  pv1, a));
        bp.add(Triple.create(mid,  pv2, b));
        assertEquals(2, varInPSize(bp));
    }
}
