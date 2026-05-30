package io.github.sparqlanything.fxbgp.stream.join.model.datasource;

import io.github.sparqlanything.fxbgp.stream.join.model.FXElement;
import io.github.sparqlanything.model.Triplifier;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

public interface DataSourceFXRoot extends DataSourceFXElement {

    public static final DataSourceFXRoot fxRoot = new DataSourceFXRoot() {
        private static final Node node = NodeFactory.createURI(Triplifier.FACADE_X_TYPE_ROOT);

        @Override
        public Node asNode() {
            return node;
        }

        @Override
        public boolean matches(FXElement o) {
            return o.asNode().equals(node);
        }

        @Override
        public String getSurface() {
            return Triplifier.FACADE_X_TYPE_ROOT;
        }

        @Override
        public int compareTo(FXElement e) {
            return getSurface().compareTo(e.getSurface());
        }

    } ;
}
