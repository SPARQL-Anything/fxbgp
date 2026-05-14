package io.github.sparqlanything.fxbgp.stream.join.model.datasource.impl;

import io.github.sparqlanything.fxbgp.stream.join.model.FXElementImpl;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceFXElement;
import java.util.Properties;

public abstract class DataSourceFXElementImpl extends FXElementImpl implements DataSourceFXElement {

    public DataSourceFXElementImpl(String surface, Properties properties){
        super(surface, properties);
    }
}
