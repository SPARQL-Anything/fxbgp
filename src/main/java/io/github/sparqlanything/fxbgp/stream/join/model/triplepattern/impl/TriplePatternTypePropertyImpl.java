package io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.impl;

import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.TriplePatternTypeProperty;
import org.apache.jena.graph.Node;
import org.apache.jena.vocabulary.RDF;

import java.util.Properties;

public class TriplePatternTypePropertyImpl extends TriplePatternNodeImpl implements TriplePatternTypeProperty {

    public TriplePatternTypePropertyImpl(Node node, Properties properties) {
        super(extractSurface(node, properties), node, properties);
    }

    private static String extractSurface(Node node, Properties properties) {
        if (node.isURI()) {
            return RDF.type.getURI();
        }
        return node.toString();
    }
}
