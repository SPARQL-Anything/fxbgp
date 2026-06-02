package io.github.sparqlanything.fxbgp.stream.join.parsers;

import io.github.sparqlanything.fxbgp.stream.join.listeners.DataSourceContainerListener;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceContainer;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.impl.DataSourceContainerImpl;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.impl.DataSourceTypeImpl;
import io.github.sparqlanything.model.SPARQLAnythingConstants;
import io.github.sparqlanything.model.Triplifier;
import io.github.sparqlanything.model.TriplifierHTTPException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Properties;

public class XMLParser implements StreamParser {

    private final Properties properties;
    private final String dataSource;
    private final XMLEventReader eventReader;
    private final Deque<DataSourceContainer> openContainers = new ArrayDeque<>(100);
    private final Deque<Integer> childrenIds = new ArrayDeque<>(100);
    private final StringBuilder idBuilder = new StringBuilder();
    private final DataSourceContainerListener listener;

    public XMLParser(Properties properties, DataSourceContainerListener listener) {
        this.properties = properties;
        this.dataSource = SPARQLAnythingConstants.DATA_SOURCE_ID;
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        this.listener = listener;
        try {
            InputStream is = Triplifier.getInputStream(properties);
            this.eventReader = inputFactory.createXMLEventReader(is);
        } catch (XMLStreamException | IOException | TriplifierHTTPException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private String getCurrentLevelId(String localPart, int currentChild) {
        if (currentChild > 1) {
            // remove last part
            idBuilder.delete(idBuilder.lastIndexOf("/") + 1, idBuilder.length());
        }
        if (currentChild == 1) {
            idBuilder.append('/');
        }
        idBuilder.append(localPart);
        if (currentChild > 0) {
            idBuilder.append('_');
            idBuilder.append(currentChild);
        }
        return idBuilder.toString();
    }

    public void parse() {

        int currentChild = 0;
        XMLEvent lastEvent = null;
        DataSourceContainer currentContainer = null;

        while (eventReader.hasNext()) {
            try {
                XMLEvent currentEvent = eventReader.nextEvent();

                // if lastEvent == null and current event is start, then the tag is root
                if (currentContainer == null && currentEvent.isStartElement()) {
                    StartElement se = currentEvent.asStartElement();
                    currentChild = 0;
                    String containerId = getCurrentLevelId(se.getName().getLocalPart(), currentChild);
                    currentContainer = new DataSourceContainerImpl(containerId, properties);
                    currentContainer.setRoot(true);
                    currentContainer.addType(new DataSourceTypeImpl(se.getName().getLocalPart(), properties));
                    lastEvent = currentEvent;
                } else if (lastEvent != null) {
                    if (lastEvent.isStartElement() && currentEvent.isStartElement()) {
                        // first child of the new level
                        openContainers.push(currentContainer); // keep container for later
                        childrenIds.push(currentChild); // keep current child count for later
                        currentChild = 1; // initialise child count to 1

                        StartElement se = currentEvent.asStartElement();

                        // create new container
                        String containerId = getCurrentLevelId(se.getName().getLocalPart(), currentChild);
                        currentContainer = new DataSourceContainerImpl(containerId, properties);
                        currentContainer.addType(new DataSourceTypeImpl(se.getName().getLocalPart(), properties));
                        lastEvent = currentEvent;
                    } else if (lastEvent.isStartElement() && currentEvent.isEndElement()) {
                        // close current element
                        listener.onDataSourceContainer(currentContainer);
                        lastEvent = currentEvent;
                    } else if (lastEvent.isEndElement() && currentEvent.isStartElement()) {
                        // sibling element
                        currentChild++;  // increase child count

                        StartElement se = currentEvent.asStartElement();

                        // create container for the current element
                        String containerId = getCurrentLevelId(se.getName().getLocalPart(), currentChild);
                        currentContainer = new DataSourceContainerImpl(containerId, properties);
                        currentContainer.addType(new DataSourceTypeImpl(se.getName().getLocalPart(), properties));
                        lastEvent = currentEvent;
                    } else if (lastEvent.isEndElement() && currentEvent.isEndElement()) {

                        currentChild = childrenIds.pop();
                        currentContainer = openContainers.pop();

                        idBuilder.delete(idBuilder.lastIndexOf("/"), idBuilder.length());

                        listener.onDataSourceContainer(currentContainer);
                        lastEvent = currentEvent;
                    }

                }

            } catch (XMLStreamException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
