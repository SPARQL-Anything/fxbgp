package io.github.sparqlanything.fxbgp;

import com.google.common.collect.Sets;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * TODO maybe move this as a method of FXModel
 */
public class AnalyserGrounder implements Analyser {

	private static final Logger L = LoggerFactory.getLogger(AnalyserGrounder.class);
	private FXModel FXM;
	private Properties properties;

	public AnalyserGrounder(Properties properties, FXModel model){
		this.FXM = model;
		this.properties = properties;
	}

	public Set<FXBGPAnnotation> annotate(OpBGP bgp, boolean complete) {
		// Precondition...

		// No cycles are allowed
		if(FXM.hasCycle(bgp)){
			return Collections.emptySet();
		}

		// For any triple, s, p, o are all different
		for(Triple t : bgp.getPattern()){
			if(t.getSubject().equals(t.getObject()) ||
				t.getSubject().equals(t.getPredicate()) ||
				t.getObject().equals(t.getPredicate()) ){
				return Collections.emptySet();
			}
		}

		// Generate all possible grounded annotations
		Set<FX> subjectTerms = FXM.groundedSpecialisations(FX.Subject);
		L.debug("subject terms: {}", subjectTerms);
		Set<FX> predicateTerms = FXM.groundedSpecialisations(FX.Predicate);
		L.debug("predicate terms: {}", predicateTerms);
		Set<FX> objectTerms = FXM.groundedSpecialisations(FX.Object);
		L.debug("object terms: {}", objectTerms);

		Set<Node> nodes = new HashSet<>();
		Map<Node,Set<FXNodeAnnotation>> possible = new HashMap<>();
		for(Triple t :bgp.getPattern().getList()){
			nodes.add(t.getSubject());
			if(!possible.containsKey(t.getSubject())){
				possible.put(t.getSubject(), new HashSet<>());
			}
			for(FX subjectTerm : subjectTerms){
				possible.get(t.getSubject()).add(FXM.getIF().make(bgp, t.getSubject(), subjectTerm));
			}

			nodes.add(t.getPredicate());
			if(!possible.containsKey(t.getPredicate())){
				possible.put(t.getPredicate(), new HashSet<>());
			}
            // We can filter the possible terms for predicates that are not variables
            if(t.getPredicate().isVariable()){
                for(FX predicateTerm : predicateTerms){
                    possible.get(t.getPredicate()).add(FXM.getIF().make(bgp, t.getPredicate(), predicateTerm));
                }
            }else{
                if(t.getPredicate().equals(RDF.type.asNode())){
                    possible.get(t.getPredicate()).add(FXM.getIF().make(bgp, t.getPredicate(), FX.TypeProperty));
                }else if(t.getPredicate().getURI().startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#_")){
                    possible.get(t.getPredicate()).add(FXM.getIF().make(bgp, t.getPredicate(), FX.SlotNumber));
                }else{
                    possible.get(t.getPredicate()).add(FXM.getIF().make(bgp, t.getPredicate(), FX.SlotString));
                }
            }
			nodes.add(t.getObject());
			if(!possible.containsKey(t.getObject())){
				possible.put(t.getObject(), new HashSet<>());
			}
			for(FX objectTerm : objectTerms){
				possible.get(t.getObject()).add(FXM.getIF().make(bgp, t.getObject(), objectTerm));
			}

			// For each node, if possible annotations include both Predicate and either Subject or Object, there is no solution
			if(possible.get(t.getSubject()).contains(FXM.getIF().make(bgp, t.getSubject(), FX.TypeProperty)) ||
				possible.get(t.getObject()).contains(FXM.getIF().make(bgp, t.getObject(), FX.TypeProperty)) ||
				possible.get(t.getPredicate()).contains(FXM.getIF().make(bgp, t.getPredicate(), FX.Container)) ){
				return Collections.emptySet();
			}
		}
        // We reduce the number of combinations with triple-level rules
        for(Triple t : bgp.getPattern()){
            // If a node is a subject, it can only be a container
            possible.get(t.getSubject()).removeIf(i -> !i.getTerm().equals(FX.Container));
            // If a predicate is concrete, the object can be determined
            if(t.getPredicate().equals(RDF.type.asNode())){
                possible.get(t.getObject()).removeIf(i -> i.getTerm().equals(FX.Container) || i.getTerm().equals(FX.Value));
            }else if(t.getPredicate().isURI() && t.getPredicate().getURI().startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#_")){
                possible.get(t.getObject()).removeIf(i -> i.getTerm().equals(FX.Type) || i.getTerm().equals(FX.Root));
            }else if(t.getPredicate().isURI()){
                possible.get(t.getObject()).removeIf(i -> i.getTerm().equals(FX.Type) || i.getTerm().equals(FX.Root));
            }
        }

		if(L.isTraceEnabled()) {
			for (Map.Entry<Node, Set<FXNodeAnnotation>> entry : possible.entrySet()) {
				L.trace(" -- possible {} > {} -- ", entry.getKey(), entry.getValue());
			}
		}
		List<Set<FXNodeAnnotation>> input = new ArrayList<>();
		input.addAll(possible.values());

		Set<List<FXNodeAnnotation>> output = Sets.cartesianProduct(input.toArray(new Set[input.size()]));
		L.debug("possible BGP annotation hypotheses: {}", output.size());
		lastTestedHypotheses = output.size();
		Set<FXBGPAnnotation> results = new HashSet<>();
		// Check if any is consistent and discard the rest
		for(List<FXNodeAnnotation> list: output){
			FXBGPAnnotation nibgp = FXM.getIF().make(bgp, new HashSet<>(list));
			// They must be all grounded
			if(!nibgp.isGrounded()){
				// We leave this here in case we mess up...
				throw new RuntimeException(nibgp + " is not grounded!");
			}
			if(FXM.isConsistent(nibgp)){
				L.trace(" -- pass > {} -- ",  nibgp);
				results.add(nibgp);
			}else{
				L.trace(" -- fail > {} -- ",  nibgp);
				// discard hypothesis
			}
			if(!complete && results.size()>0){
				return results;
			}
			// End for each hypothesised specialisation
		}
		return results;
	}
	public int getLastTestedHypotheses(){
		return lastTestedHypotheses;
	}
	private int lastTestedHypotheses = 0;
}
