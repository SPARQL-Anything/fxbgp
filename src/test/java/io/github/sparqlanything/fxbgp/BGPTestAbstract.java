package io.github.sparqlanything.fxbgp;


import io.github.sparqlanything.jdbc.JDBC;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.core.BasicPattern;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public abstract class BGPTestAbstract extends BGPTestUtils{
	final protected static Logger L = LoggerFactory.getLogger(BGPTestAbstract.class);

	protected Properties properties = null;
	protected BasicPattern bp = null;
	private FXModel FXM = null;
	protected Analyser ANA = null;
	private Set<FXBGPAnnotation> annotations = null;

	public BGPTestAbstract(FXModel FXM){
		this.FXM = FXM;
	}

	protected FXModel FXM(){
		return FXM;
	}
	protected void setAnalyser(Analyser analyser){
		ANA = analyser;
	}

	protected void runAnalyser(){
		annotations = ANA.annotate(new OpBGP(bp()));
	}
	protected Set<FXBGPAnnotation> annotations(){
		if(annotations == null){
			runAnalyser();
		}
		return annotations;
	}

	protected FXAnnotationFactory IF(){
		return FXM().getIF();
	}

	protected BasicPattern bp(){
		return bp;
	}

	protected OpBGP opBGP(){
		return new OpBGP(bp());
	}

	/**
	 * Extend to configure properties for the concrete tests
	 */
	protected void properties(){}
	@Before
	public void before(){
		bp = new BasicPattern();
		properties = new Properties();
		properties();
	}

	protected void add(Node s, Node p, Node o){
		bp().add(t(s,p,o));
	}

	protected void add(BasicPattern bgp){
		bp().addAll(bgp);
	}

	protected void add(Triple triple){
		bp().add(triple);
	}


	protected Node xyz(String localName){
		return NodeFactory.createURI(JDBC.getNamespace(properties) + localName);
	}

	/**
	 * @param easyBgpFileName
	 * @throws IOException
	 */
	protected void loadBGP(String easyBgpFileName) throws IOException {
		loadBGP(BGPTestUtils.getEasyBGP(easyBgpFileName));
	}

	protected void loadBGP(URL url) throws IOException {
        BasicPattern bp = BGPTestUtils.readBGP(url);
		L.trace("BGP: \n{}\n",bp);
		add(bp);
	}

	/**
	 * Caller to make sure all nodes are interpreted
	 *
	 * @param pairs Node, FX, Node, FX, ...
	 * @return
	 */
	protected FXBGPAnnotation make(Object... pairs){
		Set<FXNodeAnnotation> set = new HashSet<>();
		for(int x = 0; x<pairs.length; x=x+2){
			Object n = pairs[x];
			Object t = pairs[x+1];
			set.add(IF().make(opBGP(),(Node) n, (FX) t));
		}
		return IF().make(opBGP(), set);
	}
}
