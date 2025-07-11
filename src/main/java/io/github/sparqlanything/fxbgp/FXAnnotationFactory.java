package io.github.sparqlanything.fxbgp;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.op.OpBGP;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class FXAnnotationFactory {
	final FXModel FXM;
	FXAnnotationFactory(FXModel model){
		FXM = model;
	}
	public FXNodeAnnotation make(final OpBGP bgp, final Node node, final FX element){
		return new OfNode(bgp,node,element);
	}

	/**
	 * This method generates a different interpretation from an existing one.
	 * It keeps node and bgp but modifies the FX element.
	 *
	 * @param interpretation
	 * @param element
	 * @return
	 */
	public FXNodeAnnotation makeFrom(final FXNodeAnnotation interpretation, final FX element){
		return make(interpretation.getOpBGP(), interpretation.getNode(), element);
	}

	/**
	 * This method generates a starting interpretation of a BGP.
	 * All nodes are interpreated as Subject, Predicate, or Object.
	 *
	 * @param bgp
	 * @return
	 */
	public FXBGPAnnotation make(final OpBGP bgp){
		final Map<Node, FXNodeAnnotation> nodeInderpretations = new HashMap<>();
		return new OfBGP(bgp);
	}


	public FXBGPAnnotation make(FXBGPAnnotation previous, Node n, FX element){
		return make(previous, make(previous.getOpBGP(), n, element));
	}

	/**
	 * We make a new interpretation of a BGP by setting the interpretation of a single node.
	 * We don't do any verification (consistency).
	 *
	 * @param previous
	 * @param newInterpretation
	 * @return
	 */
	public FXBGPAnnotation make(final FXBGPAnnotation previous, FXNodeAnnotation newInterpretation){
		return new OfBGP(previous, newInterpretation);
	}

	public FXBGPAnnotation make(OpBGP bgp, Set<FXNodeAnnotation> interpretations){
		return new OfBGP(bgp, interpretations);
	}

	private class OfBGP implements FXBGPAnnotation {
		final Map<Node, FXNodeAnnotation> nodeInderpretations = new HashMap<>();
		boolean isGrounded = false;
		FXBGPAnnotation previous = null;
		private OpBGP bgp = null;
		private int hashCode;
		OfBGP(OpBGP bgp){
			this.bgp = bgp;
			for(Triple t: bgp.getPattern().getList()){
				if(!nodeInderpretations.containsKey(t.getSubject())) {
					nodeInderpretations.put(t.getSubject(), make(bgp, t.getSubject(), FX.Subject));
				}
				if(!nodeInderpretations.containsKey(t.getPredicate())) {
					nodeInderpretations.put(t.getPredicate(), make(bgp, t.getPredicate(), FX.Predicate));
				}
				if(!nodeInderpretations.containsKey(t.getObject())) {
					nodeInderpretations.put(t.getObject(), make(bgp, t.getObject(), FX.Object));
				}
			}
			hashCode = Objects.hash(bgp,nodeInderpretations);
		}

		OfBGP(OpBGP bgp, Set<FXNodeAnnotation> nodeInderpretations){
			this.bgp = bgp;
			this.isGrounded = true;
			for (FXNodeAnnotation n: nodeInderpretations){
				if(isGrounded && !n.isGrounded()){
					this.isGrounded = false;
				}
				this.nodeInderpretations.put(n.getNode(),n);
			}
			hashCode = Objects.hash(bgp,nodeInderpretations);
		}

		OfBGP(final FXBGPAnnotation previous, FXNodeAnnotation newInterpretation){
			this.previous = previous;
			this.bgp = previous.getOpBGP();
			// Inherit all previous interpretations
			nodeInderpretations.putAll(previous.getInterpretationOfNodes());
			// ... except for this node
			nodeInderpretations.put(newInterpretation.getNode(),newInterpretation);
			// Compute if this is grounded
			isGrounded = true;
			for(Map.Entry<Node, FXNodeAnnotation> ion: nodeInderpretations.entrySet()){
				if(!ion.getValue().isGrounded()){
					isGrounded = false;
					break;
				}
			}

			HashCodeBuilder hcb = new HashCodeBuilder();
			hashCode = hcb.append(bgp).append(nodeInderpretations).toHashCode();
		}

		@Override
		public Map<Node, FXNodeAnnotation>  getInterpretationOfNodes() {
			return Collections.unmodifiableMap(nodeInderpretations);
		}

		@Override
		public boolean isGrounded() {
			return isGrounded;
		}

		@Override
		public boolean isStart() {
			return previous() == null;
		}

		@Override
		public FXNodeAnnotation getInterpretation(Node node) {
			return nodeInderpretations.get(node);
		}

		@Override
		public FXBGPAnnotation previous() {
			return previous;
		}

		@Override
		public OpBGP getOpBGP() {
			return bgp;
		}

		@Override
		public Set<Node> nodes() {
			return Collections.unmodifiableSet(nodeInderpretations.keySet());
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof FXBGPAnnotation){
				boolean samebgp = ((FXBGPAnnotation)obj).getOpBGP().getPattern().equals(this.getOpBGP().getPattern());
				boolean sameint = ((FXBGPAnnotation)obj).getInterpretationOfNodes().equals(this.getInterpretationOfNodes());
				return sameint && samebgp;
			}
			return false;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(Integer.toString(hashCode())).append("@( ");
			for(Triple t: getOpBGP().getPattern()){
					sb.append(getInterpretation(t.getSubject()))
					.append(getInterpretation(t.getPredicate()))
					.append(getInterpretation(t.getObject())).append(" . ");
			}
			return sb.append(" ) ").toString();
		}

		@Override
		public int hashCode() {
			return hashCode;
		}
	}

	private class OfNode implements FXNodeAnnotation {
		FX element;
		Node node;
		OpBGP bgp;
		OfNode(OpBGP bgp, Node node, FX element){
			this.node = node;
			this.bgp = bgp;
			this.element = element;
		}
		@Override
		public boolean consistentWith(FXNodeAnnotation node) {
			return false;
		}

		@Override
		public FX getTerm() {
			return element;
		}

		@Override
		public Node getNode() {
			return node;
		}

		@Override
		public boolean isGrounded() {
			return FXM.isGrounded(element);
		}

		@Override
		public OpBGP getOpBGP() {
			return bgp;
		}

		@Override
		public String toString() {
			return "[" + node.toString() + " as " + element.getName() + "]";
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof OfNode){
				OfNode on = (OfNode) obj;
				return on.getNode().equals(getNode()) && on.getOpBGP().equals(getOpBGP()) && on.getTerm().equals(getTerm());
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Objects.hash(element, node, bgp);
		}
	}
}
