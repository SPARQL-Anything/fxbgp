package io.github.sparqlanything.fxbgp.stream.join.parsers;

import io.github.sparqlanything.csv.CSVTriplifier;
import io.github.sparqlanything.fxbgp.stream.join.listeners.DataSourceContainerListener;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceContainer;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.impl.DataSourceContainerImpl;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.impl.DataSourceSlotNumberImpl;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.impl.DataSourceSlotStringImpl;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.impl.DataSourceValueImpl;
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

import static io.github.sparqlanything.csv.CSVTriplifier.PROPERTY_HEADER_ROW;

public class CSVParser implements StreamParser {

    private static final Logger L = LoggerFactory.getLogger(CSVParser.class);
    private final Properties properties;
    private final DataSourceContainerListener dataSourceEventListener;
    private final LinkedHashMap<Integer, String> headersMap;
    private final Iterator<CSVRecord> recordIterator;
    private final String dataSourceId;
    private final boolean useHeaders;

    public CSVParser(Properties properties, DataSourceContainerListener dataSourceEventListener) {
        this.properties = properties;
        this.dataSourceEventListener = dataSourceEventListener;
        this.dataSourceId = SPARQLAnythingConstants.DATA_SOURCE_ID;
        this.useHeaders = PropertyUtils.getBooleanProperty(properties, CSVTriplifier.PROPERTY_HEADERS);
        try {
            InputStream is = Triplifier.getInputStream(properties);
            CSVFormat format = CSVTriplifier.buildFormat(properties);
            Charset charset = Triplifier.getCharsetArgument(properties);
            InputStreamReader closableReader = new InputStreamReader(BOMInputStream.builder().setInputStream(is).get(), charset);
            Iterable<CSVRecord> records = format.parse(closableReader);
            this.recordIterator = records.iterator();
            this.headersMap = makeHeadersMapFromOpenIterator(format, charset);
        } catch (TriplifierHTTPException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void parse() {

        DataSourceContainer rootContainer = new DataSourceContainerImpl("", properties, true);

        for (int recordId = 1; recordIterator.hasNext(); recordId++) {
            String rowContainerId = "row_".concat(String.valueOf(recordId));
            DataSourceContainer rowContainer = new DataSourceContainerImpl(rowContainerId, properties);
            CSVRecord record = recordIterator.next();
            Iterator<String> cellIterator = record.iterator();
            for (int cellId = 1; cellIterator.hasNext(); cellId++) {
                String value = cellIterator.next();
                if (useHeaders) {
                    rowContainer.addSlot(new DataSourceSlotStringImpl(headersMap.get(cellId), properties), new DataSourceValueImpl(value, properties));
                } else {
                    rowContainer.addSlot(new DataSourceSlotNumberImpl(cellId, properties), new DataSourceValueImpl(value, properties));
                }
            }
            dataSourceEventListener.onDataSourceContainer(rowContainer);

            // row container only Id
            DataSourceContainer rowContainerOnlyId = new DataSourceContainerImpl(rowContainerId, properties);
            rootContainer.addSlot(new DataSourceSlotNumberImpl(recordId, properties), rowContainerOnlyId);
        }

    }


    public LinkedHashMap<Integer, String> makeHeadersMapFromOpenIterator(CSVFormat format, Charset charset) throws TriplifierHTTPException, IOException {
        int headersRow = PropertyUtils.getIntegerProperty(properties, PROPERTY_HEADER_ROW);
        Iterator<CSVRecord> iterator = recordIterator;
        if (headersRow > 0) {
            Reader in = new InputStreamReader(BOMInputStream.builder().setInputStream(Triplifier.getInputStream(properties)).get(), charset);
            Iterable<CSVRecord> records = format.parse(in);
            iterator = records.iterator();
            LinkedHashMap<Integer, String> headers_map = makeHeadersMapFromOpenIterator(headersRow, iterator);
            in.close();
            return headers_map;
        }
        return makeHeadersMapFromOpenIterator(headersRow, iterator);
    }

    private LinkedHashMap<Integer, String> makeHeadersMapFromOpenIterator(int headersRow, Iterator<CSVRecord> iterator) {
        int rowNumber = 1;
        LinkedHashMap<Integer, String> headers_map = new LinkedHashMap<>();
        if (useHeaders && iterator.hasNext()) {
            while (rowNumber != headersRow && iterator.hasNext()) {
                rowNumber++;
                iterator.next();
            }
            CSVRecord record = iterator.next();
            Iterator<String> columns = record.iterator();
            int colid = 0;
            while (columns.hasNext()) {
                colid++;
                String colstring = columns.next();
                String colname = colstring.strip();

                if (colname.isEmpty()) {
                    continue;
                }

                int c = 0;
                while (headers_map.containsValue(colname)) {
                    c++;
                    colname += "_".concat(String.valueOf(c));
                }
                headers_map.put(colid, colname);
            }
        }
        return headers_map;
    }


}
