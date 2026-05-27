package io.github.sparqlanything.fxbgp.joins;

import io.github.sparqlanything.fxbgp.stream.join.ContainerSelector;
import io.github.sparqlanything.fxbgp.stream.join.ContainerSelectorImpl;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceContainer;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceType;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.impl.DataSourceContainerImpl;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.impl.DataSourceTypeImpl;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.ContainerIsomorphism;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.TriplePatternContainer;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.TriplePatternType;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.impl.TriplePatternContainerImpl;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.impl.TriplePatternRootImpl;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.impl.TriplePatternTypeImpl;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.impl.TriplePatternTypePropertyImpl;
import io.github.sparqlanything.model.IRIArgument;
import io.github.sparqlanything.model.PropertyUtils;
import io.github.sparqlanything.model.Triplifier;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

public class TestModel {

    @Test
    public void testConcreteContainerMatch() {

        String root = "http://example.org/root/";
        String containerId = "abc";
        Properties properties = new Properties();
        properties.setProperty(IRIArgument.ROOT.toString(), root);
        properties.setProperty(IRIArgument.BLANK_NODES.toString(), "false");
        DataSourceContainer dataSourceContainer = new DataSourceContainerImpl(containerId, properties);

        // Test if ?c matches with a container with id "/abc"
        Var containerNode = Var.alloc("c");
        TriplePatternContainer triplePatternContainer = new TriplePatternContainerImpl(containerNode, properties);
        ContainerSelector containerSelector = new ContainerSelectorImpl(triplePatternContainer);
        Collection<ContainerIsomorphism> containerBinding = containerSelector.matches(dataSourceContainer);
        Assert.assertNotNull(containerBinding);

        Binding binding = containerBinding.iterator().next().asBinding();
        Assert.assertEquals(NodeFactory.createURI(root + containerId), binding.get(containerNode));
    }

    @Test
    public void testBNContainerMatch() {

        String root = "http://example.org/root/";
        String containerId = "abc";
        Properties properties = new Properties();
        properties.setProperty(IRIArgument.ROOT.toString(), root);
        properties.setProperty(IRIArgument.BLANK_NODES.toString(), "true");
        DataSourceContainer dataSourceContainer = new DataSourceContainerImpl(containerId, properties);

        Var containerNode = Var.alloc("c");
        TriplePatternContainer triplePatternContainer = new TriplePatternContainerImpl(containerNode, properties);
        ContainerSelector containerSelector = new ContainerSelectorImpl(triplePatternContainer);
        Collection<ContainerIsomorphism> containerBinding = containerSelector.matches(dataSourceContainer);
        Assert.assertNotNull(containerBinding);

        Binding binding = containerBinding.iterator().next().asBinding();
        Assert.assertEquals(binding.get(containerNode), NodeFactory.createBlankNode(containerId));
    }

    @Test
    public void testRoot() {
        // test match
        // ?c a FX:Root
        String root = "http://example.org/root";
        String containerId = "";
        Properties properties = new Properties();
        properties.setProperty(IRIArgument.ROOT.toString(), root);
        properties.setProperty(IRIArgument.BLANK_NODES.toString(), "false");
        DataSourceContainer dataSourceContainer = new DataSourceContainerImpl(containerId, properties, true);

        Var containerNode = Var.alloc("c");
        TriplePatternContainer triplePatternContainer = new TriplePatternContainerImpl(containerNode, properties);
        ContainerSelector containerSelector = new ContainerSelectorImpl(triplePatternContainer);
        containerSelector.setRootTriplePattern(new TriplePatternTypePropertyImpl(RDF.type.asNode(), properties), new TriplePatternRootImpl(NodeFactory.createURI(Triplifier.FACADE_X_TYPE_ROOT), properties));

        Collection<ContainerIsomorphism> containerBinding = containerSelector.matches(dataSourceContainer);
        Assert.assertNotNull(containerBinding);

        Binding binding = containerBinding.iterator().next().asBinding();
        Assert.assertEquals(NodeFactory.createURI(root), binding.get(containerNode));

    }


