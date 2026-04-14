package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.fxbgp.AnalyserGrounder;
import io.github.sparqlanything.fxbgp.BGPTestUtils;
import io.github.sparqlanything.fxbgp.FXBGPAnnotation;
import io.github.sparqlanything.fxbgp.FXModel;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

public class FXQuerySolutionBuilderTest extends BGPTestUtils {
    private static final Logger L = LoggerFactory.getLogger(FXQuerySolutionBuilderTest.class);
    @Rule
    public TestName testName = new TestName();

    private BasicPattern bp;
    private FXTreePattern pattern;
    private Set<Binding> solutions;
    private FXQuerySolutionBuilder builder;
    private FXNodeEventListener proxy;

    @Before
    public void before(){
        this.pattern = null;
        this.bp = null;
        this.solutions = new HashSet<>();
        this.builder = null;
        this.proxy = null;
    }

    @Test
    public void m1() throws IOException, NotATreeException {
        prepare(this.testName.getMethodName());
        process();
        Assert.assertEquals(1, this.solutions.size());
        show();
    }

    @Test
    public void m2() throws IOException {
        prepare(this.testName.getMethodName());
        process();
        Assert.assertEquals(1, this.solutions.size());
        show();
    }

    @Test
    public void m3() throws IOException {
        prepare(this.testName.getMethodName());
        process();
        Assert.assertEquals(1, this.solutions.size());
        Binding qs = this.solutions.iterator().next();
        qs.contains("a");
        qs.contains("A");
        show();
    }

    public void prepare(String easyBGPName) throws IOException{
        this.bp = readBGP("./stream/" + easyBGPName);
        SharedPathAccessor accessor = new SharedPathAccessor();
        this.builder = new FXQuerySolutionBuilder(pattern(), this.solutions, accessor);
        this.proxy = FXProxyEventListener.make(
                java.util.Collections.singleton(this.builder),
                FXProxyEventListener.DEFAULT_PARALLEL_THRESHOLD,
                accessor,
                this.bp.getList());
    }

    private FXTreePattern pattern() {
        if(pattern == null) {
            // From bp to fx tree patterns
            AnalyserGrounder ag = new AnalyserGrounder(new Properties(), FXModel.getFXModel());
            Set<FXBGPAnnotation> annotations = ag.annotate(new OpBGP(this.bp), true);
            for(FXBGPAnnotation annotation : annotations) {
                L.info("Annotation: {}", annotation);
            }
            Assert.assertEquals(1, annotations.size());
            try {
                pattern = FXTreePattern.make(annotations.iterator().next());
            } catch (NotATreeException e) {
                throw new RuntimeException(e);
            }
        }
        return pattern;
    }

    private void show(){
        for(Binding qs : this.solutions){
            Iterator<Var> it = qs.vars();
            while(it.hasNext()){
                String var = it.next().getVarName();
                L.info("Binding: {} -> {}", var, qs.get(var));
            }
        }
    }
    private void process() {
        // We simulate a csv with the following structure
        // A,B,C
        // 1,2,3
        Node root = b();
        //Node a = RDF.type.asNode();
        //Node fxr = NodeFactory.createURI("http://sparql.xyz/facade-x/ns/root");
        Node r_1 = RDF.li(1).asNode();
        Node row1 = b();
        Node r_2 = RDF.li(2).asNode();
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

        this.proxy.startDataSource(root);

        this.proxy.startContainer(root);
        this.proxy.onTypeProperty();
        this.proxy.onTypeRoot();

        this.proxy.onSlotNumber(r_1);

        this.proxy.startContainer(row1);
        this.proxy.onSlotNumber(c_1);
        this.proxy.onValue(A);
        this.proxy.onSlotNumber(c_2);
        this.proxy.onValue(B);
        this.proxy.onSlotNumber(c_3);
        this.proxy.onValue(C);
        this.proxy.endContainer();

        this.proxy.onSlotNumber(r_2);

        this.proxy.startContainer(row2);
        this.proxy.onSlotNumber(c_1);
        this.proxy.onValue(_1);
        this.proxy.onSlotNumber(c_2);
        this.proxy.onValue(_2);
        this.proxy.onSlotNumber(c_3);
        this.proxy.onValue(_3);
        this.proxy.endContainer();

        this.proxy.endContainer();
    }
}
