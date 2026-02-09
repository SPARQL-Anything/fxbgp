package io.github.sparqlanything.fxbgp.stream;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import io.github.sparqlanything.json.JSONTriplifier;
import io.github.sparqlanything.model.PropertyUtils;
import io.github.sparqlanything.model.SPARQLAnythingConstants;
import io.github.sparqlanything.model.Triplifier;
import io.github.sparqlanything.model.TriplifierHTTPException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.fasterxml.jackson.core.JsonToken.END_ARRAY;
import static com.fasterxml.jackson.core.JsonToken.END_OBJECT;
import static com.fasterxml.jackson.core.JsonToken.FIELD_NAME;
import static com.fasterxml.jackson.core.JsonToken.START_ARRAY;
import static com.fasterxml.jackson.core.JsonToken.START_OBJECT;

public class JSONStreamParser implements FXStreamParser {
    private static final Logger L = LoggerFactory.getLogger(CSVStreamParser.class);
    //
    private Properties properties;
    //
    private FXEventType eventType;
    private Object value;
    private String container;
    private String dataSource;
    private int slotNumber;
    private String slotString;
    private String root;
    //
    //private boolean recordContainerReady;
    private boolean completed;
    private boolean  cancelled;
    //
    private InputStream closeableInputStream;
    private JsonParser parser;
    private JsonToken token;
    private boolean includeNullValues;
    //
    private int containerIndex;
    private boolean inArray;
    private boolean waitForArrayItem = false;
    private Map<Integer,Integer> arrayIndexes = new HashMap<>();
    //
    public JSONStreamParser(Properties properties) {
        this.properties = properties;
        this.eventType = null;
    }

