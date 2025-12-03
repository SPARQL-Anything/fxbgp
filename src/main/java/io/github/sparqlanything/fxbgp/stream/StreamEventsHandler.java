package io.github.sparqlanything.fxbgp.stream;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import io.github.sparqlanything.model.FacadeXAbstractNodeBuilder;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamEventsHandler extends FacadeXAbstractNodeBuilder implements TriplifierEventsHandler{
    private static final Logger L =  LoggerFactory.getLogger(StreamEventsHandler.class);
    protected FXNodeEventListener eventListener;
    protected List<String> containerStack = new ArrayList<>();
    public StreamEventsHandler(Properties properties, FXNodeEventListener eventListener) {
        super(properties);
        this.eventListener = eventListener;
    }

    @Override
    public void startRoot(String dataSourceId) {
        L.trace("startRoot {}", dataSourceId);
        String rootURI = this.getRootURI(dataSourceId);
        containerStack.add(rootURI);
        this.eventListener.startContainer(NodeFactory.createURI(rootURI));
        this.eventListener.onTypeProperty();
        this.eventListener.onTypeRoot();
    }

    @Override
    public void startContainer(String containerId) {
        L.trace("startContainer {}", containerId);
        String containerURI = containerStack.get(containerStack.size()-1);
        this.eventListener.startContainer(NodeFactory.createURI(containerURI + containerId));
    }

    @Override
    public void onSlotNumber(int key) {
        L.trace("onSlotNumber {}", key);
        this.eventListener.onSlotNumber(RDF.li(key).asNode());
    }

    @Override
    public void onSlotString(String key) {
        L.trace("onSlotString {}", key);
        this.eventListener.onSlotString(this.key2predicate(key));
    }

    @Override
    public void onValue(Object obj) {
        L.trace("onValue {}", obj);
        this.eventListener.onValue(this.value2node(obj));
    }

    @Override
    public void endContainer() {
        L.trace("endContainer");
        this.eventListener.endContainer();
    }

    @Override
    public void endRoot() {
        L.trace("endRoot");
        this.eventListener.endContainer();
    }
}
