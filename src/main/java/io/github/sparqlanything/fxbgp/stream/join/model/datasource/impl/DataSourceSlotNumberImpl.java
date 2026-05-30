package io.github.sparqlanything.fxbgp.stream.join.model.datasource.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.vocabulary.RDF;

import java.util.Properties;

public class DataSourceSlotNumberImpl extends DataSourceFXElementImpl {

    private final int slotNumber;

    public DataSourceSlotNumberImpl(Integer slotNumber, Properties properties) {
        super(slotNumber.toString(), properties);
        this.slotNumber = slotNumber;
    }

    @Override
    public Node asNode() {
        return RDF.li(slotNumber).asNode();
    }
}
