package io.github.sparqlanything.fxbgp.stream.join.model.triplepattern;

import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceContainer;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceFXElement;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceSlot;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.impl.TriplePatternContainerImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.binding.Binding;

public interface ContainerBinding {

    public void set(TriplePatternNode triplePatternNode, DataSourceFXElement dataSourceFXElement);
    public DataSourceFXElement get (TriplePatternNode triplePatternNode);
    public Binding asSPARQLBinding();
    public ContainerBinding copy();
}
