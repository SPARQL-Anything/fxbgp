package io.github.sparqlanything.fxbgp.stream.performance;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class XMLGenerator {

    public static void generateXML(int height, int branchingFactor, List<List<String>> leafContainers, String filename) throws IOException, ParserConfigurationException, TransformerException {
        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .newDocument();


        Random r = new Random(System.currentTimeMillis());
        int slots = 0;

        Element root = doc.createElement("type0");
        doc.appendChild(root);
        List<Element> nextLevel = new ArrayList<>();
        nextLevel.add(root);

        for (int lev = 0; lev < height - 1; lev++) {
            List<Element> children = new ArrayList<>();
            for (Element e : nextLevel) {
                for (int childNumber = 0; childNumber < branchingFactor; childNumber++) {
                    Element child = doc.createElement("type" + (lev + 1));
                    e.appendChild(child);
                    children.add(child);
                    slots++;
                }
            }
            nextLevel = children;
        }

        // last level
        int leafContainer = 0;

        for (Element e : nextLevel) {
            List<String> container = leafContainers.get(leafContainer % leafContainers.size());
            leafContainer++;
            for (int i = 0; i < container.size(); i++) {
                e.setAttribute("f".concat(String.valueOf(i)), container.get(i));
                slots++;
            }
        }
        // System.out.println(slots);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.transform(new DOMSource(doc), new StreamResult(new File(filename)));

    }
}
