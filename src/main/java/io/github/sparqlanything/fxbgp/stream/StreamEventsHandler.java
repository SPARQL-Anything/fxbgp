package io.github.sparqlanything.fxbgp.stream;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import io.github.sparqlanything.model.FacadeXAbstractNodeBuilder;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.vocabulary.RDF;

public class StreamEventsHandler extends FacadeXAbstractNodeBuilder implements TriplifierEventsHandler{
    protected FXNodeEventListener eventListener;
    protected List<String> containerStack = new ArrayList<>();
    public StreamEventsHandler(Properties properties, FXNodeEventListener eventListener) {
        super(properties);
        this.eventListener = eventListener;
    }

    @Override
    public void startRoot(String dataSourceId) {
        String rootURI = this.getRootURI(dataSourceId);
        containerStack.add(rootURI);
        this.eventListener.startContainer(NodeFactory.createURI(rootURI));
        this.eventListener.onTypeProperty();
        this.eventListener.onTypeRoot();
    }

    @Override
    public void startContainer(String containerId) {
        String containerURI = containerStack.get(containerStack.size()-1);
        this.eventListener.startContainer(NodeFactory.createURI(containerURI + containerId));
    }

    @Override
    public void onSlotNumber(int key) {
        this.eventListener.onSlotNumber(RDF.li(key).asNode());
    }

    @Override
    public void onSlotString(String key) {
        this.eventListener.onSlotString(this.key2predicate(key));
    }

    @Override
    public void onValue(Object obj) {
        this.eventListener.onValue(this.value2node(obj));
    }

    @Override
    public void endContainer() {
        this.eventListener.endContainer();
    }

    @Override
    public void endRoot() {
        this.eventListener.endContainer();
    }
}
