package io.github.sparqlanything.fxbgp;
import org.apache.jena.graph.Node;

public abstract class FXNodeRule implements FXRule {
	private FXNodeAnnotation interpretation = null;
	private Boolean failure = null;
	protected FXNodeRule(){
	}
	protected abstract boolean when(Node node, FXBGPAnnotation previous);
	public FXNodeAnnotation infer(){
		FXNodeAnnotation toReturn = interpretation;
		clean();
		return toReturn;

	}
	public boolean resolved(){
		return interpretation != null || failure != null;
	}
	protected void set(FXNodeAnnotation outcome){
		failure = false;
		this.interpretation = outcome;
	}

	protected void setFailure(){
		failure = true;
		this.interpretation = null;
	}

	private void clean(){
		interpretation = null;
	}
	public boolean failure(){
		return failure == null ? false : failure;
	}
}
