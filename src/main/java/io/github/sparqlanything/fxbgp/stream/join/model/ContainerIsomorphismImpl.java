package io.github.sparqlanything.fxbgp.stream.join.model;

import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceFXElement;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.TriplePatternNode;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ContainerIsomorphismImpl implements ContainerIsomorphism {

    private final Map<TriplePatternNode, DataSourceFXElement> bindings = new HashMap<>();

    public boolean set(TriplePatternNode triplePatternNode, DataSourceFXElement dataSourceFXElement) {
        Objects.requireNonNull(triplePatternNode);
        Objects.requireNonNull(dataSourceFXElement);
        DataSourceFXElement element = bindings.get(triplePatternNode);
        if (element == null) {
            bindings.put(triplePatternNode, dataSourceFXElement);
            return true;
        }
        return element.equals(dataSourceFXElement);
    }

    public void putAll(Map<TriplePatternNode, DataSourceFXElement> bindings) {
        Objects.requireNonNull(bindings);
        this.bindings.putAll(bindings);
    }

    public DataSourceFXElement get(TriplePatternNode triplePatternNode) {
        return bindings.get(triplePatternNode);
    }

    @Override
    public Binding asBinding() {
        BindingBuilder bindingBuilder = Binding.builder();
        bindings.forEach((tpn, dse) -> {
            if (tpn.asNode() instanceof Var var) {
                bindingBuilder.add(var, dse.asNode());
            }
        });
        return bindingBuilder.build();
    }

    public ContainerIsomorphism copy() {
        ContainerIsomorphismImpl result = new ContainerIsomorphismImpl();
        result.bindings.putAll(this.bindings);
        return result;
    }

    @Override
    public Map<Node, Node> asMap() {
        Map<Node, Node> result = new HashMap<>();
        bindings.forEach((k, v) -> result.put(k.asNode(), v.asNode()));
        return result;
    }

    @Override
    public String toString() {
        return "ContainerBindingImpl{" +
                "bindings=" + bindings +
                '}';
    }
}
