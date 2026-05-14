package io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.impl;

import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.TriplePatternRoot;
import io.github.sparqlanything.model.Triplifier;
import org.apache.jena.graph.Node;

import java.util.Properties;

public class TriplePatternRootImpl extends TriplePatternNodeImpl implements TriplePatternRoot {
    public TriplePatternRootImpl(Node node, Properties properties) {
        super(extractSurface(node, properties), node, properties);
    }

    private static String extractSurface(Node node, Properties properties) {
        if (node.isURI()) {
            return Triplifier.FACADE_X_TYPE_ROOT;
        }
        return node.toString();
    }
}
