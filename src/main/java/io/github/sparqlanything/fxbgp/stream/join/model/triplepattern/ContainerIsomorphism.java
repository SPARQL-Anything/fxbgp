package io.github.sparqlanything.fxbgp.stream.join.model.triplepattern;

import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceFXElement;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.binding.Binding;

import java.util.Map;

public interface ContainerIsomorphism {

    public void set(TriplePatternNode triplePatternNode, DataSourceFXElement dataSourceFXElement);
    public DataSourceFXElement get (TriplePatternNode triplePatternNode);
    public Binding asBinding();
    public ContainerIsomorphism copy();
    public Map<Node, Node> asMap();

}