    /**
     * This is where we set the Event Type and related objects
     *
     * @return
     */
    @Override
    public boolean hasNextEvent() {

        // When completed
        if(completed || cancelled){
            return false;
        }

        if(dataSource == null){
            this.eventType = FXEventType.StartDataSource;
            return initDataSource();
        }else if(root == null){
            this.eventType = FXEventType.StartRoot;
            return initRoot();
        }

        //
        if(!waitForArrayItem) {
            try {
                token = parser.nextToken();
            } catch (IOException e) {
                L.error("", e);
                throw new RuntimeException("This should never happen");
            }
        }

        switch(token) {
            case START_ARRAY:
            case START_OBJECT:
                // Manage when this happens within an array
                if(inArray && !waitForArrayItem){
                    this.eventType = FXEventType.SlotNumber;
                    this.slotNumber = this.arrayIndexes.get(this.containerIndex) + 1;
                    this.arrayIndexes.put(this.containerIndex, this.slotNumber);
                    this.waitForArrayItem = true;
                    return true;
                }
                this.waitForArrayItem = false;
                this.container = (this.container == null) ? this.root : this.container;
                // From string or from int?
                if(this.eventType == FXEventType.SlotString){
                    this.container = StringUtils.join(this.container, "/", slotString);
                }else if(this.eventType == FXEventType.SlotNumber){
                    this.container = StringUtils.join(this.container, "/_", String.valueOf(slotNumber));
                }
                this.eventType = FXEventType.StartContainer;
                this.containerIndex++;
                if(token == START_ARRAY){
                    this.inArray = true;
                    this.arrayIndexes.put(this.containerIndex, 0);
                }
                return true;
            case END_ARRAY:
            case END_OBJECT:
                this.containerIndex--;
                // Is the containing element an array?
                this.inArray = arrayIndexes.containsKey(this.containerIndex);
                if(this.containerIndex == 0){
                    this.eventType = FXEventType.EndRoot;
                    this.complete();
                }else {
                    this.eventType = FXEventType.EndContainer;
                }
                return true;
            case FIELD_NAME:
                this.eventType = FXEventType.SlotString;
                try {
                    this.slotString = parser.getText();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return true;
            case VALUE_STRING:
                // Manage when this happens within an array
                if(inArray && !waitForArrayItem){
                    this.eventType = FXEventType.SlotNumber;
                    this.slotNumber = this.arrayIndexes.get(this.containerIndex) + 1;
                    this.arrayIndexes.put(this.containerIndex, this.slotNumber);
                    this.waitForArrayItem = true;
                    return true;
                }
                this.waitForArrayItem = false;
                this.eventType = FXEventType.Value;
                try {
                    this.value = parser.getValueAsString();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return true;
            case VALUE_NULL:
                if(includeNullValues){
                    // Manage when this happens within an array
                    if(inArray && !waitForArrayItem){
                        this.eventType = FXEventType.SlotNumber;
                        this.slotNumber = this.arrayIndexes.get(this.containerIndex) + 1;
                        this.arrayIndexes.put(this.containerIndex, this.slotNumber);
                        this.inArray = true;
                        this.waitForArrayItem = true;
                        return true;
                    }
                    this.waitForArrayItem = false;
                    this.eventType = FXEventType.Value;
                    this.value = Triplifier.XYZ_NULL_NODE;
                    return true;
                }else{
                    return hasNextEvent();
                }
            case VALUE_NUMBER_INT:
                // Manage when this happens within an array
                if(inArray && !waitForArrayItem){
                    this.eventType = FXEventType.SlotNumber;
                    this.slotNumber = this.arrayIndexes.get(this.containerIndex) + 1;
                    this.arrayIndexes.put(this.containerIndex, this.slotNumber);
                    this.waitForArrayItem = true;
                    return true;
                }
                this.waitForArrayItem = false;
                this.eventType = FXEventType.Value;
                try {
                    this.value = parser.getValueAsInt();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return true;
            case VALUE_TRUE:
            case VALUE_FALSE:
                // Manage when this happens within an array
                if(inArray && !waitForArrayItem){
                    this.eventType = FXEventType.SlotNumber;
                    this.slotNumber = this.arrayIndexes.get(this.containerIndex) + 1;
                    this.arrayIndexes.put(this.containerIndex, this.slotNumber);
                    this.waitForArrayItem = true;
                    return true;
                }
                this.waitForArrayItem = false;
                this.eventType = FXEventType.Value;
                try {
                    this.value = parser.getValueAsBoolean();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return true;
            case VALUE_NUMBER_FLOAT:
                // Manage when this happens within an array
                if(inArray && !waitForArrayItem){
                    this.eventType = FXEventType.SlotNumber;
                    this.slotNumber = this.arrayIndexes.get(this.containerIndex) + 1;
                    this.arrayIndexes.put(this.containerIndex, this.slotNumber);
                    this.waitForArrayItem = true;
                    return true;
                }
                this.waitForArrayItem = false;
                this.eventType = FXEventType.Value;
                try {
                    this.value = parser.getValueAsDouble();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return true;
            case NOT_AVAILABLE:
            case VALUE_EMBEDDED_OBJECT:
                // Dunno
        }
        this.completed = true;
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
        throw new RuntimeException("invalid component: type");
    }

    private void complete(){
        this.completed = true;
        try {
            this.closeableInputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public boolean isCompleted() {
        return completed;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void cancel() {
        this.cancelled = true;
        try {
            this.closeableInputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean initDataSource() {
        JsonFactory factory = JsonFactory.builder().build();
        try {
            closeableInputStream = Triplifier.getInputStream(properties);
            parser = factory.createParser(closeableInputStream);
            this.dataSource = SPARQLAnythingConstants.DATA_SOURCE_ID;
            this.eventType = FXEventType.StartDataSource;
            this.includeNullValues = PropertyUtils.getBooleanProperty(properties, JSONTriplifier.PROPERTY_JSONINCLUDENULLVALUES);
            return true;
        } catch (IOException e) {
            L.error("Error while initializing JSON stream", e);
        } catch (TriplifierHTTPException e) {
            L.error("Error while initializing JSON stream", e);
        }
        return false;
    }

    private boolean initRoot()  {
        if(parser == null){
            throw new RuntimeException("parser is null");
        }
        try {
            token = parser.nextToken();
        } catch (IOException e) {
            L.error("Error while initializing root", e);
            return false;
        }
        containerIndex = 0; // We will sum it up everytime
        if (token == JsonToken.START_OBJECT) {
            this.eventType = FXEventType.StartRoot;
            this.root = this.dataSource;
            return true;
            //transformObject(parser, SPARQLAnythingConstants.DATA_SOURCE_ID, SPARQLAnythingConstants.ROOT_ID, eventsHandler, includeNullValues);
        } else if (token == JsonToken.START_ARRAY) {
            this.root = this.dataSource;
            this.eventType = FXEventType.StartRoot;
            return true;
        }else{
            L.error("Error while parsing root: {}", token);
            throw new RuntimeException("Unexpected token: " + token);
        }
    }
}
