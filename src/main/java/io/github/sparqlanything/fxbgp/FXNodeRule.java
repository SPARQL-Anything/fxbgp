package io.github.sparqlanything.fxbgp;
import org.apache.jena.graph.Node;

public abstract class FXNodeRule implements FXRule {
	private FXNodeAnnotation annotation = null;
	private FXNodeAnnotation lastAnnotation = null;
	private Boolean failure = null;
	protected FXNodeRule(){
	}
	protected abstract boolean when(Node node, FXBGPAnnotation previous);
	public FXNodeAnnotation infer(){
		return annotation;

	}
	public boolean resolved(){
		return annotation != null || failure != null;
	}
	protected void set(FXNodeAnnotation outcome){
//		clean();
		failure = false;
		if(outcome == null){
			throw new RuntimeException("This should not happen");
		}
		this.annotation = outcome;
	}

	protected void setFailure(){
//		clean();
		failure = true;
	}

	private void clean(){
		annotation = null;
	}

	public boolean failure(){
		return failure == null ? false : failure;
	}

}
