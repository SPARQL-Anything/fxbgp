package io.github.sparqlanything.fxbgp.stream;

import org.apache.jena.graph.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;

public class FXProxyEventListener implements FXNodeEventListener{
    private static final Logger L = LoggerFactory.getLogger(FXProxyEventListener.class);
    private Set<? extends FXNodeEventListener> listeners;
    private FXProxyEventListener(Set<? extends FXNodeEventListener> listeners) {
        this.listeners = Collections.unmodifiableSet(listeners);
    }

    public final static FXNodeEventListener make(Set<? extends FXNodeEventListener> listeners) {
        return new FXProxyEventListener(listeners);
    }

    @Override
    public void startContainer(Node container) {
        L.trace("startContainer {}", container);
        for (FXNodeEventListener listener : listeners) {
            listener.startContainer(container);
        }
    }

    @Override
    public void onSlotNumber(Node n){
        L.trace("onSlotNumber {}", n);
        for (FXNodeEventListener listener : listeners) {
            listener.onSlotNumber(n);
        }
    }

    @Override
    public void onSlotString(Node n){
        L.trace("onSlotString {}", n);
        for (FXNodeEventListener listener : listeners) {
            listener.onSlotString(n);
        }
    }

    @Override
    public void onValue(Node value) {
        L.trace("onValue {}", value);
        for (FXNodeEventListener listener : listeners) {
            listener.onValue(value);
        }
    }

    @Override
    public void onTypeProperty() {
        L.trace("onTypeProperty");
        for (FXNodeEventListener listener : listeners) {
            listener.onTypeProperty();
        }
    }

    @Override
    public void onTypeRoot() {
        L.trace("onTypeRoot");
        for (FXNodeEventListener listener : listeners) {
            listener.onTypeRoot();
        }
    }

    @Override
    public void onType(Node node) {
        L.trace("onType {}", node);
        for (FXNodeEventListener listener : listeners) {
            listener.onType(node);
        }
    }

    @Override
    public void endContainer() {
        L.trace("endContainer");
        for(FXNodeEventListener listener : listeners) {
            listener.endContainer();
        }
    }
}
