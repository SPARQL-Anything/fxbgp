package io.github.sparqlanything.fxbgp.stream;

import org.apache.jena.graph.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mutable implementation of {@link PathAccessor} owned by {@link FXProxyEventListener}.
 *
 * <p>A single instance is shared across all {@link FXQuerySolutionBuilder} listeners for a
 * given query execution. The proxy drives {@link #push}/{@link #pop}/{@link #reset} in its
 * event handlers; builders receive only the read-only {@link PathAccessor} view and never
 * mutate the path themselves.</p>
 *
 * <p>This means path computation and hash maintenance happens once per event regardless of
 * how many builders are registered, instead of once per builder per event.</p>
 *
 * <p><strong>Thread safety:</strong> mutations ({@code push}, {@code pop}, {@code reset})
 * are called exclusively by the main (parser) thread, always outside a {@code fanOut} window.
 * Reads ({@code currentPath}, {@code currentPrefixHash}, {@code currentFullHash}) are called
 * by worker threads inside {@code fanOut}. The {@link java.util.concurrent.CyclicBarrier}
 * used by the proxy establishes the required happens-before relationship.</p>
 */
public class SharedPathAccessor implements PathAccessor {

    private static final long HASH_PRIME = 1_000_000_007L;

    private final List<Node> path     = new ArrayList<>();
    private final List<Long> hashStack = new ArrayList<>();

    public SharedPathAccessor() {
        reset();
    }

    /** Resets to the empty-path state, ready for a new data source. */
    void reset() {
        path.clear();
        hashStack.clear();
        hashStack.add(0L);   // sentinel: hash of empty prefix
    }

    /** Appends {@code node} to the path and extends the rolling hash. */
    void push(Node node) {
        path.add(node);
        long prev = hashStack.get(hashStack.size() - 1);
        hashStack.add(prev * HASH_PRIME + node.hashCode());
    }

    /** Removes the last node from the path and discards its hash entry. */
    void pop() {
        path.remove(path.size() - 1);
        hashStack.remove(hashStack.size() - 1);
    }

    /** Returns {@code true} when no node has been pushed since the last {@link #reset()}. */
    boolean isEmpty() {
        return path.isEmpty();
    }

    @Override
    public List<Node> currentPath() {
        return Collections.unmodifiableList(path);
    }

    @Override
    public int currentDepth() {
        return path.size();
    }

    @Override
    public List<Node> copyCurrentPath() {
        return new ArrayList<>(path);
    }

    @Override
    public long currentPrefixHash() {
        if (hashStack.size() < 2) {
            throw new IllegalStateException("currentPrefixHash() called with empty path");
        }
        return hashStack.get(hashStack.size() - 2);
    }

    @Override
    public long currentFullHash() {
        if (hashStack.size() < 2) {
            throw new IllegalStateException("currentFullHash() called with empty path");
        }
        return hashStack.get(hashStack.size() - 1);
    }
}
