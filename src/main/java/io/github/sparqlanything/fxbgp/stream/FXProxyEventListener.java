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
        L.trace("[start] startContainer {}", container);
        for (FXNodeEventListener listener : listeners) {
            listener.startContainer(container);
        }
        L.trace("[end] startContainer {}", container);
    }

    @Override
    public void onSlotNumber(Node n){
        L.trace("[start] onSlotNumber {}", n);
        for (FXNodeEventListener listener : listeners) {
            listener.onSlotNumber(n);
        }
        L.trace("[end] onSlotNumber {}", n);
    }

    @Override
    public void onSlotString(Node n){
        L.trace("[start] onSlotString {}", n);
        for (FXNodeEventListener listener : listeners) {
            listener.onSlotString(n);
        }
        L.trace("[end] onSlotString {}", n);
    }

    @Override
    public void onValue(Node value) {
        L.trace("[start] onValue {}", value);
        for (FXNodeEventListener listener : listeners) {
            listener.onValue(value);
        }
        L.trace("[end] onValue {}", value);
    }

    @Override
    public void onTypeProperty() {
        L.trace("[start] onTypeProperty");
        for (FXNodeEventListener listener : listeners) {
            listener.onTypeProperty();
        }
        L.trace("[end] onTypeProperty");
    }

    @Override
    public void onTypeRoot() {
        L.trace("[start] onTypeRoot");
        for (FXNodeEventListener listener : listeners) {
            listener.onTypeRoot();
        }
        L.trace("[end] onTypeRoot");
    }

    @Override
    public void onType(Node node) {
        L.trace("[start] onType {}", node);
        for (FXNodeEventListener listener : listeners) {
            listener.onType(node);
        }
        L.trace("[end] onType {}", node);
    }

    @Override
    public void endContainer() {
        L.trace("[start] endContainer");
        for(FXNodeEventListener listener : listeners) {
            listener.endContainer();
        }
        L.trace("[end] endContainer");
    }
}
