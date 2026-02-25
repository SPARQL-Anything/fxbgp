package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.model.IRIArgument;
import io.github.sparqlanything.xml.XMLTriplifier;
import io.github.sparqlanything.model.TriplifierHTTPException;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.iterator.QueryIter1;
import org.apache.jena.sparql.engine.iterator.QueryIterAbortable;
import org.apache.jena.sparql.engine.iterator.QueryIteratorBase;
import org.apache.jena.sparql.engine.main.QC;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;
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


    @Test
    public void books_xml_all_ABCEquals() throws TriplifierHTTPException, IOException, NotATreeException {
        prepare(testName.getMethodName());
        testABCEquals(new XMLTriplifier(), properties());

        prepare(testName.getMethodName());
        testABCEquals(new XMLTriplifier(), properties(false ));
    }

    @Test
    public void books2_xml_all_ABCEquals() throws TriplifierHTTPException, IOException, NotATreeException {
        prepare(testName.getMethodName());
        testABCEquals(new XMLTriplifier(), properties());

        prepare(testName.getMethodName());
        testABCEquals(new XMLTriplifier(), properties(false ));
    }

    @Test
    public void books2_xml_book() throws TriplifierHTTPException, IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<Binding> bindings = getBindings(properties());
        show(bindings.iterator());
        L.info("{}", bindings.size());
        Assert.assertEquals(1, bindings.size());
    }

    @Test
    public void books2_xml_book2() throws TriplifierHTTPException, IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<Binding> bindings = getBindings(properties());
        show(bindings.iterator());
        L.info("{}", bindings.size());
        Assert.assertEquals(1, bindings.size());
    }

    @Test
    public void testXYZ(){
        Properties p  = new Properties();
        p.setProperty(IRIArgument.LOCATION.toString(), getClass().getClassLoader().getResource("./stream/books.xml").getFile());
        XMLStreamParser sp = new XMLStreamParser(p);
        while(sp.hasNext()){
            FXEventType event = sp.nextType();
            L.info("hasNextEvent: {}",event);
            switch (event){
                case Type -> L.info("Type {}", sp.getType());
                case Value -> L.info("Value {}", sp.getValue());
                case StartRoot -> L.info("StartRoot {}", sp.getRoot());
                case EndRoot -> L.info("EndRoot {}", sp.getRoot());
                case StartContainer ->  L.info("StartContainer {}", sp.getContainer());
                case EndContainer ->  L.info("EndContainer {}", sp.getContainer());
                case SlotString -> L.info("SlotString {}", sp.getSlotString());
                case SlotNumber ->  L.info("SlotNumber {}", sp.getSlotNumber());
                case StartDataSource ->   L.info("StartDataSource {}", sp.getDataSource());
                default -> L.info("??? {}", event);
            }
        }
    }
}
