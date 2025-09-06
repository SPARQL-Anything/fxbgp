package io.github.sparqlanything.fxbgp.stream;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import java.util.Iterator;

public class FXQuerySolution implements QuerySolution {
    @Override
    public RDFNode get(String s) {
        return null;
    }

    @Override
    public Resource getResource(String s) {
        return null;
    }

    @Override
    public Literal getLiteral(String s) {
        return null;
    }

    @Override
    public boolean contains(String s) {
        return false;
    }

    @Override
    public Iterator<String> varNames() {
        return null;
    }
}
