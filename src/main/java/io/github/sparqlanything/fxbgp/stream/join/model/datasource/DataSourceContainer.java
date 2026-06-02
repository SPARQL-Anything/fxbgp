package io.github.sparqlanything.fxbgp.stream.join.model.datasource;

import java.util.Map;
import java.util.Set;

public interface DataSourceContainer extends DataSourceValueOrContainer {


    public Map<Integer, DataSourceValueOrContainer> getSlotNumberValues();

    public Map<DataSourceSlotString, DataSourceValueOrContainer> getSlotString();

    public void addSlot(DataSourceSlotNumber slot, DataSourceValueOrContainer dataSourceSlotFiller);

    public void addSlot(DataSourceSlotString slot, DataSourceValueOrContainer dataSourceSlotFiller);

    public Set<DataSourceType> getTypes();

    public String getId();

    public boolean isRoot();

    public void setRoot(boolean root);

    public void addType(DataSourceType type);

    public Map<Integer, DataSourceSlotNumber> getSlotNumbers();
}
