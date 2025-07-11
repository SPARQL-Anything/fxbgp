package io.github.sparqlanything.fxbgp;

import org.apache.jena.graph.Node;

public interface FXNodeAnnotation extends FXAnnotation {
	boolean consistentWith(FXNodeAnnotation node);

	FX getTerm();

	Node getNode();

	boolean isGrounded();

}
