package io.github.sparqlanything.fxbgp.stream;

import org.apache.jena.graph.Node;

import java.util.List;

/**
 * Read-only access to the stream-position state maintained by
 * {@link FXTreeSolutionBuilder} during a streaming parse.
 *
 * <p>A {@link Matching} object holds a reference to its owning builder through this
 * interface rather than a raw {@link List} live-view. This makes the live-access
 * contract explicit and decouples {@code Matching} from its builder, enabling
 * independent testing and parallel evaluation of multiple builders (Option I).</p>
 *
 * <p><strong>Contract:</strong> both methods reflect the <em>current</em> stream
 * position at the moment of the call. Callers must not retain the return value of
 * {@link #currentPath()} across events; its contents change as the stream advances.</p>
 *
 * <p>The only intended implementor is {@link FXTreeSolutionBuilder}.</p>
 */
public interface PathAccessor {

    /**
     * Returns an unmodifiable live view of the node path from the stream root to the
     * most recently entered node. The list length equals the current nesting depth in
     * the stream.
     *
     * <p>Used by {@link Matching} in two ways:</p>
     * <ul>
     *   <li>{@code new ArrayList<>(currentPath())} — takes an immutable snapshot of the
     *       current path when a new cursor match is recorded via {@code Matching.set()}.</li>
     *   <li>Indirectly, through {@link #currentPrefixHash()} and {@link #currentFullHash()},
     *       which replace all O(depth) {@link List#equals} comparisons with O(1) hash checks.</li>
     * </ul>
     *
     * @return unmodifiable live view of the current path; never {@code null};
     *         empty when no container has been entered yet
     */
    List<Node> currentPath();

    /**
     * Returns the polynomial rolling hash of the path prefix
     * {@code currentPath().subList(0, currentPath().size() - 1)}, i.e. the hash of all
     * path nodes <em>except</em> the last one.
     *
     * <p>This value is used in {@link Matching#check} as an O(1) pre-filter before the
     * full {@link List#equals} comparison of a stored path snapshot against the current
     * prefix. A hash mismatch is a definitive rejection; a hash match triggers the full
     * comparison to guard against collisions.</p>
     *
     * <p>The hash is maintained incrementally by {@link FXTreeSolutionBuilder} using
     * a path-hash stack. Each push appends
     * {@code previousHash * PRIME + node.hashCode()}; each pop discards the top entry.
     * This makes both push and pop O(1) and gives O(1) access to the prefix hash at any
     * depth without recomputing from scratch.</p>
     *
     * <p><strong>Precondition:</strong> {@code currentPath().size() >= 1}. When the path
     * has exactly one element the prefix is empty and this method returns {@code 0L}
     * (the base sentinel value at the bottom of the hash stack). It must not be called
     * when the path is empty.</p>
     *
     * @return polynomial hash of {@code currentPath()[0..size-2]};
     *         {@code 0L} when {@code currentPath().size() == 1}
     * @throws IllegalStateException if called when {@code currentPath()} is empty
     */
    long currentPrefixHash();

    /**
     * Returns the number of nodes currently on the path, i.e.
     * {@code currentPath().size()}, without allocating a list wrapper.
     *
     * <p>Used in {@link Matching#check} as a cheap O(1) depth filter before
     * the hash comparison, replacing the previous
     * {@code accessor.currentPath().size()} call which allocated a transient
     * {@link java.util.Collections.UnmodifiableList} wrapper on every
     * invocation.</p>
     *
     * @return current path length; {@code 0} when no node has been pushed
     */
    int currentDepth();

    /**
     * Returns a fresh mutable snapshot of the current path, equivalent to
     * {@code new ArrayList<>(currentPath())} but without the intermediate
     * unmodifiable wrapper allocation.
     *
     * <p>Used wherever {@link Matching} needs to record the path position at
     * which a cursor was matched ({@code Matching.set()} call sites).</p>
     *
     * @return new {@link java.util.ArrayList} containing the current path nodes
     */
    List<Node> copyCurrentPath();

    /**
     * Returns the polynomial rolling hash of the <em>full</em> current path,
     * i.e. {@code currentPath()[0..size-1]}.
     *
     * <p>Used in {@link Matching#endContainer()} as an O(1) replacement for
     * {@code map.get(c).equals(currentPath())}: the stored hash in {@code hashMap}
     * is compared against this value to detect whether the container being closed
     * is the one the cursor is positioned on.</p>
     *
     * <p><strong>Precondition:</strong> {@code currentPath().size() >= 1}.</p>
     *
     * @return polynomial hash of all nodes in the current path
     * @throws IllegalStateException if called when {@code currentPath()} is empty
     */
    long currentFullHash();
//
//    /**
//     * To verify if a slot was already received on this container.
//     *
//     * @param container
//     * @param slot
//     * @return
//     */
//    boolean rememberSlot(Node container, Node slot, FX slotTerm);
//
//    /**
//     * To verify if a type was already received on this container.
//     *
//     * @param container
//     * @param type
//     * @return
//     */
//    boolean rememberType(Node container, Node type);
//
//    /**
//     * We remove the container and its data from the cached types and slots
//     */
//    void forget(Node container);
//
//    boolean visited(Node container, Node what, FX term);
}
