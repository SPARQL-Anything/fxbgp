package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.fxbgp.AnalyserGrounder;
import io.github.sparqlanything.fxbgp.BGPTestUtils;
import io.github.sparqlanything.fxbgp.FX;
import io.github.sparqlanything.fxbgp.FXBGPAnnotation;
import io.github.sparqlanything.fxbgp.FXModel;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class MatchingTest extends BGPTestUtils {
    private static final Logger L = LoggerFactory.getLogger(MatchingTest.class);
    @Rule
    public TestName testName = new TestName();

    private BasicPattern bp;
    private FXTreePattern pattern;

    @Before
    public void before() throws IOException {
        this.pattern = null;
        this.bp = null;
    }

    @Test
    public void m1() throws IOException {
        prepare(testName.getMethodName());
        Matching m = process();
        Assert.assertFalse(m.isEmpty());
        Assert.assertFalse(m.getMap().isEmpty());
        Assert.assertTrue(m.size() == 3);
        for(Map.Entry<FXNode,Node> entry: m.getMap().entrySet()){

        }
    }

    @Test
    public void m2() throws IOException {
        prepare(testName.getMethodName());
        Matching m = process();
        Assert.assertFalse(m.isEmpty());
        Assert.assertFalse(m.getMap().isEmpty());
        Assert.assertTrue(m.size() == 5);
        FXTreePattern p = pattern();
        System.out.println(p.variables());
        System.out.println(p.nodes());

        L.info("{} {} ",m.getMap().size(), pattern().nodes().size() );
        Assert.assertTrue(m.getMap().size() == pattern().nodes().size());
        // Even if the matching is complete, it is marked as unresolvable when a new node event is triggered
        Assert.assertTrue(m.isUnresolvable());
    }


    private void prepare(String easyBGPName) throws IOException {
        L.info("Test " + easyBGPName + " started");
        this.bp = readBGP("./stream/" + easyBGPName);
    }

    private FXTreePattern pattern() {
        if(pattern == null) {
            // From bp to fx tree patterns
            AnalyserGrounder ag = new AnalyserGrounder(new Properties(), FXModel.getFXModel());
            Set<FXBGPAnnotation> annotations = ag.annotate(new OpBGP(this.bp), true);
            Assert.assertEquals(1, annotations.size());
            try {
                pattern = FXTreePattern.make(annotations.iterator().next());
            } catch (NotATreeException e) {
                throw new RuntimeException(e);
            }
        }
        return pattern;
    }

    private Matching process()  {
        // We simulate a csv with the following structure
        // A,B,C
        // 1,2,3
        Node root = b();
        Node r_1 = RDF.li(1).asNode();
        Node r_2 = RDF.li(2).asNode();
        Node a = RDF.type.asNode();
        Node fxr = NodeFactory.createURI("http://sparql.xyz/facade-x/ns/root");
        Node row1 = b();
        Node row2 = b();
        Node c_1 = RDF.li(1).asNode();
        Node c_2 = RDF.li(2).asNode();
        Node c_3 = RDF.li(3).asNode();
        Node A = l("A");
        Node B = l("B");
        Node C = l("C");
        Node _1 = l("1");
        Node _2 = l("2");
        Node _3 = l("3");

        Matching m = new Matching(pattern().getRoot(), root);
        m.check(a, FX.TypeProperty);
        m.check(fxr, FX.Root);
        m.check(r_1, FX.SlotNumber);
        m.check(row1, FX.Container);
        m.check(c_1, FX.SlotNumber);
        m.check(A, FX.Value);
        m.check(c_2, FX.SlotNumber);
        m.check(B, FX.Value);
        m.check(c_3, FX.SlotNumber);
        m.check(C, FX.Value);
        m.endContainer();
        m.check(r_2, FX.SlotNumber);
        m.check(row2, FX.Container);
        m.check(c_1, FX.SlotNumber);
        m.check(_1, FX.Value);
        m.check(c_2, FX.SlotNumber);
        m.check(_2, FX.Value);
        m.check(c_3, FX.SlotNumber);
        m.check(_3, FX.Value);
        m.endContainer();
        m.endContainer();
        return m;
    }
}
