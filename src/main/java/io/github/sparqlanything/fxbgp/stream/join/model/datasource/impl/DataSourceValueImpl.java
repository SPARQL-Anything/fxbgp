package io.github.sparqlanything.fxbgp.stream.join.model.datasource.impl;

import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceValue;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

import java.util.Properties;

public class DataSourceValueImpl extends DataSourceFXElementImpl implements DataSourceValue {

    private final Node node;

    public DataSourceValueImpl(Object value, Properties properties) {
        super(value.toString(), properties);
        this.node = NodeFactory.createLiteralByValue(value);
    }

    @Override
    public Node asNode() {
        return node;
    }
}
