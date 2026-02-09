package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.model.SPARQLAnythingConstants;
import io.github.sparqlanything.model.Triplifier;
import io.github.sparqlanything.model.TriplifierHTTPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class XMLStreamParser implements FXStreamParser{
    private static final Logger L = LoggerFactory.getLogger(XMLStreamParser.class);
    //
    private Properties properties;
    //
    private FXEventType eventType;
    private Object value;
    private String container;
    private String dataSource;
    private int slotNumber;
    private String slotString;
    private String type;
    private String root;
    //
    private boolean completed = false;
    private boolean  cancelled;
    //
    private XMLInputFactory inputFactory;
    private XMLEventReader eventReader;
    private Deque<String> stack;
    private Map<String, Integer> members;
    private List<String> path;
    private XMLEvent event = null;
    private boolean moveToNext = true;
    private boolean containerStarted = false;
    private boolean checkForAttributes = false;
    private Iterator<Attribute> attributes = null;
    private Attribute attribute = null;
    private boolean startCharacters = false;
    private StringBuilder charBuilder = null;
    private int containerIndex = 0;
    private Map<Integer,Integer> containerCounter = new HashMap<>();
    public XMLStreamParser(Properties properties) {
        this.properties = properties;
    }

    @Override
    public boolean hasNextEvent() {
        // When completed
        if(isCancelled() || isCompleted()){
            return false;
        }

        if(dataSource == null){
            this.eventType = FXEventType.StartDataSource;
            return initDataSource();
        }

        if(!eventReader.hasNext()){
            return false;
        }

        if(moveToNext) {
            try {
                event = eventReader.nextEvent();
                if(event.isStartDocument()){
                    event = eventReader.nextEvent();
                }
            } catch (XMLStreamException e) {
                throw new RuntimeException(e);
            }
        }

        if(event.isStartElement()){
            if(!containerStarted && !checkForAttributes) {
                // If we are within another container, advance index
                // If root was set, we are within another container
                if(this.root != null && moveToNext){
                    // If this is the case, we are within another container
                    this.slotNumber = this.containerCounter.get(this.containerIndex) + 1;
                    this.containerCounter.put(this.containerIndex, this.slotNumber);
                    this.eventType = FXEventType.SlotNumber;
                    moveToNext = false;
                    return true;
                }

                // StartContainer
                if(root == null){
                    this.eventType = FXEventType.StartRoot;
                    this.root = SPARQLAnythingConstants.ROOT_ID;
                }else{
                    this.eventType = FXEventType.StartContainer;
                }
                this.containerIndex++;
                this.containerCounter.put(this.containerIndex, 0);
                // Set Container and Prepare type
                StartElement se = event.asStartElement();
                String name;
                // TODO Verify namespace
                if (se.getName().getPrefix().equals("")) {
                    name = se.getName().getLocalPart();
                } else {
                    name = se.getName().getPrefix() + ":" + se.getName().getLocalPart();
                }
                if(containerIndex > 1) {
                    this.path.add(this.slotNumber + ":" + name);
                }else{
                    this.path.add(name);
                }
                // Set container type
                this.type = name;
                containerStarted = true;
                if(se.asStartElement().getAttributes().hasNext()) {
                    this.attributes = se.asStartElement().getAttributes();
                }
                moveToNext = false; // We must trigger Type
                this.container = String.join("/", this.path) ;
                return true;
            }else if (containerStarted){
                this.eventType = FXEventType.Type;
                containerStarted = false;
                if(this.attributes != null){
                    checkForAttributes = true;
                    moveToNext = false;
                }else{
                    moveToNext = true;
                }
                return true;
            } else if(checkForAttributes){
                if(attribute == null){
                    // Trigger string key
                    this.eventType = FXEventType.SlotString;
                    attribute = attributes.next();
                    // TODO Check and handle namespaces
                    this.slotString = attribute.getName().getLocalPart();
                }else{
                    // Trigger Value
                    this.eventType = FXEventType.Value;
                    this.value = attribute.getValue();
                    this.attribute = null;
                    if(!attributes.hasNext()){
                        checkForAttributes = false;
                        moveToNext = true;
                        this.attributes = null;
                    }
                }
                return true;
            }
            throw new RuntimeException("This should not happen");
        }else if(event.isEndElement()){
            // Trigger end Container or end Root
            if(path.size() == 1){
                this.root = null;
                this.dataSource = null;
                this.path = new ArrayList<>();
                this.eventType = FXEventType.EndRoot;
                this.completed = true;
            } else {
                path.remove(path.size() - 1);
                this.eventType = FXEventType.EndContainer;
            }
            this.containerCounter.remove(this.containerIndex);
            this.containerIndex--;
            return true;
        }else if(event.isCharacters()){
            if(!startCharacters) {
                // Trigger slot number
                this.slotNumber = this.containerCounter.get(this.containerIndex) + 1;
                this.eventType = FXEventType.SlotNumber;
                this.startCharacters = true;
                this.moveToNext = false;
                this.containerCounter.put(this.containerIndex, this.slotNumber);
                return true;
            }else if(startCharacters && charBuilder == null){
                // Collect value
                charBuilder = new StringBuilder();
                while(eventReader.hasNext()) {
                    try {
                        this.event = eventReader.nextEvent();
                    } catch (XMLStreamException e) {
                        throw new RuntimeException(e);
                    }
                    if(event.isCharacters()){
                        charBuilder.append(event.asCharacters().getData().trim());
                    }else{
                        break;
                    }
                }
                this.moveToNext = false; // We already moved to the next one
                String chars = charBuilder.toString().trim();
                if(chars.length() == 0){
                    // Ignore event
                    return hasNextEvent();
                }else {
                    this.value = charBuilder.toString();
                    this.eventType = FXEventType.Value;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public FXEventType nextType() {
        return eventType;
    }

    @Override
    public Object getValue() {
        return this.value;
    }

    @Override
    public String getContainer() {
        return this.container;
    }

    @Override
    public String getDataSource() {
        return this.dataSource;
    }

    @Override
    public int getSlotNumber() {
        return this.slotNumber;
    }

    @Override
    public String getSlotString() {
        return this.slotString;
    }

    @Override
    public String getRoot() {
        return this.root;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public boolean isCompleted() {
        return this.completed;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void cancel() {
        this.cancelled = true;
        try {
            this.eventReader.close();
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean initDataSource() {
        this.dataSource = SPARQLAnythingConstants.DATA_SOURCE_ID;

        this.inputFactory = XMLInputFactory.newInstance();
        // TODO allow users to configure XML parser via properties
        inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        this.stack = new ArrayDeque<>();
        this.members = new HashMap<>();
        this.path = new ArrayList();
        //
        try {
            InputStream is = Triplifier.getInputStream(properties);
            this.eventReader = inputFactory.createXMLEventReader(is);
        } catch (XMLStreamException | IOException | TriplifierHTTPException e) {
            L.error("",e);
            return false;
        }
        return true;
    }
}
