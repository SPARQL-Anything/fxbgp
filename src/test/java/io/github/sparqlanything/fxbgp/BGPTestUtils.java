package io.github.sparqlanything.fxbgp;

import org.apache.commons.io.IOUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.BasicPattern;
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

    public static Triple t(Node s, Node p, Node o) {
        return Triple.create(s, p, o);
    }

    public static Node v(String v) {
        return NodeFactory.createVariable(v);
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
            for (int c = 0; c<3; c++) {
                if(tr[c].trim().startsWith("<")){
                    nodes.add(u(tr[c].trim().substring(1, tr[c].trim().length()-1)));
                }else
                if(tr[c].trim().startsWith("?")){
                    nodes.add(v(tr[c].trim().substring(1)));
                }else
                if(tr[c].trim().startsWith("_:")){
                    nodes.add(b(tr[c].trim().substring(2)));
                }else
                if(tr[c].trim().startsWith("\"")){
                    nodes.add(l(tr[c].trim().substring(1,tr[c].trim().length()-1)));
                }else
                if(tr[c].trim().equals("a")){
                    nodes.add(u(RDF.type.getURI()));
                }else{
                    // other
                    nodes.add(v(tr[c].trim()));
                }
            }
            t = Triple.create(nodes.get(0),
                    nodes.get(1),
                    nodes.get(2));
            bp.add(t);
        }
        L.trace("BGP: \n{}\n",bp);
        return bp;
    }
}
