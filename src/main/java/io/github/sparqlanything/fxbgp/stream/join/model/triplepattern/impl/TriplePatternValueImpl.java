package io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.impl;

import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.TriplePatternValue;
import org.apache.jena.graph.Node;

import java.util.Properties;

public class TriplePatternValueImpl extends TriplePatternNodeImpl implements TriplePatternValue {

    public TriplePatternValueImpl(Node node, Properties properties) {
        super(node, properties);
        node.sameTermAs(node);
    }



}
