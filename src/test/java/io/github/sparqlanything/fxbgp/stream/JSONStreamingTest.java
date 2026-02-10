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
}
