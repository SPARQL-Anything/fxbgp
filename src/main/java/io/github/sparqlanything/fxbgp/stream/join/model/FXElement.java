package io.github.sparqlanything.fxbgp.stream.join.model;

import org.apache.jena.graph.Node;

public interface FXElement {
    public String getSurface();
    public Node asNode();
    public boolean matches(FXElement o);
}
