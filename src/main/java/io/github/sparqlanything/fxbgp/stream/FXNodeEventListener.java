package io.github.sparqlanything.fxbgp.stream;

import org.apache.jena.graph.Node;

public interface FXNodeEventListener {

    public void startContainer(Node container);

    public void onSlotNumber(Node predicate);

    public void onSlotString(Node predicate);

    public void onValue(Node value);

    public void onTypeProperty();

    public void onTypeRoot();

    public void onType(Node type);

    public void endContainer();

}