    @Test
    public void testRoot2() {
        // test match
        // ?c:Container ?p:TypeProperty fx:Root:FX:Root
        String root = "http://example.org/root";
        String containerId = "";
        Properties properties = new Properties();
        properties.setProperty(IRIArgument.ROOT.toString(), root);
        properties.setProperty(IRIArgument.BLANK_NODES.toString(), "false");
        DataSourceContainer dataSourceContainer = new DataSourceContainerImpl(containerId, properties, true);

        Var containerNode = Var.alloc("c");
        Var predicate = Var.alloc("p");
        TriplePatternContainer triplePatternContainer = new TriplePatternContainerImpl(containerNode, properties);
        ContainerSelector containerSelector = new ContainerSelectorImpl(triplePatternContainer);
        containerSelector.setRootTriplePattern(new TriplePatternTypePropertyImpl(predicate, properties), new TriplePatternRootImpl(NodeFactory.createURI(Triplifier.FACADE_X_TYPE_ROOT), properties));

        Collection<ContainerIsomorphism> containerBinding = containerSelector.matches(dataSourceContainer);
        Assert.assertNotNull(containerBinding);

        Binding binding = containerBinding.iterator().next().asBinding();
        Assert.assertEquals(NodeFactory.createURI(root), binding.get(containerNode));
        Assert.assertEquals(RDF.type.getURI(), binding.get(predicate).getURI());

    }

    @Test
    public void testRoot3() {
        // test match
        // ?c:Container ?p:TypeProperty ?o:FX:Root
        String root = "http://example.org/root";
        String containerId = "";
        Properties properties = new Properties();
        properties.setProperty(IRIArgument.ROOT.toString(), root);
        properties.setProperty(IRIArgument.BLANK_NODES.toString(), "false");
        DataSourceContainer dataSourceContainer = new DataSourceContainerImpl(containerId, properties, true);


        Var containerNode = Var.alloc("c");
        Var predicate = Var.alloc("p");
        Var o = Var.alloc("o");
        TriplePatternContainer triplePatternContainer = new TriplePatternContainerImpl(containerNode, properties);
        ContainerSelector containerSelector = new ContainerSelectorImpl(triplePatternContainer);
        containerSelector.setRootTriplePattern(new TriplePatternTypePropertyImpl(predicate, properties), new TriplePatternRootImpl(o, properties));

        Collection<ContainerIsomorphism> containerBinding = containerSelector.matches(dataSourceContainer);
        Assert.assertNotNull(containerBinding);

        Binding binding = containerBinding.iterator().next().asBinding();
        Assert.assertEquals(NodeFactory.createURI(root), binding.get(containerNode));
        Assert.assertEquals(RDF.type.getURI(), binding.get(predicate).getURI());
        Assert.assertEquals(Triplifier.FACADE_X_TYPE_ROOT, binding.get(o).getURI());

    }


    @Test
    public void testContainsForTypes() {
        Properties properties = new Properties();
        String ns = PropertyUtils.getStringProperty(properties, IRIArgument.NAMESPACE);
        HashSet<DataSourceType> dataSourceTypes = new HashSet<>();
        DataSourceTypeImpl t1 = new DataSourceTypeImpl("type1", properties);
        DataSourceTypeImpl t2 = new DataSourceTypeImpl("type2", properties);
        dataSourceTypes.add(t1);
        dataSourceTypes.add(t2);
        Assert.assertTrue(dataSourceTypes.contains(new TriplePatternTypeImpl(NodeFactory.createURI(ns + "type1"), properties)));
    }

    @Test
    public void testEquals() {
        Properties properties = new Properties();
        properties.setProperty(IRIArgument.LOCATION.toString(), "/path/to/location/");
        properties.setProperty(IRIArgument.BLANK_NODES.toString(), "false");
        TriplePatternContainerImpl triplePatternContainerVar = new TriplePatternContainerImpl(Var.alloc("c"), properties);
        DataSourceContainer dataSourceContainer = new DataSourceContainerImpl("abc", properties);
        Assert.assertTrue(triplePatternContainerVar.matches(dataSourceContainer));
        Assert.assertTrue(dataSourceContainer.matches(triplePatternContainerVar));

        TriplePatternContainerImpl triplePatternContainerConcrete = new TriplePatternContainerImpl(NodeFactory.createURI("file:///path/to/locationabc"), properties);
        Assert.assertTrue(triplePatternContainerConcrete.matches(dataSourceContainer));
        Assert.assertTrue(dataSourceContainer.matches(triplePatternContainerConcrete));

        triplePatternContainerConcrete = new TriplePatternContainerImpl(NodeFactory.createURI("file:///path/to/locationabc2"), properties);
        Assert.assertFalse(triplePatternContainerConcrete.matches(dataSourceContainer));
        Assert.assertFalse(dataSourceContainer.matches(triplePatternContainerConcrete));

        TriplePatternType triplePatternType = new TriplePatternTypeImpl(NodeFactory.createURI("file:///path/to/locationabc"), properties);
        Assert.assertFalse(triplePatternType.matches(dataSourceContainer));
        Assert.assertFalse(dataSourceContainer.matches(triplePatternType));

    }
}
