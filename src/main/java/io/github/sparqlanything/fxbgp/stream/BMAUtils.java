package io.github.sparqlanything.fxbgp.stream;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class BMAUtils {

    public static boolean singleRoot(BasicPattern bp) {
        Set<Node> subjects = new HashSet<>();
        Set<Node> objects = new HashSet<>();

        for (Triple t : bp.getList()) {
            subjects.add(t.getSubject());
            objects.add(t.getObject());
        }

        Set<Node> roots = new HashSet<>(subjects);
        roots.removeAll(objects);

        return roots.size() == 1;
    }

    public static boolean connected(BasicPattern bp) {
        if (bp.getList().isEmpty()) {
            return true;
        }

        // Build undirected adjacency map (predicates are edge labels, not nodes)
        Map<Node, Set<Node>> adjacency = new HashMap<>();
        for (Triple t : bp.getList()) {
            Node s = t.getSubject();
            Node o = t.getObject();
            adjacency.computeIfAbsent(s, k -> new HashSet<>()).add(o);
            adjacency.computeIfAbsent(o, k -> new HashSet<>()).add(s);
        }

        // BFS from an arbitrary start node
        Set<Node> allNodes = adjacency.keySet();
        Set<Node> visited = new HashSet<>();
        Queue<Node> queue = new LinkedList<>();
        Node start = allNodes.iterator().next();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            for (Node neighbor : adjacency.get(current)) {
                if (visited.add(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }

        return visited.size() == allNodes.size();
    }

    public static int varInPSize(BasicPattern bp) {
        if (bp.getList().isEmpty()) {
            return 0;
        }

        // Build directed graph: subject -> set of reachable objects
        Map<Node, Set<Node>> children = new HashMap<>();
        Set<Node> allNodes = new HashSet<>();
        for (Triple t : bp.getList()) {
            Node s = t.getSubject();
            Node o = t.getObject();
            children.computeIfAbsent(s, k -> new HashSet<>()).add(o);
            allNodes.add(s);
            allNodes.add(o);
        }

        // For each node, BFS to collect its subtree, then count distinct var-predicates
        int max = 0;
        for (Node start : allNodes) {
            Set<Node> subtree = new HashSet<>();
            Queue<Node> queue = new LinkedList<>();
            queue.add(start);
            subtree.add(start);
            while (!queue.isEmpty()) {
                Node current = queue.poll();
                for (Node child : children.getOrDefault(current, Collections.emptySet())) {
                    if (subtree.add(child)) {
                        queue.add(child);
                    }
                }
            }

            Set<Node> varPreds = new HashSet<>();
            for (Triple t : bp.getList()) {
                if (subtree.contains(t.getSubject()) && t.getPredicate().isVariable()) {
                    varPreds.add(t.getPredicate());
                }
            }

            max = Math.max(max, varPreds.size());
        }

        return max;
    }

    public static boolean isBMA(BasicPattern p){
        return singleRoot(p) && varInPSize(p) <= 1;
    }
}
