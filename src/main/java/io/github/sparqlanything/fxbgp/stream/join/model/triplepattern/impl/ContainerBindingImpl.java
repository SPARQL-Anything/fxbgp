package io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.impl;

import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceFXElement;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.ContainerBinding;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.TriplePatternNode;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;

import java.util.HashMap;
import java.util.Map;

public class ContainerBindingImpl implements ContainerBinding {

    private final Map<TriplePatternNode, DataSourceFXElement> bindings = new HashMap<>();

    public void set(TriplePatternNode triplePatternNode, DataSourceFXElement dataSourceFXElement) {
        bindings.put(triplePatternNode, dataSourceFXElement);
    }

    public DataSourceFXElement get(TriplePatternNode triplePatternNode) {
        return bindings.get(triplePatternNode);
    }

    @Override
    public Binding asSPARQLBinding() {
        BindingBuilder bindingBuilder = Binding.builder();
        bindings.forEach((tpn, dse) -> {
            if (tpn.asNode() instanceof Var var) {
                bindingBuilder.add(var, dse.asNode());
            }
        });
        return bindingBuilder.build();
    }

    public ContainerBinding copy() {
        ContainerBindingImpl result = new ContainerBindingImpl();
        result.bindings.putAll(this.bindings);
        return result;
    }
}
