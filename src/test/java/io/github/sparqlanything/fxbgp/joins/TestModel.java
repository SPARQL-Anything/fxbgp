package io.github.sparqlanything.fxbgp.joins;

import io.github.sparqlanything.fxbgp.stream.join.ContainerSelectorImpl;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceContainer;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceType;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.impl.DataSourceContainerImpl;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.impl.DataSourceTypeImpl;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.ContainerBinding;
import io.github.sparqlanything.fxbgp.stream.join.ContainerSelector;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.TriplePatternContainer;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.impl.*;
import io.github.sparqlanything.model.IRIArgument;
import io.github.sparqlanything.model.PropertyUtils;
import io.github.sparqlanything.model.Triplifier;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Properties;

public class TestModel {

    String root = "https://example.org/root";
    String containerId = "/abc";

    Properties properties = new Properties();

    @Test
    public void testConcreteContainerMatch() {

        DataSourceContainer dataSourceContainer = getDataSourceContainer(properties, false, false);

        // Test if ?c matches with a container with id "/abc"
        Var containerNode = Var.alloc("c");
        TriplePatternContainer triplePatternContainer = new TriplePatternContainerImpl(containerNode, properties);
        ContainerSelector containerSelector = new ContainerSelectorImpl(triplePatternContainer);
        ContainerBinding containerBinding = containerSelector.matches(dataSourceContainer);
        Assert.assertNotNull(containerBinding);

        Binding binding = containerBinding.asSPARQLBinding();
        Assert.assertEquals(binding.get(containerNode), NodeFactory.createURI(root + containerId));
    }

    @Test
    public void testBNContainerMatch() {

        DataSourceContainer dataSourceContainer = getDataSourceContainer(properties, false, true);

        Var containerNode = Var.alloc("c");
        TriplePatternContainer triplePatternContainer = new TriplePatternContainerImpl(containerNode, properties);
        ContainerSelector containerSelector = new ContainerSelectorImpl(triplePatternContainer);
        ContainerBinding containerBinding = containerSelector.matches(dataSourceContainer);
        Assert.assertNotNull(containerBinding);

        Binding binding = containerBinding.asSPARQLBinding();
        Assert.assertEquals(binding.get(containerNode), NodeFactory.createBlankNode(containerId));
    }

    @Test
    public void testRoot() {
        // test match
        // ?c a FX:Root
        DataSourceContainer dataSourceContainer = getDataSourceContainer(properties, true, false);

        Var containerNode = Var.alloc("c");
        TriplePatternContainer triplePatternContainer = new TriplePatternContainerImpl(containerNode, properties);
        ContainerSelector containerSelector = new ContainerSelectorImpl(triplePatternContainer);
        containerSelector.setRootTriplePattern(new TriplePatternTypePropertyImpl(RDF.type.asNode(), properties), new TriplePatternRootImpl(NodeFactory.createURI(Triplifier.FACADE_X_TYPE_ROOT), properties));

        ContainerBinding containerBinding = containerSelector.matches(dataSourceContainer);
        Assert.assertNotNull(containerBinding);

        Binding binding = containerBinding.asSPARQLBinding();
        Assert.assertEquals(NodeFactory.createURI(root), binding.get(containerNode));

    }


    @Test
    public void testRoot2() {
        // test match
        // ?c:Container ?p:TypeProperty fx:Root:FX:Root
        DataSourceContainer dataSourceContainer = getDataSourceContainer(properties, true, false);

        Var containerNode = Var.alloc("c");
        Var predicate = Var.alloc("p");
        TriplePatternContainer triplePatternContainer = new TriplePatternContainerImpl(containerNode, properties);
        ContainerSelector containerSelector = new ContainerSelectorImpl(triplePatternContainer);
        containerSelector.setRootTriplePattern(new TriplePatternTypePropertyImpl(predicate, properties), new TriplePatternRootImpl(NodeFactory.createURI(Triplifier.FACADE_X_TYPE_ROOT), properties));

        ContainerBinding containerBinding = containerSelector.matches(dataSourceContainer);
        Assert.assertNotNull(containerBinding);

        Binding binding = containerBinding.asSPARQLBinding();
        Assert.assertEquals(NodeFactory.createURI(root), binding.get(containerNode));
        Assert.assertEquals(RDF.type.getURI(), binding.get(predicate).getURI());

    }

    @Test
    public void testRoot3() {
        // test match
        // ?c:Container ?p:TypeProperty ?o:FX:Root
        DataSourceContainer dataSourceContainer = getDataSourceContainer(properties, true, false);

        Var containerNode = Var.alloc("c");
        Var predicate = Var.alloc("p");
        Var o = Var.alloc("o");
        TriplePatternContainer triplePatternContainer = new TriplePatternContainerImpl(containerNode, properties);
        ContainerSelector containerSelector = new ContainerSelectorImpl(triplePatternContainer);
        containerSelector.setRootTriplePattern(new TriplePatternTypePropertyImpl(predicate, properties), new TriplePatternRootImpl(o, properties));

        ContainerBinding containerBinding = containerSelector.matches(dataSourceContainer);
        Assert.assertNotNull(containerBinding);

        Binding binding = containerBinding.asSPARQLBinding();
        Assert.assertEquals(NodeFactory.createURI(root), binding.get(containerNode));
        Assert.assertEquals(RDF.type.getURI(), binding.get(predicate).getURI());
        Assert.assertEquals(Triplifier.FACADE_X_TYPE_ROOT, binding.get(o).getURI());

    }

    private DataSourceContainer getDataSourceContainer(Properties properties, boolean isRoot, boolean isBN) {
        String containerId = isRoot ? "" : this.containerId;
        // return new DataSourceContainerImpl(isBN, root, properties, isRoot, containerId);
        return new DataSourceContainerImpl(containerId,  properties, isRoot, containerId);
    }


    @Test
    public void testContainsForTypes() {
        String ns = PropertyUtils.getStringProperty(properties, IRIArgument.NAMESPACE);
        HashSet<DataSourceType> dataSourceTypes = new HashSet<>();
        DataSourceTypeImpl t1 = new DataSourceTypeImpl("type1", properties);
        DataSourceTypeImpl t2 = new DataSourceTypeImpl("type2", properties);
        dataSourceTypes.add(t1);
        dataSourceTypes.add(t2);
        Assert.assertTrue(dataSourceTypes.contains(new TriplePatternTypeImpl(NodeFactory.createURI(ns + "type1"), properties)));
    }
}
