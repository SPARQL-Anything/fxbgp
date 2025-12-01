package io.github.sparqlanything.fxbgp.stream;

import org.apache.jena.graph.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FXAbstractNodeEventListener implements FXNodeEventListener {
    private Logger L = LoggerFactory.getLogger(FXAbstractNodeEventListener.class);

    @Override
    public void startContainer(Node container) {
        L.info("startContainer: {}", container);
    }

    @Override
    public void onValue(Node value) {
        L.info("onValue: {}", value);
    }

    @Override
    public void onType(Node type) {
        L.info("onType: {}", type);
    }

    @Override
    public void onSlotNumber(Node predicate) {
        L.info("onSlotNumber: {}", predicate);
    }

    @Override
    public void onSlotString(Node predicate) {
        L.info("onSlotString: {}", predicate);
    }

    @Override
    public void onTypeRoot() {
        L.info("onTypeRoot");
    }

    @Override
    public void onTypeProperty() {
        L.info("onTypeProperty");
    }

    @Override
    public void endContainer() {
        L.info("endContainer");
    }

}
