package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.fxbgp.AnalyserGrounder;
import io.github.sparqlanything.fxbgp.BGPTestUtils;
import io.github.sparqlanything.fxbgp.FX;
import io.github.sparqlanything.fxbgp.FXBGPAnnotation;
import io.github.sparqlanything.fxbgp.FXModel;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QuerySolution;
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
    private Set<QuerySolution> solutions;
    private FXQuerySolutionBuilder builder;

    @Before
    public void before(){
        this.pattern = null;
        this.bp = null;
        this.solutions = new HashSet<>();
        this.builder = null;
    }

    @Test
    public void m1() throws IOException, NotAStarException {
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
        //Assert.assertEquals(1, this.solutions.size());
        QuerySolution qs = this.solutions.iterator().next();
        qs.contains("a");
        qs.contains("A");
        show();
    }

    public void prepare(String easyBGPName) throws IOException{
        this.bp = readBGP("./stream/" + easyBGPName);
        this.builder = new FXQuerySolutionBuilder(pattern(), this.solutions);
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
            pattern = FXTreePattern.make(annotations.iterator().next());
        }
        return pattern;
    }

    private void show(){
        for(QuerySolution qs : this.solutions){
            Iterator<String> it = qs.varNames();
            while(it.hasNext()){
                String var = it.next();
                L.info("Solution: {} -> {}", var, qs.get(var));
            }
        }
    }
    private void process() {
        // We simulate a csv with the following structure
        // A,B,C
        // 1,2,3
        Node root = b();
        Node a = RDF.type.asNode();
        Node fxr = NodeFactory.createURI("http://sparql.xyz/facade-x/ns/root");
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
        this.builder.startContainer(root);
        this.builder.onTypeProperty();
        this.builder.onType(fxr);
        this.builder.onSlotNumber(r_1);
        this.builder.startContainer(row1);
        this.builder.onSlotNumber(c_1);
        this.builder.onValue(A);
        this.builder.onSlotNumber(c_2);
        this.builder.onValue(B);
        this.builder.onSlotNumber(c_3);
        this.builder.onValue(C);
        this.builder.endContainer();
        this.builder.onSlotNumber(r_2);
        this.builder.startContainer(row2);
        this.builder.onSlotNumber(c_1);
        this.builder.onValue(_1);
        this.builder.onSlotNumber(c_2);
        this.builder.onValue(_2);
        this.builder.onSlotNumber(c_3);
        this.builder.onValue(_3);
        this.builder.endContainer();
        this.builder.endContainer();
    }
}
