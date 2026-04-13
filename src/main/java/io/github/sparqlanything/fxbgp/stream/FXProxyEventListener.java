package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.model.Triplifier;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.function.Consumer;

public class FXProxyEventListener implements FXNodeEventListener {
    private static final Logger L = LoggerFactory.getLogger(FXProxyEventListener.class);
    public static final String PARALLEL_THRESHOLD_OPTION = "parallel-threshold";
    public static final int DEFAULT_PARALLEL_THRESHOLD = 999;

    private static final Node FX_ROOT_NODE =
            NodeFactory.createURI(Triplifier.FACADE_X_TYPE_ROOT);

    private final SharedPathAccessor accessor;

    // Serial path: iterate this array directly.
    private final FXNodeEventListener[] serialListeners;

    // Parallel path: pre-built worker threads, null when serial.
    private final WorkerThread[] workers;
    private final CyclicBarrier startBarrier;
    private final CyclicBarrier endBarrier;

    /**
     * One long-lived worker thread per listener.  It loops between two barriers:
     * <ol>
     *   <li>{@code startBarrier} – main thread sets {@code action} then releases all workers.</li>
     *   <li>{@code endBarrier}   – workers signal completion; main thread waits here.</li>
     * </ol>
     * No tasks or futures are allocated per event.
     */
    private static final class WorkerThread extends Thread {
        final FXNodeEventListener listener;
        // Written by main before startBarrier.await(); barrier provides happens-before.
        volatile Consumer<FXNodeEventListener> action;
        private final CyclicBarrier startBarrier;
        private final CyclicBarrier endBarrier;

        WorkerThread(FXNodeEventListener listener, CyclicBarrier start, CyclicBarrier end) {
            super("fx-worker");
            this.listener = listener;
            this.startBarrier = start;
            this.endBarrier = end;
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                for (;;) {
                    startBarrier.await();
                    try {
                        action.accept(listener);
                    } catch (RuntimeException e) {
                        L.error("Listener error during parallel dispatch", e);
                    }
                    endBarrier.await();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (BrokenBarrierException e) {
                // Normal shutdown path — barriers broken by shutdown().
            }
        }
    }

    private FXProxyEventListener(Set<? extends FXNodeEventListener> listeners, int threshold,
                                  SharedPathAccessor accessor) {
        this.accessor = accessor;
        if (listeners.size() > threshold) {
            L.info("Number of listeners exceeds threshold: {}", threshold);
            int n = listeners.size();
            CyclicBarrier start = new CyclicBarrier(n + 1);
            CyclicBarrier end   = new CyclicBarrier(n + 1);
            workers = new WorkerThread[n];
            int i = 0;
            for (FXNodeEventListener l : listeners) {
                workers[i] = new WorkerThread(l, start, end);
                workers[i].start();
                i++;
            }
            startBarrier = start;
            endBarrier   = end;
            serialListeners = null;
        } else {
            //L.info("Number of listeners below threshold: {}", threshold);
            serialListeners = listeners.toArray(new FXNodeEventListener[0]);
            workers      = null;
            startBarrier = null;
            endBarrier   = null;
        }
    }

    public static FXProxyEventListener make(Set<? extends FXNodeEventListener> listeners,
                                             int threshold, SharedPathAccessor accessor) {
        return new FXProxyEventListener(listeners, threshold, accessor);
    }

    public void shutdown() {
        if (workers != null) {
            for (WorkerThread w : workers) w.interrupt();
            startBarrier.reset();
            endBarrier.reset();
        }
    }

    private void fanOut(Consumer<FXNodeEventListener> action) {
        if (workers != null) {
            for (WorkerThread w : workers) w.action = action;
            try {
                startBarrier.await();   // release all workers
                endBarrier.await();     // wait for all workers to finish
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (BrokenBarrierException e) {
                L.warn("Barrier broken during fanOut — shutting down");
            }
        } else {
            for (FXNodeEventListener l : serialListeners) action.accept(l);
        }
    }

    @Override
    public void startDataSource(Node dataSource) {
        L.trace("[start] startDataSource {}", dataSource);
        accessor.reset();
        fanOut(l -> l.startDataSource(dataSource));
        L.trace("[end] startDataSource {}", dataSource);
    }

    @Override
    public void startContainer(Node container) {
        L.trace("[start] startContainer {}", container);
        accessor.push(container);
        fanOut(l -> l.startContainer(container));
        L.trace("[end] startContainer {}", container);
    }

    @Override
    public void onSlotNumber(Node n) {
        L.trace("[start] onSlotNumber {}", n);
        accessor.push(n);
        fanOut(l -> l.onSlotNumber(n));
        L.trace("[end] onSlotNumber {}", n);
    }

    @Override
    public void onSlotString(Node n) {
        L.trace("[start] onSlotString {}", n);
        accessor.push(n);
        fanOut(l -> l.onSlotString(n));
        L.trace("[end] onSlotString {}", n);
    }

    @Override
    public void onValue(Node value) {
        L.trace("[start] onValue {}", value);
        accessor.push(value);
        fanOut(l -> l.onValue(value));
        accessor.pop();  // pop value
        accessor.pop();  // pop predicate (pushed by onSlotNumber/onSlotString)
        L.trace("[end] onValue {}", value);
    }

    @Override
    public void onTypeProperty() {
        L.trace("[start] onTypeProperty");
        accessor.push(RDF.type.asNode());
        fanOut(FXNodeEventListener::onTypeProperty);
        L.trace("[end] onTypeProperty");
    }

    @Override
    public void onTypeRoot() {
        L.trace("[start] onTypeRoot");
        accessor.push(FX_ROOT_NODE);
        fanOut(FXNodeEventListener::onTypeRoot);
        accessor.pop();  // pop fxRoot
        accessor.pop();  // pop rdf:type (pushed by onTypeProperty)
        L.trace("[end] onTypeRoot");
    }

    @Override
    public void onType(Node node) {
        L.trace("[start] onType {}", node);
        accessor.push(node);
        fanOut(l -> l.onType(node));
        accessor.pop();  // pop type value
        accessor.pop();  // pop rdf:type (pushed by onTypeProperty)
        L.trace("[end] onType {}", node);
    }

    @Override
    public void endContainer() {
        L.trace("[start] endContainer");
        fanOut(FXNodeEventListener::endContainer);
        accessor.pop();                              // pop container
        if (!accessor.isEmpty()) accessor.pop();    // pop parent predicate, unless root
        L.trace("[end] endContainer");
    }
}
