package io.github.sparqlanything.fxbgp;

import org.apache.jena.sparql.algebra.op.OpBGP;

import java.util.Set;

public interface Analyser {
	/**
	 * Returns all possible grounded annotations of a BGP according to a FX model
	 *
	 * @param bgp
	 * @return
	 */


	default boolean isSatisfiable(OpBGP bgp) {
		return annotate(bgp, false).size()>0;
	}

	default Set<FXBGPAnnotation> annotate(OpBGP bgp){
		return annotate(bgp, true);
	}

	Set<FXBGPAnnotation> annotate(OpBGP bgp, boolean complete);

}
