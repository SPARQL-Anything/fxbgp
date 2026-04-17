package io.github.sparqlanything.fxbgp.stream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import io.github.sparqlanything.model.FacadeXAbstractNodeBuilder;
import io.github.sparqlanything.model.Triplifier;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamEventsHandler extends FacadeXAbstractNodeBuilder implements FXParserEventsHandler {
    private static final Logger L =  LoggerFactory.getLogger(StreamEventsHandler.class);
    public static final String PREDICATE_CACHE_OPTION = "predicate-cache";
    protected FXNodeEventListener eventListener;
    protected List<String> containerStack = new ArrayList<>();
    private boolean blankNodes = true;
    // null when disabled; populated on first use when enabled
    private final Map<String, Node> predicateCache;
    public StreamEventsHandler(Properties properties, FXNodeEventListener eventListener) {
        super(properties);
        if(properties.containsKey("blank-nodes")){
            this.blankNodes = Boolean.parseBoolean(properties.getProperty("blank-nodes"));
        }else{
            this.blankNodes = true;
        }
        this.eventListener = eventListener;
        this.predicateCache = Boolean.parseBoolean(
                properties.getProperty(PREDICATE_CACHE_OPTION, "false"))
                ? new HashMap<>()
                : null;
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
        Node predicate = predicateCache != null
                ? predicateCache.computeIfAbsent(key, this::key2predicate)
                : this.key2predicate(key);
        this.eventListener.onSlotString(predicate);
    }

    @Override
    public void onValue(Object obj) {
        L.trace("onValue {}", obj);
        this.eventListener.onValue(this.value2node(obj));
    }

    @Override
    public void onType(String type) {
        L.trace("onType {}", type);
        // FIXME Method seem missing on FacadeXNodeBuilder!
        this.eventListener.onTypeProperty();
        this.eventListener.onType(NodeFactory.createURI(this.getNamespace().concat(Triplifier.toSafeURIString(type))));
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

    public void shutdown() {
        if (eventListener instanceof FXBroadcastEventListener p) p.shutdown();
    }
}
