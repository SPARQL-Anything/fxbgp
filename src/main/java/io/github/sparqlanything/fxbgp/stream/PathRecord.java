package io.github.sparqlanything.fxbgp.stream;

import org.apache.jena.graph.Node;

import java.util.List;

// ---------------------------------------------------------------------------
// PathRecord — bundles the path snapshot and its precomputed hash.
// equals/hashCode delegate to path only so Matching.equals/hashCode
// preserve their original semantics when operating on recordMap directly.
// ---------------------------------------------------------------------------
public final class PathRecord {
    final List<Node> path;
    final long hash;
    final Node node;

    PathRecord(List<Node> path, long hash) {
        this.path = path;
        this.hash = hash;
        this.node = path.getLast();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PathRecord) return path.equals(((PathRecord) obj).path);
        return false;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(hash);
    }

    public Node getNode() {
        return node;
    }
}
