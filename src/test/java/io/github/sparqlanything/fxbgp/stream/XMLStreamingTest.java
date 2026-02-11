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
    }


    @Test
    public void test1_xml_all_ABCEquals() throws TriplifierHTTPException, IOException, NotATreeException {
        prepare(testName.getMethodName());
        testABCEquals(new XMLTriplifier(), properties());

        prepare(testName.getMethodName());
        testABCEquals(new XMLTriplifier(), properties(false ));
    }

    @Test
    public void test2_xml_all() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<Binding> it = set(executor.exec(getOpBGP(), properties(true)));
        //
        show(getGraphFrom_abc(it));
    }

    @Test
    public void test3_xml_all() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<Binding> it = set(executor.exec(getOpBGP(), properties(true)));
        //
        show(getGraphFrom_abc(it));
    }

    @Test
    public void test3_xml_s12() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<Binding> it = set(executor.exec(getOpBGP(), properties(true)));
        //
        show(it.iterator());
    }

    @Test
    public void test2_xml_all_ABCEquals() throws TriplifierHTTPException, IOException, NotATreeException {
        prepare(testName.getMethodName());
        testABCEquals(new XMLTriplifier(), properties());

        prepare(testName.getMethodName());
        testABCEquals(new XMLTriplifier(), properties(false ));
    }


    @Test
    public void test3_xml_all_ABCEquals() throws TriplifierHTTPException, IOException, NotATreeException {
        prepare(testName.getMethodName());
        testABCEquals(new XMLTriplifier(), properties());

        prepare(testName.getMethodName());
        testABCEquals(new XMLTriplifier(), properties(false ));
    }


    public void testXYZ(){
        // s12
        // ?a-Container.673384643[ ?b1-SlotString.1574573264[ ?c-Container.498134055 ] ?b2-SlotNumber.1033880954[ ?c-Container.223000346 ] ]
    }
}
