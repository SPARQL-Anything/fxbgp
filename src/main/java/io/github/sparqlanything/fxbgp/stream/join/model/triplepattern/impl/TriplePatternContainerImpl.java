package io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.impl;

import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceContainer;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.ContainerBinding;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.TriplePatternContainer;
import io.github.sparqlanything.model.Triplifier;
import org.apache.jena.graph.Node;

import java.util.Properties;

public class TriplePatternContainerImpl extends TriplePatternNodeImpl implements TriplePatternContainer {

    // if the container node is a URI the container id is the id in the data source of the target container

    public TriplePatternContainerImpl(Node node, Properties properties) {
        super(extractSurface(node, properties), node, properties);
    }

    @Override
    public boolean matchId(DataSourceContainer dataSourceContainer, ContainerBinding containerBinding) {

        if (super.getBGPNode().isVariable() || dataSourceContainer.getId().matches(surface)) {
            containerBinding.set(this, dataSourceContainer);
            return true;
        }
        return false;

    }

    private static String extractSurface(Node node, Properties properties) {
        if (node.isURI()) {
            return node.getURI().replace(Triplifier.getRootArgument(properties), "");
        }
        return node.toString();
    }
}
