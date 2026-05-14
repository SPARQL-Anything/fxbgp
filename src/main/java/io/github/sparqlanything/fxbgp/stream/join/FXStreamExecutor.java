package io.github.sparqlanything.fxbgp.stream.join;

import io.github.sparqlanything.fxbgp.AnalyserGrounder;
import io.github.sparqlanything.fxbgp.FXBGPAnnotation;
import io.github.sparqlanything.fxbgp.FXModel;
import io.github.sparqlanything.fxbgp.stream.*;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FXStreamExecutor {
    private static final Logger L = LoggerFactory.getLogger(FXStreamExecutor.class);

    public QueryIterator exec(OpBGP opBGP, Properties properties) throws NotATreeException {

        AnalyserGrounder ag = new AnalyserGrounder(properties, FXModel.getFXModel());
        Set<FXBGPAnnotation> annotations = ag.annotate(opBGP, true);
        final Set<Binding> bindings = ConcurrentHashMap.newKeySet();
        final Set<FXTreeSolutionBuilder> patterns = new HashSet<>();
        SharedPathAccessor accessor = new SharedPathAccessor();
        for (FXBGPAnnotation annotation : annotations) {
            FXTreePattern tp = FXTreePattern.make(annotation);
            patterns.add(new FXTreeSolutionBuilder(tp, bindings, accessor));
        }

        List<Triple> bgpTriples = new ArrayList<>();
        for (FXBGPAnnotation a : annotations) {
            a.getOpBGP().getPattern().getList().forEach(bgpTriples::add);
        }

        //return new FXParserQueryIterator(parser, handler, bindings);
        return null;
    }
}
