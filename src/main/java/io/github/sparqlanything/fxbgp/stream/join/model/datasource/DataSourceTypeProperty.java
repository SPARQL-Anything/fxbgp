package io.github.sparqlanything.fxbgp.stream.join.model.datasource;

import io.github.sparqlanything.fxbgp.stream.join.model.FXElement;
import org.apache.jena.graph.Node;
import org.apache.jena.vocabulary.RDF;

public interface DataSourceTypeProperty extends DataSourceFXElement {

    public static final DataSourceTypeProperty rdfType = new DataSourceTypeProperty() {
        private final Node node = RDF.type.asNode();

        @Override
        public Node asNode() {
            return node;
        }

        @Override
        public boolean matches(FXElement o) {
            return o.asNode().equals(RDF.type.asNode());
        }

        @Override
        public String getSurface() {
            return node.getURI();
        }

        @Override
        public int compareTo(FXElement e) {
            return getSurface().compareTo(e.getSurface());
        }
    };
}
