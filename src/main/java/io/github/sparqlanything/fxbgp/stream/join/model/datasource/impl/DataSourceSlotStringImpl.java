package io.github.sparqlanything.fxbgp.stream.join.model.datasource.impl;

import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceSlotNumber;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceSlotString;
import io.github.sparqlanything.model.IRIArgument;
import io.github.sparqlanything.model.PropertyUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

import java.util.Properties;

public class DataSourceSlotStringImpl extends DataSourceFXElementImpl implements DataSourceSlotString {


    public DataSourceSlotStringImpl(String slotName, Properties properties) {
        super(slotName, properties);
    }

    @Override
    public Node asNode() {
        return NodeFactory.createURI(PropertyUtils.getStringProperty(properties, IRIArgument.NAMESPACE).concat(getSurface()));
    }
}
