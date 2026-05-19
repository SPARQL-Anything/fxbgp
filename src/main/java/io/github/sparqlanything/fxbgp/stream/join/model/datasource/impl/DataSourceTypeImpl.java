package io.github.sparqlanything.fxbgp.stream.join.model.datasource.impl;

import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceType;
import io.github.sparqlanything.model.Triplifier;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

import java.util.Properties;

public class DataSourceTypeImpl extends DataSourceFXElementImpl implements DataSourceType {

    public DataSourceTypeImpl(String surface, Properties properties) {
        super(surface, properties);
    }

    @Override
    public Node asNode() {
        return NodeFactory.createURI(Triplifier.XYZ_NS.concat(getSurface()));
    }

}
