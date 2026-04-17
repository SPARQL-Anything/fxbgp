package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.fxbgp.AnalyserGrounder;
import io.github.sparqlanything.fxbgp.FXBGPAnnotation;
import io.github.sparqlanything.fxbgp.FXModel;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Properties;
import java.util.Set;

public class FXStreamExecutor {
    private static final Logger L = LoggerFactory.getLogger(FXStreamExecutor.class);

    public QueryIterator exec(Op op, Properties properties) throws NotATreeException {
//
        Node graphNode = null;
        OpBGP opBGP = null;
        if (op instanceof OpGraph) {
            try {
                graphNode = ((OpGraph) op).getNode();
                opBGP = (OpBGP) ((OpGraph) op).getSubOp();
            } catch (Exception e) {
            }
        } else if (op instanceof OpBGP) {
            opBGP = (OpBGP) op;
        }
        if (opBGP == null) {
            L.error("Only Basic Graph Patterns are supported");
            throw new RuntimeException();
        }
        AnalyserGrounder ag = new AnalyserGrounder(properties, FXModel.getFXModel());
        Set<FXBGPAnnotation> annotations = ag.annotate(opBGP, true);
        final Set<Binding> bindings = ConcurrentHashMap.newKeySet();
        final Set<FXTreeSolutionBuilder> patterns = new HashSet<>();
        SharedPathAccessor accessor = new SharedPathAccessor();
        for (FXBGPAnnotation annotation : annotations) {
            FXTreePattern tp;
            if (graphNode == null) {
                // Play with default graph
                tp = FXTreePattern.make(annotation);
            } else {
                // Play with named graph
                tp = FXTreePattern.make(annotation, graphNode);
            }
            patterns.add(new FXTreeSolutionBuilder(tp, bindings, accessor));
        }

        List<Triple> bgpTriples = new ArrayList<>();
        for (FXBGPAnnotation a : annotations) {
            a.getOpBGP().getPattern().getList().forEach(bgpTriples::add);
        }

        int threshold = Integer.parseInt(properties.getProperty(
                FXProxyEventListener.PARALLEL_THRESHOLD_OPTION,
                String.valueOf(FXProxyEventListener.DEFAULT_PARALLEL_THRESHOLD)));
        boolean eventsFiltering = Boolean.parseBoolean(properties.getProperty(
                FXProxyEventListener.EVENTS_FILTERING_OPTION,
                String.valueOf(FXProxyEventListener.DEFAULT_EVENTS_FILTERING)));
        FXProxyEventListener proxy = eventsFiltering
                ? FXProxyEventListener.make(patterns, threshold, accessor, bgpTriples)
                : FXProxyEventListener.make(patterns, threshold, accessor);
        FXStreamParser parser = FXStreamParserRegistry.get(properties);
        StreamEventsHandler handler = new StreamEventsHandler(properties, proxy);
        return new FXParserQueryIterator(parser, handler, bindings);
    }
}
