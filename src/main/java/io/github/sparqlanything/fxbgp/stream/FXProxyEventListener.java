package io.github.sparqlanything.fxbgp.stream;

import org.apache.jena.graph.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class FXProxyEventListener implements FXNodeEventListener {
    private static final Logger L = LoggerFactory.getLogger(FXProxyEventListener.class);

    static final int DEFAULT_PARALLEL_THRESHOLD = 4;

    private final Set<? extends FXNodeEventListener> listeners;
    private final ExecutorService pool;   // null when listeners.size() <= threshold

    private FXProxyEventListener(Set<? extends FXNodeEventListener> listeners, int threshold) {
        this.listeners = Collections.unmodifiableSet(listeners);
        this.pool = listeners.size() > threshold
                ? Executors.newWorkStealingPool()
                : null;
    }

    public static FXProxyEventListener make(Set<? extends FXNodeEventListener> listeners) {
        return new FXProxyEventListener(listeners, DEFAULT_PARALLEL_THRESHOLD);
    }

    public static FXProxyEventListener make(Set<? extends FXNodeEventListener> listeners, int threshold) {
        return new FXProxyEventListener(listeners, threshold);
    }

    public void shutdown() {
        if (pool != null) pool.shutdown();
    }

    private void fanOut(Consumer<FXNodeEventListener> action) {
        if (pool != null) {
            List<CompletableFuture<Void>> futures = listeners.stream()
                    .map(l -> CompletableFuture.runAsync(() -> action.accept(l), pool))
                    .collect(Collectors.toList());
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } else {
            for (FXNodeEventListener listener : listeners) {
                action.accept(listener);
            }
        }
    }

    @Override
    public void startDataSource(Node dataSource) {
        L.trace("[start] startDataSource {}", dataSource);
        fanOut(l -> l.startDataSource(dataSource));
        L.trace("[end] startDataSource {}", dataSource);
    }

    @Override
    public void startContainer(Node container) {
        L.trace("[start] startContainer {}", container);
        fanOut(l -> l.startContainer(container));
        L.trace("[end] startContainer {}", container);
    }

    @Override
    public void onSlotNumber(Node n) {
        L.trace("[start] onSlotNumber {}", n);
        fanOut(l -> l.onSlotNumber(n));
        L.trace("[end] onSlotNumber {}", n);
    }

    @Override
    public void onSlotString(Node n) {
        L.trace("[start] onSlotString {}", n);
        fanOut(l -> l.onSlotString(n));
        L.trace("[end] onSlotString {}", n);
    }

    @Override
    public void onValue(Node value) {
        L.trace("[start] onValue {}", value);
        fanOut(l -> l.onValue(value));
        L.trace("[end] onValue {}", value);
    }

    @Override
    public void onTypeProperty() {
        L.trace("[start] onTypeProperty");
        fanOut(FXNodeEventListener::onTypeProperty);
        L.trace("[end] onTypeProperty");
    }

    @Override
    public void onTypeRoot() {
        L.trace("[start] onTypeRoot");
        fanOut(FXNodeEventListener::onTypeRoot);
        L.trace("[end] onTypeRoot");
    }

    @Override
    public void onType(Node node) {
        L.trace("[start] onType {}", node);
        fanOut(l -> l.onType(node));
        L.trace("[end] onType {}", node);
    }

    @Override
    public void endContainer() {
        L.trace("[start] endContainer");
        fanOut(FXNodeEventListener::endContainer);
        L.trace("[end] endContainer");
    }
}
