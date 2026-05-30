package io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.impl;

import io.github.sparqlanything.fxbgp.stream.join.model.ModelException;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.TriplePatternSlotNumber;
import org.apache.jena.graph.Node;

import java.util.Properties;

public class TriplePatternSlotNumberImpl extends TriplePatternNodeImpl implements TriplePatternSlotNumber {

    private final Integer slotNumber;

    public TriplePatternSlotNumberImpl(Node node, Properties properties) {
        super(extractSurface(node), node, properties);
        if (node.isURI() && node.getURI().startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#_")) {
            this.slotNumber = Integer.parseInt(node.getURI().substring(44));
        } else {
            slotNumber = null;
        }
    }

    private static String extractSurface(Node node) {
        if (node.isURI()) {
            if (node.getURI().startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#_"))
                return node.getURI().substring(44);
            else
                throw new ModelException("Concrete Slot Numbers must start with http://www.w3.org/1999/02/22-rdf-syntax-ns#_");
        }
        return node.toString();
    }

    @Override
    public Integer getNumber() {
        return slotNumber;
    }
}
