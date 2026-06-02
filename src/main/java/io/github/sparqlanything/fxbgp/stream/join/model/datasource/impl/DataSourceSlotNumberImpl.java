package io.github.sparqlanything.fxbgp.stream.join.model.datasource.impl;

import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceSlotNumber;
import org.apache.jena.graph.Node;
import org.apache.jena.vocabulary.RDF;

import java.util.Properties;

public class DataSourceSlotNumberImpl extends DataSourceFXElementImpl implements DataSourceSlotNumber {

    private final int slotNumber;

    public DataSourceSlotNumberImpl(Integer slotNumber, Properties properties) {
        super(slotNumber.toString(), properties);
        this.slotNumber = slotNumber;
    }

    @Override
    public Node asNode() {
        return RDF.li(slotNumber).asNode();
    }

    public Integer getNumber(){
        return slotNumber;
    }
}
