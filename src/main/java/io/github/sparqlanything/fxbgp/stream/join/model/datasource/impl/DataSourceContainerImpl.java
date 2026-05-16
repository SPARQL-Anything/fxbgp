package io.github.sparqlanything.fxbgp.stream.join.model.datasource.impl;

import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceContainer;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceSlot;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceSlotFiller;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceType;
import io.github.sparqlanything.model.IRIArgument;
import io.github.sparqlanything.model.PropertyUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

import java.util.*;

public class DataSourceContainerImpl extends DataSourceFXElementImpl implements DataSourceContainer {

    private final Map<DataSourceSlot, DataSourceSlotFiller> slotMap = new HashMap<>();
    private final Set<DataSourceType> types = new HashSet<>();
    private boolean isRoot;
    private final String root;
    private final boolean useBlankNodes;

    public DataSourceContainerImpl(String id, Properties properties) {
        this(id, properties, false, PropertyUtils.getStringProperty(properties, IRIArgument.ROOT));
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

    public Map<DataSourceSlot, DataSourceSlotFiller> getSlotMap() {
        return slotMap;
    }

    @Override
    public void addSlot(DataSourceSlot slot, DataSourceSlotFiller dataSourceSlotFiller) {
        Objects.requireNonNull(slot);
        Objects.requireNonNull(dataSourceSlotFiller);
        slotMap.put(slot, dataSourceSlotFiller);
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

    @Override
    public Node asRDFNode() {

        if (useBlankNodes)
            return NodeFactory.createBlankNode(getSurface());

        return NodeFactory.createURI(root.concat(getSurface()));
    }


}
