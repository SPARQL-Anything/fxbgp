package io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.impl;

import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.TriplePatternType;
import io.github.sparqlanything.model.IRIArgument;
import io.github.sparqlanything.model.PropertyUtils;
import org.apache.jena.graph.Node;

import java.util.Properties;

public class TriplePatternTypeImpl extends TriplePatternNodeImpl implements TriplePatternType {

    public TriplePatternTypeImpl(Node node, Properties properties) {
        super(extractSurface(node, properties), node, properties);
    }

    private static String extractSurface(Node node, Properties properties) {
        if (node.isURI()) {
            return node.getURI().replace(PropertyUtils.getStringProperty(properties, IRIArgument.NAMESPACE), "");
        }
        return node.toString();
    }
}
