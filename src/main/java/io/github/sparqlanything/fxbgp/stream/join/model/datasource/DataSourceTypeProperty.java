package io.github.sparqlanything.fxbgp.stream.join.model.datasource;

import org.apache.jena.graph.Node;
import org.apache.jena.vocabulary.RDF;

public interface DataSourceTypeProperty extends DataSourceFXElement {

    public static final DataSourceTypeProperty rdfType = new DataSourceTypeProperty() {
        private final Node node = RDF.type.asNode();

        @Override
        public Node asRDFNode() {
            return node;
        }

        @Override
        public String getSurface() {
            return node.getURI();
        }
    };
}
