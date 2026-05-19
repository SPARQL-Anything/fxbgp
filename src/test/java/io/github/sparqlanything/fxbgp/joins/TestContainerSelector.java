package io.github.sparqlanything.fxbgp.joins;

import io.github.sparqlanything.fxbgp.BGPTestUtils;
import io.github.sparqlanything.fxbgp.stream.join.ContainerSelector;
import io.github.sparqlanything.fxbgp.stream.join.ContainerSelectorImpl;
import io.github.sparqlanything.fxbgp.stream.join.listeners.impl.DataSourceContainerCollectorSelectorListenerImpl;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceContainer;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.impl.DataSourceContainerImpl;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.TriplePatternContainer;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.TriplePatternRoot;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.TriplePatternTypeProperty;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.impl.TriplePatternContainerImpl;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.impl.TriplePatternRootImpl;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.impl.TriplePatternTypePropertyImpl;
import io.github.sparqlanything.fxbgp.stream.join.parsers.XMLParser;
import io.github.sparqlanything.model.IRIArgument;
import io.github.sparqlanything.model.Triplifier;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class TestContainerSelector {

    @Rule
    public TestName name = new TestName();

    private String getInputFilename(String methodName) throws URISyntaxException {
        methodName = methodName.contains("$") ? methodName.substring(0, methodName.indexOf('$')) : methodName;
        URL url = BGPTestUtils.class.getClassLoader().getResource("./joins/xml/" + methodName + ".xml");
        Assert.assertNotNull(url);
        return url.toURI().toString();
    }

    @Test
    public void test1() throws URISyntaxException {
        Properties properties = new Properties();
        properties.setProperty(IRIArgument.LOCATION.toString(), getInputFilename(name.getMethodName()));
        Node s = NodeFactory.createVariable("s");
        TriplePatternContainer triplePatternContainer = new TriplePatternContainerImpl(s, properties);
        ContainerSelector containerSelector = new ContainerSelectorImpl(triplePatternContainer);
        DataSourceContainerCollectorSelectorListenerImpl listener = new DataSourceContainerCollectorSelectorListenerImpl(containerSelector);
        XMLParser xmlParser = new XMLParser(properties, listener);
        xmlParser.parse();
        Set<DataSourceContainer> expected = new HashSet<>();
        expected.add(new DataSourceContainerImpl("A/B_1", properties));
        expected.add(new DataSourceContainerImpl("A/B_2", properties));
        expected.add(new DataSourceContainerImpl("A/B_2/C_1", properties));
        expected.add(new DataSourceContainerImpl("A", properties, true));
        Assert.assertEquals(expected, listener.getCollectedContainers());
    }

    public ContainerSelector getContainerSelector(Properties properties, boolean subjectConcrete, boolean predicateConcrete, boolean objectConcrete) {
        Node s = NodeFactory.createVariable("s");
        if (subjectConcrete)
            s = NodeFactory.createURI(Triplifier.getRootArgument(properties) + "A");
        TriplePatternContainer triplePatternContainer = new TriplePatternContainerImpl(s, properties);
        ContainerSelector containerSelector = new ContainerSelectorImpl(triplePatternContainer);
        Node p = NodeFactory.createVariable("p");
        if (predicateConcrete)
            p = RDF.type.asNode();
        TriplePatternTypeProperty triplePatternTypeProperty = new TriplePatternTypePropertyImpl(p, properties);
        Node o = NodeFactory.createVariable("o");
        if (objectConcrete)
            o = NodeFactory.createURI(Triplifier.FACADE_X_TYPE_ROOT);
        TriplePatternRoot triplePatternRoot = new TriplePatternRootImpl(o, properties);
        containerSelector.setRootTriplePattern(triplePatternTypeProperty, triplePatternRoot);
        return containerSelector;
    }

    @Test
    public void test1$root() throws URISyntaxException {
        Properties properties = new Properties();
        properties.setProperty(IRIArgument.LOCATION.toString(), getInputFilename(name.getMethodName()));
        ContainerSelector containerSelector = getContainerSelector(properties, true, false, false);
        DataSourceContainerCollectorSelectorListenerImpl listener = new DataSourceContainerCollectorSelectorListenerImpl(containerSelector);
        XMLParser xmlParser = new XMLParser(properties, listener);
        xmlParser.parse();
        Set<DataSourceContainer> expected = new HashSet<>();
        expected.add(new DataSourceContainerImpl("A", properties, true));
        Assert.assertEquals(expected, listener.getCollectedContainers());
    }

    @Test
    public void test1$rootSubjectConcrete() throws URISyntaxException {
        Properties properties = new Properties();
        properties.setProperty(IRIArgument.LOCATION.toString(), getInputFilename(name.getMethodName()));
        ContainerSelector containerSelector = getContainerSelector(properties, true, false, true);
        DataSourceContainerCollectorSelectorListenerImpl listener = new DataSourceContainerCollectorSelectorListenerImpl(containerSelector);
        XMLParser xmlParser = new XMLParser(properties, listener);
        xmlParser.parse();
        Set<DataSourceContainer> expected = new HashSet<>();
        expected.add(new DataSourceContainerImpl("A", properties, true));
        Assert.assertEquals(expected, listener.getCollectedContainers());
    }

    @Test
    public void test1$rootSubjectPredicateConcrete() throws URISyntaxException {
        Properties properties = new Properties();
        properties.setProperty(IRIArgument.LOCATION.toString(), getInputFilename(name.getMethodName()));
        ContainerSelector containerSelector = getContainerSelector(properties, true, true, false);
        DataSourceContainerCollectorSelectorListenerImpl listener = new DataSourceContainerCollectorSelectorListenerImpl(containerSelector);
        XMLParser xmlParser = new XMLParser(properties, listener);
        xmlParser.parse();
        Set<DataSourceContainer> expected = new HashSet<>();
        expected.add(new DataSourceContainerImpl("A", properties, true));
        Assert.assertEquals(expected, listener.getCollectedContainers());
    }

    @Test
    public void test1$rootSubjectPredicateObjectConcrete() throws URISyntaxException {
        Properties properties = new Properties();
        properties.setProperty(IRIArgument.LOCATION.toString(), getInputFilename(name.getMethodName()));
        ContainerSelector containerSelector = getContainerSelector(properties, true, true, true);
        DataSourceContainerCollectorSelectorListenerImpl listener = new DataSourceContainerCollectorSelectorListenerImpl(containerSelector);
        XMLParser xmlParser = new XMLParser(properties, listener);
        xmlParser.parse();
        Set<DataSourceContainer> expected = new HashSet<>();
        expected.add(new DataSourceContainerImpl("A", properties, true));
        Assert.assertEquals(expected, listener.getCollectedContainers());
    }


}

