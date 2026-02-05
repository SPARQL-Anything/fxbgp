package io.github.sparqlanything.fxbgp.stream;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import io.github.sparqlanything.model.FacadeXAbstractNodeBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamEventsHandler extends FacadeXAbstractNodeBuilder implements TriplifierEventsHandler{
    private static final Logger L =  LoggerFactory.getLogger(StreamEventsHandler.class);
    protected FXNodeEventListener eventListener;
    protected List<String> containerStack = new ArrayList<>();
    private boolean blankNodes = true;
    public StreamEventsHandler(Properties properties, FXNodeEventListener eventListener) {
        super(properties);
        // TODO Blank nodes = True?
        if(properties.containsKey("blank-nodes")){
            this.blankNodes = Boolean.parseBoolean(properties.getProperty("blank-nodes"));
        }else{
            this.blankNodes = true;
        }
        this.eventListener = eventListener;
    }

    @Override
    public void onDataSource(String dataSourceId) {
        L.trace("onDataSource {}", dataSourceId);
        String dsURI = this.getRootURI(dataSourceId);
        //containerStack.add(dsURI);
        this.eventListener.startDataSource(NodeFactory.createURI(dsURI));
    }

    @Override
    public void startRoot(String dataSourceId) {
        L.trace("startRoot {}", dataSourceId);
        Node containerNode;
        if(blankNodes){
            containerNode = NodeFactory.createBlankNode(dataSourceId);
            containerStack.add(dataSourceId);
        }else {
            String rootURI = this.getRootURI(dataSourceId);
            containerNode = NodeFactory.createURI(rootURI);
            containerStack.add(rootURI);
        }

        this.eventListener.startContainer(containerNode);
        this.eventListener.onTypeProperty();
        this.eventListener.onTypeRoot();
    }

    @Override
    public void startContainer(String containerId) {
        L.trace("startContainer {}", containerId);
        Node containerNode;
        if(blankNodes){
            containerNode = NodeFactory.createBlankNode(containerId);
        }else {
            String containerURI = containerStack.get(containerStack.size()-1);
            containerNode = NodeFactory.createURI(containerURI + containerId);
        }
        this.eventListener.startContainer(containerNode);
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
