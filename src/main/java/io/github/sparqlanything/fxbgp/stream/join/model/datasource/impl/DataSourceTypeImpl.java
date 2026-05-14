package io.github.sparqlanything.fxbgp.stream.join.model.datasource.impl;

import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceType;
import org.apache.jena.graph.Node;

import java.util.Properties;

public class DataSourceTypeImpl extends DataSourceFXElementImpl implements DataSourceType {

    public DataSourceTypeImpl(String surface, Properties properties) {
        super(surface, properties);
    }

    @Override
    public Node asRDFNode() {
        throw new RuntimeException();
    }

}
