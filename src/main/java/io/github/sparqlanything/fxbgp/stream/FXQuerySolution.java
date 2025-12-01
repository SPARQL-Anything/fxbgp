package io.github.sparqlanything.fxbgp.stream;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

public class FXQuerySolution implements QuerySolution {
    @Override
    public RDFNode get(String s) {
        return map.get(s);
    }

    @Override
    public Resource getResource(String s) {
        return (Resource)  map.get(s);
    }

    @Override
    public Literal getLiteral(String s) {
        return (Literal)  map.get(s);
    }

    @Override
    public boolean contains(String s) {
        return map.containsKey(s);
    }

    @Override
    public Iterator<String> varNames() {
        return map.keySet().iterator();
    }

    private Map<String, RDFNode> map;
    private FXQuerySolution(Map<String, RDFNode> map) {
        this.map = Collections.unmodifiableMap(map);
    }
    public static final QuerySolution make(Map<String, RDFNode> map){
        return new FXQuerySolution(map);
    }
}
