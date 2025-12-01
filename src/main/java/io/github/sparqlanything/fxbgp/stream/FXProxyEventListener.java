package io.github.sparqlanything.fxbgp.stream;

import org.apache.jena.graph.Node;

import java.util.Collections;
import java.util.Set;

public class FXProxyEventListener implements FXNodeEventListener{
    private Set<? extends FXNodeEventListener> listeners;
    private FXProxyEventListener(Set<? extends FXNodeEventListener> listeners) {
        this.listeners = Collections.unmodifiableSet(listeners);
    }

    public final static FXNodeEventListener make(Set<? extends FXNodeEventListener> listeners) {
        return new FXProxyEventListener(listeners);
    }

    @Override
    public void startContainer(Node container) {
        for (FXNodeEventListener listener : listeners) {
            listener.startContainer(container);
        }
    }

    @Override
    public void onSlotNumber(Node n){
        for (FXNodeEventListener listener : listeners) {
            listener.onSlotNumber(n);
        }
    }

    @Override
    public void onSlotString(Node n){
        for (FXNodeEventListener listener : listeners) {
            listener.onSlotString(n);
        }
    }

    @Override
    public void onValue(Node value) {
        for (FXNodeEventListener listener : listeners) {
            listener.onValue(value);
        }
    }

    @Override
    public void onTypeProperty() {
        for (FXNodeEventListener listener : listeners) {
            listener.onTypeProperty();
        }
    }

    @Override
    public void onTypeRoot() {
        for (FXNodeEventListener listener : listeners) {
            listener.onTypeRoot();
        }
    }

    @Override
    public void onType(Node node) {
        for (FXNodeEventListener listener : listeners) {
            listener.onType(node);
        }
    }

    @Override
    public void endContainer() {
        for(FXNodeEventListener listener : listeners) {
            listener.endContainer();
        }
    }
}
