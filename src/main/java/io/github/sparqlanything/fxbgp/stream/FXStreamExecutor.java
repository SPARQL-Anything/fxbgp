package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.fxbgp.AnalyserGrounder;
import io.github.sparqlanything.fxbgp.FXBGPAnnotation;
import io.github.sparqlanything.fxbgp.FXModel;
import io.github.sparqlanything.model.TriplifierHTTPException;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Node;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * This is a temporary implementation; we will need to change the Triplifier interface and replace FacadeXGraphBuilder with an intermediate interface that returns the iterator of query solutions
 *
 */
public class FXStreamExecutor {
    private static final Logger L = LoggerFactory.getLogger(FXStreamExecutor.class);
    private volatile Set<Binding> bindings = new HashSet<>();
    private volatile boolean complete = false;
    public QueryIterator exec(Op op, Properties properties) throws NotATreeException {
        //
        Node graphNode = null;
        OpBGP opBGP = null;
        if (op instanceof OpGraph){
            try{
                graphNode = ((OpGraph) op).getNode();
                opBGP = (OpBGP) ((OpGraph) op).getSubOp();
            }catch (Exception e){
            }
        }else if (op instanceof OpBGP) {
            opBGP = (OpBGP) op;
        }
        if(opBGP == null){
            L.error("Only Basic Graph Patterns are supported");
            throw new RuntimeException();
        }
        AnalyserGrounder ag = new AnalyserGrounder(properties, FXModel.getFXModel());
        Set<FXBGPAnnotation> annotations = ag.annotate(opBGP, true);
        final Set<FXQuerySolutionBuilder> patterns = new HashSet<>();
        for (FXBGPAnnotation annotation : annotations) {
            FXTreePattern tp;
            if(graphNode == null){
                // Play with default graph
                tp = FXTreePattern.make(annotation);
            }else{
                // Play with named graph
                tp = FXTreePattern.make(annotation, graphNode);
            }
            patterns.add( new FXQuerySolutionBuilder(tp, bindings));
        }

        // Parsing thread vs solution thread
        Executor executor = Executors.newCachedThreadPool();

        executor.execute(() -> {
            // Run the parser and attach the listening tree patterns.
            // TODO choose the FX Parser specific to the format
            FXParser parser = Triplifier2.get(properties);
            try {
                parser.triplify(properties,
                        new StreamEventsHandler(properties,
                                FXProxyEventListener.make(patterns)));
                complete = true;
            } catch (IOException e) {
                complete = true; // Let's leave the other thread in peace.
                throw new RuntimeException(e);
            } catch (TriplifierHTTPException e) {
                complete = true; // Let's leave the other thread in peace.
                throw new RuntimeException(e);
            }
//            L.error("Execution complete in inner thread");
        });

        // Solution returns the iteator and waits
        return new QueryIterator() {

            @Override
            public boolean hasNext() {
//                System.out.println("before hasNext");
                if(!complete) {
                    while(bindings.isEmpty() && !complete) {
                        // Wait for the other thread
//                        System.out.print(".");
                    }
                }
//                System.out.println("after hasNext");
                //L.error("Returning {}", retu);
                return bindings.iterator().hasNext();
            }

            @Override
            public void output(IndentedWriter indentedWriter, SerializationContext serializationContext) {
//                System.out.println("output");
                //for(Binding binding : bindings) {}
            }

            @Override
            public String toString(PrefixMapping prefixMapping) {
//                System.out.println("toString");
                return "";
            }

            @Override
            public void close() {
                // XXX What to do here?
//                System.out.println("close");
            }

            @Override
            public void output(IndentedWriter indentedWriter) {
//                System.out.println("output 2");
            }

            @Override
            public Binding nextBinding() {
//                System.out.println("nextBinding");
                return next();
            }

            @Override
            public void cancel() {
//                System.out.println("cancel");
                // XXX What to do here?
            }

            @Override
            public Binding next() {
//                System.out.println("next (before)");
                Binding solution;
                synchronized (bindings) {
                    solution = bindings.iterator().next();
                    bindings.remove(solution);
                }
//                System.out.println("next (after");
                return solution;
            }
        };
    }
}
