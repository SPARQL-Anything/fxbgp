package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.csv.CSVTriplifier;
import io.github.sparqlanything.json.JSONTriplifier;
import io.github.sparqlanything.model.BaseFacadeXGraphBuilder;
import io.github.sparqlanything.model.FacadeXGraphBuilder;
import io.github.sparqlanything.model.IRIArgument;
import io.github.sparqlanything.model.TriplifierHTTPException;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.mem.DatasetGraphInMemory;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;

public class JSONStreamingTest extends FXStreamExecutorTest{

    @Test
    public void test1_json_all() throws IOException, NotATreeException {
        String name = testName.getMethodName();
        prepare(name);
        Set<Binding> it = set(executor.exec(getOpBGP(), propertiesNullValueTrue()));
        show(it.iterator());
        show(getGraphFrom_abc(it));
        Assert.assertEquals(8,it.size());
    }

    @Test
    public void test3_json_all() throws IOException, NotATreeException {
        String name = testName.getMethodName();
        prepare(name);
        Set<Binding> it = set(executor.exec(getOpBGP(), propertiesNullValueTrue()));
        show(it.iterator());
        show(getGraphFrom_abc(it));
        Assert.assertEquals(4,it.size());
    }

    @Test
    public void test2_json_a1() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        QueryIterator it = executor.exec(getOpBGP(), properties());
        Assert.assertEquals(6,set(it).size());
        show(it);
    }

    @Test
    public void test2_json_a2() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        QueryIterator it = executor.exec(getOpBGP(), properties());
        Assert.assertEquals(5,set(it).size());
        show(it);
    }

    @Test
    public void test1_json_all_ABCEquals() throws TriplifierHTTPException, IOException, NotATreeException {
        prepare(testName.getMethodName());
        testABCEquals(new JSONTriplifier(), properties());

        prepare(testName.getMethodName());
        testABCEquals(new JSONTriplifier(), properties(false));
    }

    @Test
    public void test2_json_all_ABCEquals() throws TriplifierHTTPException, IOException, NotATreeException {
        prepare(testName.getMethodName());
        testABCEquals(new JSONTriplifier(), properties());

        prepare(testName.getMethodName());
        testABCEquals(new JSONTriplifier(), properties(false));
    }

    @Test
    public void example_json_example() throws IOException {
        prepare(testName.getMethodName());
        testSelectStarEquals(new JSONTriplifier(), properties());
        //testSelectStarEqualsResult.forEach(System.out::println);
    }

    /**
     * Skipping example of Section 5: ?n is matched to "B" by the slot name, then
     * ?i must skip rdf:_1 (value "A" differs from ?n) and match rdf:_2.
     * Exactly one solution: ?i = rdf:_2, ?n = "B".
     */
    @Test
    public void skipjoin_json_skipjoin() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        for (boolean filtering : new boolean[]{true, false}) {
            Set<Binding> it = set(executor.exec(getOpBGP(), properties(true, false, filtering)));
            show(it.iterator());
            Assert.assertEquals("events-filtering=" + filtering, 1, it.size());
            Binding b = it.iterator().next();
            Assert.assertEquals(NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#_2"),
                    b.get(org.apache.jena.sparql.core.Var.alloc("i")));
            Assert.assertEquals(NodeFactory.createLiteralString("B"),
                    b.get(org.apache.jena.sparql.core.Var.alloc("n")));
        }
    }
}
