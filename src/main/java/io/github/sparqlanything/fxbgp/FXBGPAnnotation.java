package io.github.sparqlanything.fxbgp;

import org.apache.jena.graph.Node;

import java.util.Map;
import java.util.Set;

public interface FXBGPAnnotation extends FXAnnotation {
	Map<Node, FXNodeAnnotation> getAnnotationOfNodes();

	public boolean isGrounded();

	public boolean isStart();

	public FXNodeAnnotation getAnnotation(Node node);

	public FXBGPAnnotation previous();

	public Set<Node> nodes();
}
