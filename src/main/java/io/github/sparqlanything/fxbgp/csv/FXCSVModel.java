package io.github.sparqlanything.fxbgp.csv;

import io.github.sparqlanything.fxbgp.FX;
import io.github.sparqlanything.fxbgp.FXBGPAnnotation;
import io.github.sparqlanything.fxbgp.FXModel;
import io.github.sparqlanything.fxbgp.FXNodeRule;
import io.github.sparqlanything.fxbgp.FXNodeRuleFactory;
import io.github.sparqlanything.fxrdb.FXRDB;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

/**
 * TODO Not implemented yet
 */
public class FXCSVModel extends FXModel {
    private static FXCSVModel instance = null;

    public final static FXCSVModel getFXCSVModel() {
        if (instance == null) {
            instance = new FXCSVModel();
        }
        return instance;
    }

    @Override
    protected void extend() {
        super.extend();

        /**
         * CSV.1 Type(o) is not allowed
         */
        addInferenceRule(new FXNodeRuleFactory() {
            @Override
            public FXNodeRule make() {
                return new FXNodeRule() {

                    @Override
                    protected boolean when(Node node, FXBGPAnnotation previous) {
                        if (previous.getAnnotation(node).getTerm().equals(FX.Type)) {
                            setFailure();
                            return true;
                        }
                        return false;
                    }
                };
            }
        });

        /**
         * CSV.2 Root(c) can only have slot numbers
         */
        addInferenceRule(new FXNodeRuleFactory() {
            @Override
            public FXNodeRule make() {
                return new FXNodeRule() {
                    @Override
                    protected boolean when(Node node, FXBGPAnnotation previous) {
                        boolean triggers = false;
                        if (previous.getAnnotation(node).getTerm().equals(FX.Root)) {
                            // Get subjects
                            for (Triple t : previous.getOpBGP().getPattern()) {
                                if (t.getObject().equals(node)) {
                                    Node subject = t.getSubject();
                                    for (Triple st : previous.getOpBGP().getPattern()) {
                                        if (st.getSubject().equals(subject) && !st.getObject().equals(node)) {
                                            set(IF.make(previous.getOpBGP(), st.getPredicate(), FX.SlotNumber));
                                            triggers = true;
                                        }
                                    }
                                }
                            }
                        }
                        return triggers;
                    }
                };
            }
        });
        /**
         * CSV.3 First level predicates in nested patterns cannot be SlotString
         */
        addInferenceRule(new FXNodeRuleFactory() {
            @Override
            public FXNodeRule make() {
                return new FXNodeRule() {
                    @Override
                    protected boolean when(Node node, FXBGPAnnotation previous) {
                        if (previous.getAnnotation(node).getTerm().equals(FX.SlotString)) {
                            for (Triple t : previous.getOpBGP().getPattern()) {
                                if(t.getPredicate().equals(node)){
                                   if(previous.getAnnotation(t.getObject()).getTerm().equals(FX.Container)){
                                       // Node cannot be a slot string
                                       setFailure();
                                       return true;
                                   };
                                }
                            }
                        }
                        return false;
                    }
                };
            }
        });
    }
}
