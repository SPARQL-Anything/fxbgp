package io.github.sparqlanything.fxbgp.stream.join.model;

import org.apache.jena.graph.Node;

import java.util.Properties;

public interface FXElement {
    public String getSurface();
    public Node asNode();
    public boolean matches(FXElement o);
    public int compareTo(FXElement e);
    public Properties getProperties();
}
