package io.github.sparqlanything.fxbgp;
import org.apache.jena.graph.Node;

public abstract class FXNodeRule implements FXRule {
	private FXNodeAnnotation annotation = null;
	private Boolean failure = null;
	protected FXNodeRule(){
	}
	protected abstract boolean when(Node node, FXBGPAnnotation previous);
	public FXNodeAnnotation infer(){
		FXNodeAnnotation toReturn = annotation;
		clean();
		return toReturn;

	}
	public boolean resolved(){
		return annotation != null || failure != null;
	}
	protected void set(FXNodeAnnotation outcome){
		failure = false;
		this.annotation = outcome;
	}

	protected void setFailure(){
		failure = true;
		this.annotation = null;
	}

	private void clean(){
		annotation = null;
	}
	public boolean failure(){
		return failure == null ? false : failure;
	}
}
