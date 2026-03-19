package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.fxbgp.AnalyserGrounder;
import io.github.sparqlanything.fxbgp.FXBGPAnnotation;
import io.github.sparqlanything.fxbgp.FXModel;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
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
        final Set<FXQuerySolutionBuilder> patterns = new HashSet<>();
        for (FXBGPAnnotation annotation : annotations) {
            FXTreePattern tp;
            if (graphNode == null) {
                // Play with default graph
                tp = FXTreePattern.make(annotation);
            } else {
                // Play with named graph
                tp = FXTreePattern.make(annotation, graphNode);
            }
            patterns.add(new FXQuerySolutionBuilder(tp, bindings));
        }

        int threshold = Integer.parseInt(properties.getProperty(
                "parallel-threshold",
                String.valueOf(FXProxyEventListener.DEFAULT_PARALLEL_THRESHOLD)));
        FXStreamParser parser = FXStreamParserRegistry.get(properties);
        StreamEventsHandler handler = new StreamEventsHandler(properties,
                FXProxyEventListener.make(patterns, threshold));
        return new FXParserQueryIterator(parser, handler, bindings);
    }
}
