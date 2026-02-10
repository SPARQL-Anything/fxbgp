package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.csv.CSVTriplifier;
import io.github.sparqlanything.model.PropertyUtils;
import io.github.sparqlanything.model.SPARQLAnythingConstants;
import io.github.sparqlanything.model.Triplifier;
import io.github.sparqlanything.model.TriplifierHTTPException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;

import static io.github.sparqlanything.csv.CSVTriplifier.IGNORE_COLUMNS_WITH_NO_HEADERS;
import static io.github.sparqlanything.csv.CSVTriplifier.PROPERTY_HEADER_ROW;

public class CSVStreamParser implements FXStreamParser{
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
    private boolean recordContainerReady;
    private boolean completed;
    private boolean  cancelled;
    //
    private Integer headersRowNumber;
    private boolean ignoreColumnsWithNoHeaders;
    private CSVFormat format;
    private Charset charset;
    private Reader closableReader;
    private Iterator<CSVRecord> recordIterator;
    private LinkedHashMap<Integer, String> headersMap;
    //
    private CSVRecord record;
    private int recordIndex = 0;
    private Iterator<String> cellIterator;
    private int cellIndex = 0;
    private String cellValue = null;

    //
    public CSVStreamParser(Properties properties) {
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
        }else if(record == null){
            // If CSV record is null, set the next record
            if((this.slotNumber = nextRecord()) > 0){
                // If there is a next record, return a slotNumber event
                this.eventType = FXEventType.SlotNumber;
                this.recordContainerReady = false;
                return true;
            }
            // No other records, close Root Container
            this.eventType = FXEventType.EndRoot;
            this.complete();
            // There is still the EndRoot event to be returned
            return true;
        } else if(record != null && !recordContainerReady ){
            // Open the container
            this.container = "#row" + Integer.toString(this.slotNumber);
            this.eventType = FXEventType.StartContainer;
            this.recordContainerReady = true;
            return true;
        }else if(record != null && recordContainerReady ){
            // We move on checking cells
            // If last event was a Property
            if(this.eventType == FXEventType.SlotString ||
                    this.eventType == FXEventType.SlotNumber){
                this.value = this.cellValue;
                this.eventType = FXEventType.Value;
                return true;
            }
            // If the last event was a value or a container and there will be a next cell
            if(
                    (this.eventType == FXEventType.StartContainer ||
                    this.eventType == FXEventType.Value)
                 ) {
                int nextCell = nextCell();
                if(nextCell == -1){
                    this.eventType = FXEventType.EndContainer;
                    this.record = null;
                    this.cellIterator = null;
                    this.cellIndex = 0;
                    this.recordContainerReady = false;
                    return true;
                }
                if(!headersMap.isEmpty()){
                    this.eventType = FXEventType.SlotString;
                    this.slotString = (String) headersMap.get(nextCell);
                }else {
                    this.eventType = FXEventType.SlotNumber;
                    this.slotNumber = (int) nextCell;
                }
                return true;
            }
        }
        throw new RuntimeException("Can we get here?");
        //return false;
    }

    @Override
    public FXEventType nextType() {
        return eventType;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public String getContainer() {
        return container;
    }

    @Override
    public String getDataSource() {
        return this.dataSource;
    }

    @Override
    public int getSlotNumber() {
        return slotNumber;
    }

    @Override
    public String getSlotString() {
        return slotString;
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
            this.closableReader.close();
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
    public void cancel(){
        this.cancelled = true;
        try {
            this.closableReader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean initDataSource(){
        try {
            format = CSVTriplifier.buildFormat(properties);
        } catch (IOException e) {
            L.error("{}", e);
            return false;
        }
        this.charset = Triplifier.getCharsetArgument(properties);
        this.headersRowNumber = PropertyUtils.getIntegerProperty(properties, PROPERTY_HEADER_ROW);
        this.dataSource = SPARQLAnythingConstants.DATA_SOURCE_ID; // there is always 1 data source id
        this.ignoreColumnsWithNoHeaders = PropertyUtils.getBooleanProperty(properties, IGNORE_COLUMNS_WITH_NO_HEADERS);
        // We are ready to go
        return true;
    }

    private boolean initRoot(){
        this.root = this.dataSource;
        try {
            InputStream is = Triplifier.getInputStream(properties);
            this.closableReader = new InputStreamReader(BOMInputStream.builder().setInputStream(is).get(), charset);
            Iterable<CSVRecord> records = format.parse(closableReader);
            this.recordIterator = records.iterator();
            headersMap = CSVTriplifier2.makeHeadersMapFromOpenIterator(recordIterator, properties, format, charset);
        } catch (TriplifierHTTPException e) {
            L.error("{}", e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            L.error("{}", e);
            throw new RuntimeException(e);
        }
        return true;
    }

    private int nextRecord(){
        // Assumption: record is null
        if(record != null){
            throw new RuntimeException("record must be null");
        }
        if(recordIterator.hasNext()) {
            recordIndex += 1;
            if (recordIndex == headersRowNumber && !headersMap.isEmpty()) {
                // skip headers row
                headersRowNumber = -1; // this avoids that the condition is verified in the next iterations
                // Ignore this record
                recordIterator.next();
            }
        }
        // We try again, recordIndex already pushed
        if(recordIterator.hasNext()){
            record = recordIterator.next();
            cellIterator = record.iterator();
            return recordIndex;
        }else{
            recordIndex = -1;
            return recordIndex;
        }
    }

    private int nextCell(){
        if(cellIterator == null){
            throw new RuntimeException("cellIterator cannot be null");
        }

        if(cellIterator.hasNext()){
            cellIndex += 1;
            cellValue = cellIterator.next();
            return cellIndex;
        }else{
            cellIndex = -1;
            cellValue = null;
            cellIterator = null;
            return cellIndex;
        }
    }
}
