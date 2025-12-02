package io.github.sparqlanything.fxbgp.stream;

import org.apache.jena.graph.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FXAbstractNodeEventListener implements FXNodeEventListener {
    private Logger L = LoggerFactory.getLogger(FXAbstractNodeEventListener.class);

    @Override
    public void startContainer(Node container) {
        L.trace("startContainer: {}", container);
    }

    @Override
    public void onValue(Node value) {
        L.trace("onValue: {}", value);
    }

    @Override
    public void onType(Node type) {
        L.trace("onType: {}", type);
    }

    @Override
    public void onSlotNumber(Node predicate) {
        L.trace("onSlotNumber: {}", predicate);
    }

    @Override
    public void onSlotString(Node predicate) {
        L.trace("onSlotString: {}", predicate);
    }

    @Override
    public void onTypeRoot() {
        L.trace("onTypeRoot");
    }

    @Override
    public void onTypeProperty() {
        L.trace("onTypeProperty");
    }

    @Override
    public void endContainer() {
        L.trace("endContainer");
    }

}
