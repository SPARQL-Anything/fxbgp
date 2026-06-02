package io.github.sparqlanything.fxbgp.stream.join.model.datasource.impl;

import io.github.sparqlanything.fxbgp.stream.join.model.datasource.*;
import io.github.sparqlanything.model.IRIArgument;
import io.github.sparqlanything.model.PropertyUtils;
import io.github.sparqlanything.model.Triplifier;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

import java.util.*;

public class DataSourceContainerImpl extends DataSourceFXElementImpl implements DataSourceContainer {

    private final Map<Integer, DataSourceValueOrContainer> slotNumberValues = new HashMap<>();
    private final Map<Integer, DataSourceSlotNumber> slotNumbers = new HashMap<>();
    private final Map<DataSourceSlotString, DataSourceValueOrContainer> slotString = new HashMap<>();
    private final Set<DataSourceType> types = new HashSet<>();
    private boolean isRoot;
    private final String root;
    private final boolean useBlankNodes;

    public DataSourceContainerImpl(String id, Properties properties) {
        this(id, properties, false, Triplifier.getRootArgument(properties));
    }

    public DataSourceContainerImpl(String id, Properties properties, boolean isRoot) {
        this(id, properties, isRoot, PropertyUtils.getStringProperty(properties, IRIArgument.ROOT));

    }

    public DataSourceContainerImpl(String id, Properties properties, boolean isRoot, String root) {
        super(id, properties);
        this.useBlankNodes = PropertyUtils.getBooleanProperty(properties, IRIArgument.BLANK_NODES);
        this.root = root;
        this.isRoot = isRoot;
    }

    public Map<Integer, DataSourceValueOrContainer> getSlotNumberValues() {
        return slotNumberValues;
    }

    public Map<DataSourceSlotString, DataSourceValueOrContainer> getSlotString() {
        return slotString;
    }

    @Override
    public void addSlot(DataSourceSlotNumber slot, DataSourceValueOrContainer dataSourceSlotFiller) {
        Objects.requireNonNull(slot);
        Objects.requireNonNull(dataSourceSlotFiller);
        slotNumberValues.put(slot.getNumber(), dataSourceSlotFiller);
        slotNumbers.put(slot.getNumber(), slot);
    }

    @Override
    public void addSlot(DataSourceSlotString slot, DataSourceValueOrContainer dataSourceSlotFiller) {
        Objects.requireNonNull(slot);
        Objects.requireNonNull(dataSourceSlotFiller);
        slotString.put(slot, dataSourceSlotFiller);
    }


    public Set<DataSourceType> getTypes() {
        return types;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public void setRoot(boolean root) {
        isRoot = root;
    }

    public void addType(DataSourceType type) {
        Objects.requireNonNull(type);
        types.add(type);
    }

    public String getId() {
        return super.getSurface();
    }

    public Node asNode() {

        if (useBlankNodes)
            return NodeFactory.createBlankNode(getSurface());

        return NodeFactory.createURI(root.concat(getSurface()));
    }

    public Map<Integer, DataSourceSlotNumber> getSlotNumbers() {
        return slotNumbers;
    }
}
