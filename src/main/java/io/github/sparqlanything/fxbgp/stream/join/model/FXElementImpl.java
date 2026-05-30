package io.github.sparqlanything.fxbgp.stream.join.model;

import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceSlotNumber;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceSlotString;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.impl.DataSourceContainerImpl;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.impl.DataSourceTypeImpl;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.TriplePatternSlotNumber;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.TriplePatternSlotString;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.impl.TriplePatternContainerImpl;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.impl.TriplePatternTypeImpl;
import org.apache.jena.sparql.core.Var;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public abstract class FXElementImpl implements FXElement {


    // DataSource elements:
    // - For containers: the surface is the container id.
    // - For types: the surface is the type local name
    // Triple pattern elements:
    // - For non URIs is the node.toString()
    // - For URIs is: local-id for types and containers; rdf:type fx:Root for type property and FXRoot
    protected final String surface;
    protected final Properties properties;

    public FXElementImpl(String surface, Properties properties) {
        this.surface = surface;
        this.properties = properties;
    }

    private static void addMatchingClasses(Class<?> c1, Class<?> c2) {
        matchingClass.put(c1, c2);
        matchingClass.put(c2, c1);

    }

    private static final Map<Class<?>, Class<?>> matchingClass = new HashMap<>();

    static {
        addMatchingClasses(TriplePatternTypeImpl.class, DataSourceTypeImpl.class);
        addMatchingClasses(TriplePatternContainerImpl.class, DataSourceContainerImpl.class);
        addMatchingClasses(TriplePatternSlotNumber.class, DataSourceSlotNumber.class);
        addMatchingClasses(TriplePatternSlotString.class, DataSourceSlotString.class);
    }

    @Override
    public boolean matches(FXElement fxElement) {
        // if same class or matching class
        if (!sameClass(fxElement) && !isMatchingClass(fxElement)) {
            return false;
        }

        if (fxElement.asNode() instanceof Var v1 || this.asNode() instanceof Var) {
            return true;
        }

        return asNode().equals(fxElement.asNode());
    }

    private boolean sameClass(Object o) {
        return o.getClass() == this.getClass();
    }

    private boolean isMatchingClass(Object o) {
        return o.getClass() == matchingClass.get(this.getClass());
    }

    @Override
    public int hashCode() {
        return getSurface().hashCode();
    }

    @Override
    public String getSurface() {
        return surface;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FXElement fxElement && matches(fxElement);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "surface='" + surface + '\'' +
                " node='" + asNode() + '\'' +
                '}';
    }

    @Override
    public int compareTo(FXElement e) {
        return e.getSurface().compareTo(getSurface());
    }

    public Properties getProperties() {
        return properties;
    }
}
