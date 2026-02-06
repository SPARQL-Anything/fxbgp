package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.fxbgp.FX;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIteratorBase;
import org.apache.jena.sparql.serializer.SerializationContext;

import java.util.Set;

public class FXParserQueryIterator extends QueryIteratorBase {
    FXStreamParser parser;
    Set<Binding> bindings;
    StreamEventsHandler handler;
    public FXParserQueryIterator(FXStreamParser parser, StreamEventsHandler handler, Set<Binding> bindings) {
        this.parser = parser;
        this.bindings = bindings;
        this.handler = handler;
    }
    @Override
    protected boolean hasNextBinding() {
        while(bindings.isEmpty() && !parser.isCompleted()) {
            // Keep moving until you get some binding
            if(parser.hasNext()) {
                FXEventType event = parser.nextType();
                switch(event) {
                    case StartDataSource -> { handler.onDataSource(parser.getDataSource());}
                    case StartRoot -> {handler.startRoot(parser.getRoot());}
                    case StartContainer -> {handler.startContainer(parser.getContainer());}
                    case Type -> { handler.onType(parser.getType());}
                    case SlotNumber ->  { handler.onSlotNumber(parser.getSlotNumber());}
                    case SlotString ->   { handler.onSlotString(parser.getSlotString());}
                    case Value ->   { handler.onValue(parser.getValue());}
                    case EndContainer -> {handler.endContainer();}
                    case EndRoot -> {handler.endRoot();}
                }
            }
        }
        return !bindings.isEmpty();
    }

    @Override
    protected Binding moveToNextBinding() {
        Binding binding = bindings.iterator().next();
        bindings.remove(binding);
        return binding;
    }

    @Override
    protected void closeIterator() {
        parser.cancel();
    }

    @Override
    protected void requestCancel() {
        parser.cancel();
    }

    @Override
    public void output(IndentedWriter indentedWriter, SerializationContext serializationContext) {

    }
}
