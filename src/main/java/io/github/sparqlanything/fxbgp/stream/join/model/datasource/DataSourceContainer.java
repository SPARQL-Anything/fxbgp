package io.github.sparqlanything.fxbgp.stream.join.model.datasource;

import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.TriplePatternType;

import java.util.Map;
import java.util.Set;

public interface DataSourceContainer extends DataSourceFXElement{

    public Map<DataSourceSlot, DataSourceSlotFiller> getSlotMap() ;
    public void addSlot(DataSourceSlot slot, DataSourceSlotFiller dataSourceSlotFiller) ;
    public Set<DataSourceType> getTypes() ;
    public String getId() ;
    public boolean isRoot() ;
    public void setRoot(boolean root) ;
    public void addType(DataSourceType type) ;
}
