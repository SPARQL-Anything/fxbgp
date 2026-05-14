package io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.impl;

import io.github.sparqlanything.fxbgp.stream.join.model.FXElementImpl;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.TriplePatternNode;
import org.apache.jena.graph.Node;

import java.util.Properties;

public abstract class TriplePatternNodeImpl extends FXElementImpl implements TriplePatternNode {

    protected final Node node;

    public TriplePatternNodeImpl(String surface, Node node, Properties properties) {
        super(surface, properties);
        this.node = node;
    }

    @Override
    public Node getBGPNode() {
        return node;
    }

}


