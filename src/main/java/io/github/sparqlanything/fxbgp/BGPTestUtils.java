package io.github.sparqlanything.fxbgp;

import org.apache.commons.io.IOUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BGPTestUtils {
    public static final Logger L = LoggerFactory.getLogger(BGPTestUtils.class);



    public static URL getEasyBGP(String easyBgpFileName) throws IOException {
        URL url = BGPTestUtils.class.getClassLoader().getResource("./" + easyBgpFileName + ".easybgp");
        L.trace("easy bgp: {}", url);
        return url;
    }

    public static String nodeToString(Node n){
        if(n.isURI()){
            return "<" + n.getURI() + ">";
        }
        if(n.isVariable()){
            return "?" + n.getName();
        }
        if(n.isLiteral()){
            return "\"" + n.getLiteral().getLexicalForm() +"\"" ;
        }
        return n.toString();
    }

    public static String basicPatternToString(BasicPattern bp){
        StringBuilder sb = new StringBuilder();
        for(Triple t: bp.getList()){
            sb.append(nodeToString(t.getSubject()));
            sb.append(" ");
            sb.append(nodeToString(t.getPredicate()));
            sb.append(" ");
            sb.append(nodeToString(t.getObject()));
            sb.append("\n");
        }
        return sb.toString();
    }

    public static Triple t(Node s, Node p, Node o) {
        return Triple.create(s, p, o);
    }

    public static Node v(String v) {
        return Var.alloc(v);
    }

    public static Node u(String v) {
        return NodeFactory.createURI(v);
    }

    public static Node b(String v) {
        return NodeFactory.createBlankNode(v);
    }

    public static Node b() {
        return NodeFactory.createBlankNode();
    }

    public static Node l(Object o) {
        return ResourceFactory.createTypedLiteral(o).asNode();
    }

    public static BasicPattern readBGP(String easyBGPfileName) throws IOException {
        return readBGP(getEasyBGP(easyBGPfileName));
    }

    public static BasicPattern readBGP(URL url) throws IOException {
        BasicPattern bp = new BasicPattern();
//		L.info("{}", easyBgpFile);
        String sBGP = IOUtils.toString(url, StandardCharsets.UTF_8);
//		L.trace("sBGP: {}", sBGP);
        String[] lines = sBGP.split("\n");
//		L.trace("lines: {} {}", lines,lines.length);
        for(String line : lines){
//			L.trace("line: {}", line);
            List<Node> nodes = new ArrayList<Node>();
            String[] tr = line.split(" ");
            Triple t = null;
            boolean load = false;
            StringBuilder loaded = null;
            for (int c = 0; c<tr.length; c++) {
                if(load && tr[c].endsWith("\"")){
                    if(load){
                        loaded.append(" ");
                        loaded.append(tr[c].substring(0,tr[c].length()-1));
                        nodes.add(l(loaded.toString()));
                        loaded = null;
                    }
                }else
                if(!load && tr[c].trim().startsWith("\"") && !tr[c].trim().endsWith("\"")){
                    loaded = new StringBuilder(tr[c].substring(1,tr[c].length()));
                    load = true;
                }else if(load){
                    loaded.append(" ");
                    loaded.append(tr[c]);
                    continue;
                }else if(tr[c].trim().endsWith("\"") && tr[c].trim().startsWith("\"")){
                    nodes.add(l(tr[c].trim().substring(1,tr[c].trim().length()-1)));
                }else
                if(tr[c].trim().startsWith("<")){
                    nodes.add(u(tr[c].trim().substring(1, tr[c].trim().length()-1)));
                }else
                if(tr[c].trim().startsWith("?")){
                    nodes.add(v(tr[c].trim().substring(1)));
                }else
                if(tr[c].trim().startsWith("_:")){
                    nodes.add(b(tr[c].trim().substring(2)));
                }else
                if(tr[c].trim().equals("a")){
                    nodes.add(u(RDF.type.getURI()));
                }else{
                    // other
                    nodes.add(v(tr[c].trim()));
                }
            }
            if(nodes.size() != 3) throw new RuntimeException("Wrong number of nodes");
            t = Triple.create(nodes.get(0),
                    nodes.get(1),
                    nodes.get(2));
            bp.add(t);
        }
        //L.trace("BGP: \n{}\n",bp);
        return bp;
    }
}
