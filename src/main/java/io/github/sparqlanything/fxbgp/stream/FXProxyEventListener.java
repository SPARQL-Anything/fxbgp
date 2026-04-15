package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.fxbgp.FX;
import io.github.sparqlanything.model.Triplifier;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.function.Consumer;

public class FXProxyEventListener implements FXNodeEventListener {
    private static final Logger L = LoggerFactory.getLogger(FXProxyEventListener.class);
    public static final String PARALLEL_THRESHOLD_OPTION = "parallel-threshold";
    public static final int DEFAULT_PARALLEL_THRESHOLD = 99999;

    private static final Node FX_ROOT_NODE =
            NodeFactory.createURI(Triplifier.FACADE_X_TYPE_ROOT);

    private final SharedPathAccessor accessor;

    // Serial path: iterate this array directly.
    private final FXNodeEventListener[] serialListeners;

    // Parallel path: pre-built worker threads, null when serial.
    private final WorkerThread[] workers;
    private final CyclicBarrier startBarrier;
    private final CyclicBarrier endBarrier;

    // BGP triples index: concrete predicate → triples; wildcards always checked
    private final Map<Node, Triple[]> bgpByPredicate;
    private final Triple[] bgpWildcardTriples;

    private Node subjectNode = null;
    private Node predicateNode = null;
    private Node objectNode = null;
    private FX predicateComponent = null;
    private FX objectComponent = null;
//    private int containersSent = 0;
    private Deque<Node> containersSent = new ArrayDeque<>();
    private Deque<Node> containersReceived = new ArrayDeque<>();
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
                                  SharedPathAccessor accessor, List<Triple> bgpTriples) {
        this.accessor = accessor;
        // Build predicate index
        Map<Node, List<Triple>> byPred = new HashMap<>();
        List<Triple> wildcards = new ArrayList<>();
        for (Triple t : bgpTriples) {
            Node p = t.getPredicate();
            if (p.isVariable() || p.isBlank()) {
                wildcards.add(t);
            } else {
                byPred.computeIfAbsent(p, k -> new ArrayList<>()).add(t);
            }
        }
        Map<Node, Triple[]> indexed = new HashMap<>();
        for (Map.Entry<Node, List<Triple>> e : byPred.entrySet()) {
            indexed.put(e.getKey(), e.getValue().toArray(new Triple[0]));
        }
        this.bgpByPredicate = indexed;
        this.bgpWildcardTriples = wildcards.toArray(new Triple[0]);
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
                                             int threshold, SharedPathAccessor accessor, List<Triple> bgpTriples) {
        return new FXProxyEventListener(listeners, threshold, accessor, bgpTriples);
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
        containersReceived.add(container);
        if(subjectNode == null) {
            this.subjectNode = container;
        } else {
            this.objectNode = container;
            this.objectComponent = FX.Container;
            triggerEvents(tripleMatches());
            clear();
            this.subjectNode = container;
        }
        L.trace("[end] startContainer {}", container);
    }

    @Override
    public void onSlotNumber(Node n) {
        L.trace("[start] onSlotNumber {}", n);
        predicateComponent = FX.SlotNumber;
        predicateNode = n;
        L.trace("[end] onSlotNumber {}", n);
    }

    @Override
    public void onSlotString(Node n) {
        L.trace("[start] onSlotString {}", n);
        predicateComponent = FX.SlotString;
        predicateNode = n;
        L.trace("[end] onSlotString {}", n);
    }

    @Override
    public void onValue(Node value) {
        L.trace("[start] onValue {}", value);
        objectComponent = FX.Value;
        objectNode = value;
        Node subject = subjectNode;
        triggerEvents(tripleMatches());
        clear();
        subjectNode = subject;
        L.trace("[end] onValue {}", value);
    }

    @Override
    public void onTypeProperty() {
        L.trace("[start] onTypeProperty");
        predicateComponent = FX.TypeProperty;
        predicateNode = RDF.type.asNode();
        L.trace("[end] onTypeProperty");
    }

    @Override
    public void onTypeRoot() {
        L.trace("[start] onTypeRoot");
        objectComponent = FX.Root;
        objectNode = FX_ROOT_NODE;
        triggerEvents(tripleMatches());
        Node subject = subjectNode;
        clear();
        subjectNode = subject;
        L.trace("[end] onTypeRoot");
    }

    @Override
    public void onType(Node node) {
        L.trace("[start] onType {}", node);
        objectComponent = FX.Type;
        objectNode = node;
        triggerEvents(tripleMatches());
        Node subject = subjectNode;
        clear();
        subjectNode = subject;
        L.trace("[end] onType {}", node);
    }

    @Override
    public void endContainer() {
        L.trace("[start] endContainer");

        Node n = containersReceived.removeLast();
        if(containersSent.size() > 0 && containersSent.getLast().equals(n) && ! accessor.isEmpty()) {
            fanOut(FXNodeEventListener::endContainer);
            // pop parent predicate, unless root
            containersSent.removeLast();
        }
        accessor.pop();                              // pop container
        if (!accessor.isEmpty()) accessor.pop();
        if(!accessor.isEmpty()) {
            this.subjectNode = accessor.currentPath().getLast(); // containersReceived.getLast();
        }
        L.trace("[end] endContainer");
    }

    protected boolean tripleMatches(){
        // Concrete-predicate bucket: predicate match is implicit in the map lookup
        Triple[] bucket = bgpByPredicate.get(predicateNode);
        if (bucket != null) {
            for (Triple t : bucket) {
                if (Matching.nodeMatches(t.getSubject(), subjectNode) &&
                        Matching.nodeMatches(t.getObject(), objectNode))
                    return true;
            }
        }
        // Wildcard-predicate triples: predicate is var/blank, skip that check
        for (Triple t : bgpWildcardTriples) {
            if (Matching.nodeMatches(t.getSubject(), subjectNode) &&
                    Matching.nodeMatches(t.getObject(), objectNode))
                return true;
        }
        return false;
    }

    protected void triggerEvents(boolean tripleMatches){
        // Do subject (only if root)
        if(accessor.isEmpty()){
            accessor.push(subjectNode);
        }
        if(tripleMatches && (containersSent.isEmpty() || !containersSent.getLast().equals(subjectNode))){
            fanOut(l -> l.startContainer(subjectNode));
            containersSent.add(subjectNode);
        }
        // Do predicate
        accessor.push(predicateNode);
        if(tripleMatches) {
            if (predicateComponent == FX.TypeProperty) {
                fanOut(l -> l.onTypeProperty());
            } else if (predicateComponent == FX.SlotString) {
                fanOut(l -> l.onSlotString(predicateNode));
            } else if (predicateComponent == FX.SlotNumber) {
                fanOut(l -> l.onSlotNumber(predicateNode));
            }
        }

        // Do Object
        accessor.push(objectNode);
        if (objectComponent == FX.Type) {
            if(tripleMatches)
                fanOut(l -> l.onType(objectNode));
            accessor.pop();
            accessor.pop();
        }else if(objectComponent == FX.Root){
            if(tripleMatches)
                fanOut(l -> l.onTypeRoot());
            accessor.pop();
            accessor.pop();
        }else if(objectComponent == FX.Value){
            if(tripleMatches)
                fanOut(l -> l.onValue(objectNode));
            accessor.pop();
            accessor.pop();
        }else if(objectComponent == FX.Container){
            if(tripleMatches) {
                fanOut(l -> l.startContainer(objectNode));
                containersSent.add(objectNode);
            }
        }
    }

    protected void clear(){
        subjectNode = null;
        predicateNode = null;
        predicateComponent = null;
        objectNode = null;
        objectComponent = null;
    }
}
