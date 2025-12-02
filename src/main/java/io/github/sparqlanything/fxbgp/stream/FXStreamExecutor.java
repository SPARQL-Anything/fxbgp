package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.fxbgp.AnalyserGrounder;
import io.github.sparqlanything.fxbgp.FXBGPAnnotation;
import io.github.sparqlanything.fxbgp.FXModel;
import io.github.sparqlanything.model.TriplifierHTTPException;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.sparql.algebra.op.OpBGP;
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
    private volatile Set<QuerySolution> solutions = new HashSet<>();
    private volatile boolean complete = false;
    public Iterator<QuerySolution> exec(OpBGP bgp, Properties properties) {
        // TODO choose the FX model specific to the format.
        AnalyserGrounder ag = new AnalyserGrounder(properties, FXModel.getFXModel());
        Set<FXBGPAnnotation> annotations = ag.annotate(bgp, true);
        final Set<FXQuerySolutionBuilder> patterns = new HashSet<>();
        for (FXBGPAnnotation annotation : annotations) {
            FXTreePattern tp = FXTreePattern.make(annotation);
            patterns.add( new FXQuerySolutionBuilder(tp, solutions));
        }

        // Parsing thread vs solution thread
        Executor executor = Executors.newCachedThreadPool();
        executor.execute(() -> {
            // Run the parser and attach the listening tree patterns.
            // TODO choose the FX Parser specific to the format
            CSVTriplifier2 triplifier2 = new CSVTriplifier2();
            try {
                triplifier2.triplify(properties,
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
            L.info("================ FINISHED ==========");
        });


        // Solution returns the iteator and waits
        return new Iterator<QuerySolution>() {

            @Override
            public boolean hasNext() {
                if(!complete) {
                    while(solutions.isEmpty() && !complete) {
                        // Wait for the other thread
                    }
                }
                return solutions.iterator().hasNext();
            }

            @Override
            public QuerySolution next() {
                QuerySolution solution = solutions.iterator().next();
                solutions.remove(solution);
                return solution;
            }
        };
    }
}
