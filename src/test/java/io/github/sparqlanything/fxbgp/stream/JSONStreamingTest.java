package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.csv.CSVTriplifier;
import io.github.sparqlanything.json.JSONTriplifier;
import io.github.sparqlanything.model.BaseFacadeXGraphBuilder;
import io.github.sparqlanything.model.FacadeXGraphBuilder;
import io.github.sparqlanything.model.IRIArgument;
import io.github.sparqlanything.model.TriplifierHTTPException;
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
    public void testEquals() throws TriplifierHTTPException, IOException, NotATreeException {
        JSONTriplifier triplifier = new JSONTriplifier();
        String input = getClass().getClassLoader().getResource("./stream/test1.json").getPath();
        Properties p = new Properties();
        p.setProperty(IRIArgument.LOCATION.toString(), input.toString());
        //p.setProperty(JSONTriplifier.PROPERTY_JSONINCLUDENULLVALUES.toString(), nullValues ? "true" : "false");
        p.setProperty("blank-nodes", "true");
        p.setProperty("media-type", "application/json");
        FacadeXGraphBuilder gb = new BaseFacadeXGraphBuilder(p);
        triplifier.triplify(p, gb);
        DatasetGraph dg1 = gb.getDatasetGraph();
        long size1 = dg1.size();
        //
        BasicPattern bp = readBGP("./stream/all");
        QueryIterator it = executor.exec(new OpBGP(bp), p);
        DatasetGraph dg2 = new DatasetGraphInMemory();
        Node graph = NodeFactory.createURI(input.toString());
        while(it.hasNext()) {
            Binding binding = it.next();
            Node subject = binding.get("a");
            Node predicate = binding.get("b");
            Node object = binding.get("c");
            dg2.add(graph, subject, predicate, object);
        }
        Assert.assertEquals(size1, dg2.size());

    }
}
