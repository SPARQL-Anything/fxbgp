package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.fxbgp.Analyser;
import io.github.sparqlanything.fxbgp.AnalyserGrounder;
import io.github.sparqlanything.fxbgp.BGPTestUtils;
import io.github.sparqlanything.fxbgp.FXBGPAnnotation;
import io.github.sparqlanything.fxbgp.FXModel;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.core.BasicPattern;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Properties;
import java.util.Set;

public class ToGraphML {
    private static String NS = "http://graphml.graphdrawing.org/xmlns";
    private static String Y = "http://www.yworks.com/xml/graphml";
    private static Document createGraphMLDocument() throws ParserConfigurationException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        Document doc = builder.newDocument();
        doc.setXmlStandalone(true);
        doc.setXmlVersion("1.0");
        // graphml
        Element graphml = doc.createElement("graphml");
        doc.appendChild(graphml);
        doc.getDocumentElement().setAttribute("xmlns", NS);
        doc.getDocumentElement().setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        // xmlns:y="http://www.yworks.com/xml/graphml"
        doc.getDocumentElement().setAttribute("xmlns:y", "http://www.yworks.com/xml/graphml");
        return doc;
    }

    public static Element makeGraph(Document doc, String id){
        // graph
        Element graph = doc.createElement("graph");
        graph.setAttribute("edgedefault","directed");
        graph.setAttribute("id",id);
        doc.getDocumentElement().appendChild(graph);
        // Node label
        Element key = doc.createElement("key");
        key.setAttribute("id","label");
        key.setAttribute("for","node");
        key.setAttribute("attr.name","label");
        key.setAttribute("attr.type","string");
        doc.getDocumentElement().appendChild(key);
        // Edge label
        Element key2 = doc.createElement("key");
        key2.setAttribute("id","label");
        key2.setAttribute("for","edge");
        key2.setAttribute("attr.name","label");
        key2.setAttribute("attr.type","string");
        doc.getDocumentElement().appendChild(key2);

        return graph;
    }

    public static Document createGraph(String id) throws ParserConfigurationException {
        Document doc = createGraphMLDocument();
        makeGraph(doc, id);
        return doc;
    }

    public static Document toDocument(FXTreePattern tp) throws ParserConfigurationException {
        Document doc = createGraph(Integer.toString(tp.hashCode()));
        processNode(doc, tp.getRoot());
        return doc;
    }

    public static void makeNode(Document doc, Node node) {
        makeNode(doc, node.toString(), node.toString());
    }


    public static Element makeNode(Document doc, String nodeId, String nodeLabel) {
        Element e_n = doc.createElement("node");
        e_n.setAttribute("id", nodeId);
        doc.getElementsByTagName("graph").item(0).appendChild(e_n);

        Element e_s_label = doc.createElement("data");
        e_s_label.setAttribute("key", "label");
        e_s_label.setTextContent(nodeLabel);
        e_n.appendChild(e_s_label);

        Element shape = doc.createElement("y:ShapeNode");
        Element label = doc.createElement("y:NodeLabel");
        label.setTextContent(nodeLabel);
        // <y:Geometry height="30.0" width="30.0" x="519.0" y="135.0"/>
        Element geometry = doc.createElement("y:Geometry");
        geometry.setAttribute("height", "30.0");
        geometry.setAttribute("width", "80.0");
        shape.appendChild(geometry);
        // <y:Fill color="#FFCC00" transparent="false"/>
        Element fill = doc.createElement("y:Fill");
        fill.setAttribute("color", "#FFCC00");
        fill.setAttribute("transparent", "false");
        shape.appendChild(fill);
        // <y:BorderStyle color="#000000" raised="false" type="line" width="1.0"/>
        Element border = doc.createElement("y:BorderStyle");
        border.setAttribute("color", "#000000");
        border.setAttribute("raised", "false");
        border.setAttribute("type", "line");
        border.setAttribute("width", "1.0");
        shape.appendChild(border);
        // <y:Shape type="ellipse"/>
        Element sh = doc.createElement("y:Shape");
        sh.setAttribute("type", "ellipse");
        shape.appendChild(sh);
        // <y:NodeLabel alignment="center" autoSizePolicy="content" fontFamily="Dialog" fontSize="12" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="18.1328125" horizontalTextPosition="center" iconTextGap="4" modelName="custom" textColor="#000000" verticalTextPosition="bottom" visible="true" width="41.78125" x="-5.890625" xml:space="preserve" y="5.93359375">Target<y:LabelModel><y:SmartNodeLabelModel distance="4.0"/></y:LabelModel><y:ModelParameter><y:SmartNodeLabelModelParameter labelRatioX="0.0" labelRatioY="0.0" nodeRatioX="0.0" nodeRatioY="0.0" offsetX="0.0" offsetY="0.0" upX="0.0" upY="-1.0"/></y:ModelParameter></y:NodeLabel>
        e_n.appendChild(shape);
        shape.appendChild(label);
        return e_n;
    }

    public static void makeEdge(Document doc, Node source, Node predicate, Node target) {
        makeEdge(doc, source.toString(), predicate.toString(), target.toString());
    }

    public static void makeEdge(Document doc, String sourceId, String predicateLabel, String targetId) {
        Element e_p = doc.createElement("edge");
        e_p.setAttribute("id", predicateLabel.toString() + "#" + System.identityHashCode(new String[]{sourceId, predicateLabel, targetId}));
        e_p.setAttribute("source", sourceId);
        e_p.setAttribute("target", targetId);
        Element e_p_label = doc.createElement("data");
        e_p_label.setAttribute("key", "label");
        e_p_label.setTextContent(predicateLabel);
        e_p.appendChild(e_p_label);
        doc.getElementsByTagName("graph").item(0).appendChild(e_p);

        Element shape = doc.createElement( "y:PolyLineEdge");
        Element label = doc.createElement( "y:EdgeLabel");
        label.setTextContent(predicateLabel);

        e_p.appendChild(shape);
        shape.appendChild(label);
    }

    public static Document toDocument(BasicPattern bp) throws ParserConfigurationException {
        Document doc = createGraphMLDocument();
        makeGraph(doc, Integer.toString(bp.hashCode()));
        int x = 0;
        for(Triple triple : bp) {
            Node s = triple.getSubject();
            Node p = triple.getPredicate();
            Node o = triple.getObject();
            makeNode(doc, s);
            makeNode(doc, o);
            makeEdge(doc,s,p,o);
        }
        return doc;
    }

    private static Element processNode(Document d, FXNode node){
        Element e = makeNode(d, Integer.toString(node.hashCode()), node.getNode().toString() + "\n(" + node.getAnnotation().getTerm().getName().toString() + ")");
        for(FXNode child : node.getChildren()){
            d.getElementsByTagName("graph").item(0).appendChild(processNode(d,child));
            makeEdge(d, Integer.toString(node.hashCode()), "", Integer.toString(child.hashCode()));
        }
        d.getElementsByTagName("graph").item(0).appendChild(e);
        return e;
    }

    public static void main(String[] args) throws IOException, ParserConfigurationException, NotATreeException, TransformerException {
        File easybgpFile= new File(args[0]);
        String fname = easybgpFile.getName().substring(0, easybgpFile.getName().lastIndexOf('.'));
        BasicPattern bp = BGPTestUtils.readBGP(easybgpFile.toURI().toURL());
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        File outf = easybgpFile.getParentFile();
        String outputGraphMLName = new File(outf, fname + ".graphml").getAbsolutePath();
        Document bpd = toDocument(bp);
        DOMSource source = new DOMSource(bpd);
        FileWriter bpfw = new FileWriter(outputGraphMLName);
        StreamResult target = new StreamResult(bpfw);
        transformer.transform(source, target);
        //
        //
        Properties properties = new Properties();
        Analyser a = new AnalyserGrounder(properties, FXModel.getFXModel());
        Set<FXBGPAnnotation> anns = a.annotate(new OpBGP(bp), true);

        int x = 0;
        for(FXBGPAnnotation ann : anns){
            x++;
            String outputFile = new File(outf, fname + "_" + x + ".graphml" ).getAbsolutePath();
            Document d = toDocument(FXTreePattern.make(ann));
            source = new DOMSource(d);
            target = new StreamResult(new FileWriter(outputFile));
            transformer.transform(source, target);
            System.out.println("Generated " + outputFile);
        }
    }
}
