package io.github.sparqlanything.fxbgp.joins;

import io.github.sparqlanything.fxbgp.FX;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceContainer;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.impl.DataSourceContainerImpl;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.impl.DataSourceTypeImpl;
import io.github.sparqlanything.model.IRIArgument;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class TestCTPT {

    @Rule
    public TestName name = new TestName();

    static Set<List<FX>> pattern = new HashSet<>();

    static {
        pattern.add(List.of(FX.Container, FX.TypeProperty, FX.Type));
    }

    // (_, a:TP, A:T) [A] :::

    @Test
    public void test4() throws IOException, URISyntaxException {
        TestUtils.assertEquals(name.getMethodName(), pattern);
    }

    // (_, ?p:TP, t:T) [t] ::: ?p = rdf:type

    @Test
    public void test5() throws IOException, URISyntaxException {
        TestUtils.assertEquals(name.getMethodName(), pattern);
    }

    // (_, a:TP, t1:T) [t] ::: X

    @Test
    public void test4_test4n_test4n_xml() throws IOException, URISyntaxException {
        TestUtils.assertEquals(name.getMethodName(), pattern, false);
    }


    // (_, ?p:TP, A1:T) (_, ?p:TP, A2:T) [A1, A2] ::: ?p = rdf:type
    @Test
    public void sc_doubleType_doubleType_sc() throws IOException, URISyntaxException {
        Properties properties = new Properties();
        properties.setProperty(IRIArgument.ROOT.toString(), "https://example.org/root");
        DataSourceContainer dataSourceContainer = new DataSourceContainerImpl("A", properties);
        dataSourceContainer.addType(new DataSourceTypeImpl("A1", properties));
        dataSourceContainer.addType(new DataSourceTypeImpl("A2", properties));

        TestUtils.assertEquals(name.getMethodName(), pattern, true, Set.of(dataSourceContainer), properties);
    }


    // (_, ?p1:TP, A1:T) (_, ?p2:TP, A2:T) [A1, A2] ::: ?p1 = rdf:type, ?p2 = rdf:type

    @Test
    public void sc_doubleTypeDoublePredicate_doubleTypeDoublePredicate_sc() throws IOException, URISyntaxException {
        Properties properties = new Properties();
        properties.setProperty(IRIArgument.ROOT.toString(), "https://example.org/root");
        DataSourceContainer dataSourceContainer = new DataSourceContainerImpl("A", properties);
        dataSourceContainer.addType(new DataSourceTypeImpl("A1", properties));
        dataSourceContainer.addType(new DataSourceTypeImpl("A2", properties));

        TestUtils.assertEquals(name.getMethodName(), pattern, true, Set.of(dataSourceContainer), properties);
    }

    /*
    case 5:
        (_, ?p1:TP, ?t1:T) (_, ?p2:TP, ?t2:T) [t1, t2] :::
            <?p1 = rdf:type, ?p2 = rdf:type, ?t1 = t1, ?t2 = ?t1>
            <?p1 = rdf:type, ?p2 = rdf:type, ?t1 = t2, ?t2 = ?t2>
            <?p1 = rdf:type, ?p2 = rdf:type, ?t1 = t1, ?t2 = ?t2>
            <?p1 = rdf:type, ?p2 = rdf:type, ?t1 = t2, ?t2 = ?t1>
     */
    @Test
    public void sc_doubleTypeDoublePredicateVar_doubleTypeDoublePredicateVar_sc() throws IOException, URISyntaxException {
        Properties properties = new Properties();
        properties.setProperty(IRIArgument.ROOT.toString(), "https://example.org/root");
        DataSourceContainer dataSourceContainer = new DataSourceContainerImpl("A", properties);
        dataSourceContainer.addType(new DataSourceTypeImpl("A1", properties));
        dataSourceContainer.addType(new DataSourceTypeImpl("A2", properties));

        TestUtils.assertEquals(name.getMethodName(), pattern, true, Set.of(dataSourceContainer), properties);
    }

    // (_, a:TP, A:T) (_, a:TP, t2:T) [A] ::: NO MATCH
    @Test
    public void test6_doubleType_noMatch_xml() throws IOException, URISyntaxException {
        TestUtils.assertEquals(name.getMethodName(), pattern, false);
    }

    // (_, a:TP, t1:T) (_, a:TP, t2:T) [t1, t] ::: X
    @Test
    public void sc_doubleType_noMatch_sc() throws IOException, URISyntaxException {
        Properties properties = new Properties();
        properties.setProperty(IRIArgument.ROOT.toString(), "https://example.org/root");
        DataSourceContainer dataSourceContainer = new DataSourceContainerImpl("A", properties);
        dataSourceContainer.addType(new DataSourceTypeImpl("A1", properties));
        dataSourceContainer.addType(new DataSourceTypeImpl("A3", properties));

        TestUtils.assertEquals(name.getMethodName(), pattern, false, Set.of(dataSourceContainer), properties);
    }
}
