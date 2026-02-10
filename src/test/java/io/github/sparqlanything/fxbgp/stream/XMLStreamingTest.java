package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.xml.XMLTriplifier;
import io.github.sparqlanything.model.TriplifierHTTPException;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Set;

public class XMLStreamingTest extends FXStreamExecutorTest{

    @Test
    public void test1_xml_all_basic() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<Binding> it = set(executor.exec(getOpBGP(), properties(false)));
        //
        show(getGraphFrom_abc(it));
        Assert.assertTrue(it.size() == 10);
//        testEquals(new XMLTriplifier());
    }


    @Test
    public void test1_xml_all_ABCEquals() throws TriplifierHTTPException, IOException, NotATreeException {
        prepare(testName.getMethodName());
        testABCEquals(new XMLTriplifier(), properties());

        prepare(testName.getMethodName());
        testABCEquals(new XMLTriplifier(), properties(false ));
    }



}
