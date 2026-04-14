package io.github.sparqlanything.fxbgp;

import io.github.sparqlanything.engine.FacadeX;
import io.github.sparqlanything.fxbgp.stream.FXStreamExecutor;
import io.github.sparqlanything.fxbgp.stream.NotATreeException;
import io.github.sparqlanything.model.IRIArgument;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sys.JenaSystem;

import java.util.Properties;

public class Run {


    private long executeWithStream(OpBGP op, Properties properties) throws NotATreeException {
        FXStreamExecutor exec = new FXStreamExecutor();
        return countResults(exec.exec(op, properties));
    }

    private long executeMaterialisation(OpBGP op, Properties properties) {
        JenaSystem.init();
        QC.setFactory(ARQ.getContext(), FacadeX.ExecutorFactory);
        ExecutionContext execCxt = ExecutionContext.create(DatasetGraphFactory.create());
        OpService opService = new OpService(NodeFactory.createURI("x-sparql-anything:location=" + properties.getProperty(IRIArgument.LOCATION.toString())), op, false);
        return countResults(QC.execute(opService, OpExecutor.createRootQueryIterator(execCxt), execCxt));
    }

    private long countResults(QueryIterator qi) {
        long l = 0L;
        while (qi.hasNext()) {
            qi.next();
            l++;
        }
        return l;
    }

    public static void main(String[] args) {

    }
}
