package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.fxbgp.FX;
import org.apache.jena.graph.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class Matching {
    private static final Logger L = LoggerFactory.getLogger(Matching.class);
    private static final long HASH_PRIME = 1_000_000_007L;

    // ---------------------------------------------------------------------------
    // PathRecord — bundles the path snapshot and its precomputed hash.
    // equals/hashCode delegate to path only so Matching.equals/hashCode
    // preserve their original semantics when operating on recordMap directly.
    // ---------------------------------------------------------------------------
    public static final class PathRecord {
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
            return path.hashCode();
        }

        public Node getNode() {
            return node;
        }
    }

    // Single map replacing the former pair (map + hashMap).
    private Map<FXNode, PathRecord> recordMap;

    // Lazy Map<FXNode, List<Node>> view of recordMap for external callers.
    // Rebuilt from recordMap when null; invalidated by dirty().
    //private Map<FXNode, List<Node>> cachedPathMap = null;

    private Map<Node, Set<FXNode>> nodesMap;

    private Set<FXNode> cursor;
    private boolean unresolvable = false;
    private int cachedHash = 0;
    private boolean hashDirty = true;
    private PathAccessor accessor;

    Matching(FXNode cursor, List<Node> nodePath, PathAccessor accessor) {
        if (cursor == null) throw new RuntimeException("cursor is null");
        if (!cursor.isRoot()) throw new RuntimeException("cursor is not root");
        this.cursor = new HashSet<>();
        this.recordMap = new HashMap<>();
        this.accessor = accessor;
        populate(cursor);
        this.set(cursor, nodePath);
    }

    private Matching(Map<FXNode, PathRecord> recordMap, Set<FXNode> cursor,
                     PathAccessor accessor, Map<Node, Set<FXNode>> nodesMap) {
        this.recordMap = recordMap;
        this.cursor = cursor;
        this.accessor = accessor;
        this.nodesMap = nodesMap;
    }

    private void populate(FXNode cursor) {
        if (cursor.isRoot()) {
            nodesMap = new HashMap<>();
        }
        if (!nodesMap.containsKey(cursor.getNode())) {
            nodesMap.put(cursor.getNode(), new HashSet<>());
        }
        nodesMap.get(cursor.getNode()).add(cursor);
        for (FXNode entry : cursor.getChildren()) {
            populate(entry);
        }
    }

    // -----------------------------------------------------------------------
    // External map view — lazily built, invalidated by dirty()
    // -----------------------------------------------------------------------

//    public Map<FXNode, List<Node>> getMap() {
//        if (cachedPathMap == null) {
//            Map<FXNode, List<Node>> m = new HashMap<>(recordMap.size());
//            for (Map.Entry<FXNode, PathRecord> e : recordMap.entrySet()) {
//                m.put(e.getKey(), e.getValue().path);
//            }
//            cachedPathMap = Collections.unmodifiableMap(m);
//        }
//        return cachedPathMap;
//    }

    public Map<FXNode, PathRecord> getMatches() {
//        if (cachedMatches == null) {
//            Map<FXNode, Node> m = new HashMap<>();
//            for (Map.Entry<FXNode, PathRecord> e : recordMap.entrySet()) {
//                List<Node> vals = e.getValue().path;
//                m.put(e.getKey(), vals.get(vals.size() - 1));
//            }
//            cachedMatches = Collections.unmodifiableMap(m);
//        }
        return recordMap;
    }

    public Set<FXNode> getCursor() {
        return Collections.unmodifiableSet(cursor);
    }

    public boolean contains(FXNode patternNode) {
        return recordMap.containsKey(patternNode);
    }

    public boolean isEmpty() {
        return recordMap.isEmpty();
    }

    public int size() {
        return recordMap.size();
    }

    // -----------------------------------------------------------------------
    // set / removeFromMap
    // -----------------------------------------------------------------------

    /** Public API: computes hash by iterating valuePath. */
    public void set(FXNode patternNode, List<Node> valuePath) {
        long h = 0L;
        for (Node n : valuePath) { h = h * HASH_PRIME + n.hashCode(); }
        setRecord(patternNode, valuePath, h);
    }

    /** Internal fast path: hash already known from the accessor. */
    private void setRecord(FXNode patternNode, List<Node> path, long hash) {
        //dirty();
        recordMap.put(patternNode, new PathRecord(path, hash));
        if (!patternNode.isRoot()) {
            if (isContainer(patternNode)) {
                this.cursor.remove(patternNode.getParent());
                this.cursor.add(patternNode);
            } else if (isValueOrTypeOrRoot(patternNode)) {
                this.cursor.remove(patternNode.getParent());
                this.cursor.add(patternNode.getParent().getParent());
            } else if (isPredicate(patternNode)) {
                this.cursor.remove(patternNode.getParent());
                this.cursor.add(patternNode);
            }
        } else {
            this.cursor.add(patternNode);
        }
    }

    private void removeFromMap(FXNode patternNode) {
        //dirty();
        if (patternNode.getNode().isVariable()) {
            for (FXNode k : nodesMap.get(patternNode.getNode())) {
                recordMap.remove(k);
            }
        } else {
            recordMap.remove(patternNode);
        }
    }

    // -----------------------------------------------------------------------
    // Type helpers — two field reads, no map lookup
    // -----------------------------------------------------------------------

    public boolean isValueOrTypeOrRoot(FXNode patternNode) {
        FX t = patternNode.getAnnotation().getTerm();
        return t == FX.Type || t == FX.Value || t == FX.Root;
    }

    public boolean isContainer(FXNode patternNode) {
        return patternNode.getAnnotation().getTerm() == FX.Container;
    }

    public boolean isPredicate(FXNode patternNode) {
        FX t = patternNode.getAnnotation().getTerm();
        return t == FX.SlotNumber || t == FX.SlotString || t == FX.TypeProperty;
    }

    public boolean isValueOrTypeOrRoot(FX t) {
        return t == FX.Type || t == FX.Value || t == FX.Root;
    }

    public boolean isContainer(FX t) {
        return t == FX.Container;
    }

    public boolean isPredicate(FX t) {
        return t == FX.SlotNumber || t == FX.SlotString || t == FX.TypeProperty;
    }

    // -----------------------------------------------------------------------
    // copy
    // -----------------------------------------------------------------------

    public Matching copy() {
        return new Matching(new HashMap<>(recordMap), new HashSet<>(cursor), accessor, nodesMap);
    }

    // -----------------------------------------------------------------------
    // unset
    // -----------------------------------------------------------------------

    private void unset(FXNode patternNode) {
        removeFromMap(patternNode);
        if (isContainer(patternNode)) {
            removeFromMap(patternNode.getParent());
            this.cursor.remove(patternNode);
            this.cursor.add(patternNode.getParent().getParent());
        } else if (isValueOrTypeOrRoot(patternNode)) {
            removeFromMap(patternNode);
            this.cursor.remove(patternNode);
            removeFromMap(patternNode.getParent());
            this.cursor.add(patternNode.getParent().getParent());
        } else if (isPredicate(patternNode)) {
            this.cursor.remove(patternNode);
            this.cursor.add(patternNode.getParent());
        }
    }

    // -----------------------------------------------------------------------
    // check
    // -----------------------------------------------------------------------

    public Set<Matching> check(Node node, FX component, long prefixHash) {
        int expectedDepth = accessor.currentDepth() - 1;
        Set<FXNode> matched = null;

        for (FXNode c : cursor) {
            PathRecord record = recordMap.get(c);
            if (record == null || record.path.size() != expectedDepth) continue;
            if (record.hash != prefixHash) continue;
            for (FXNode newCursor : c.getChildren()) {
                if (newCursor.getAnnotation().getTerm() == component &&
                        nodeMatches(newCursor.getNode(), node)) {
                    if (matched == null) matched = new HashSet<>();
                    matched.add(newCursor);
                }
            }
        }

        // No match following this path
        if (matched == null) {
            FXNode firstCursor = cursor.iterator().next();
            if (cursor.size() == 1 && firstCursor.getAnnotation().getTerm() == FX.Container) {
                // single container cursor — stay put, do nothing
            } else if (isPredicate(firstCursor.getAnnotation().getTerm())) {
                // Restore container: predicate value didn't match
                for (FXNode old : cursor) { recordMap.remove(old); }
                cursor = new HashSet<>();
                cursor.add(firstCursor.getParent());
            } else {
                this.unresolvable = true;
            }
            return Collections.emptySet();
        }

        Set<Matching> spawned = null;

        if (matched.size() == 1) {
            if (cursor.size() != 1) {
                this.unresolvable = true;
            } else {
                spawned = setAndSpawn(matched);
            }
        } else {
            // matched.size() > 1 — spawn all combinations
            Map<Node, Set<FXNode>> dn = new HashMap<>();
            for (FXNode c : matched) {
                if (!dn.containsKey(c.getNode())) dn.put(c.getNode(), new HashSet<>());
                dn.get(c.getNode()).add(c);
            }
            List<List> input = new ArrayList<>();
            for (Set<FXNode> nn : dn.values()) { input.add(new ArrayList<>(nn)); }
            List subsets = FXTreeUtils.subsets(input);
            List<Node> currentPath = accessor.copyCurrentPath();
            long currentHash = accessor.currentFullHash();
            for (Object s : subsets) {
                List sList = (List) s;
                if (sList.isEmpty()) continue;
                Matching m = copy();
                for (Object sn : sList) {
                    for (FXNode c : (List<FXNode>) sn) {
                        m.setRecord(c, currentPath, currentHash);
                    }
                }
                if (spawned == null) spawned = new HashSet<>();
                spawned.add(m);
            }
            this.unresolvable = true;
        }

        // Check if matching is partial: all cursors must be the same type
        if (cursor.size() > 1) {
            for (FXNode c1 : cursor) {
                for (FXNode c2 : cursor) {
                    if (c1.getAnnotation().getTerm() != c2.getAnnotation().getTerm()) {
                        this.unresolvable = true;
                    }
                }
            }
        }

        // Check orphan variables
        if (!unresolvable && recordMap.size() > 1) {
            for (Map.Entry<FXNode, PathRecord> entry : recordMap.entrySet()) {
                Node matchingNode = entry.getKey().getNode();
                if (matchingNode.isConcrete()) continue;
                long entryHash = entry.getValue().hash;
                for (FXNode fxn : nodesMap.get(matchingNode)) {
                    PathRecord fxnRecord = recordMap.get(fxn);
                    if (fxnRecord == null || fxnRecord.hash != entryHash) {
                        this.unresolvable = true;
                        break;
                    }
                }
                if (this.unresolvable) break;
            }
        }

        if (L.isDebugEnabled()) logSpawned(spawned);
        return spawned != null ? spawned : Collections.emptySet();
    }

    // -----------------------------------------------------------------------
    // spawn / setAndSpawn
    // -----------------------------------------------------------------------

    private Set<Matching> spawn(Set<FXNode> matched) {
        Set<Matching> spawned = null;
        for (FXNode match : matched) {
            FX component = match.getAnnotation().getTerm();
            if (isContainer(component) || isValueOrTypeOrRoot(component)) {
                Matching m = copy();
                m.unset(match);
                if (spawned == null) spawned = new HashSet<>();
                spawned.add(m);
            }
            // predicates: nothing to do
        }
        return spawned;
    }

    private Set<Matching> setAndSpawn(Set<FXNode> matched) {
        List<Node> currentPath = accessor.copyCurrentPath();
        long currentHash = accessor.currentFullHash();
        for (FXNode c : matched) {
            setRecord(c, currentPath, currentHash);
        }
        Set<Matching> result = spawn(matched);
        return result != null ? result : Collections.emptySet();
    }

    // -----------------------------------------------------------------------
    // endContainer
    // -----------------------------------------------------------------------

    public void endContainer() {
        long fullHash = accessor.currentFullHash();
        boolean containerInCursor = false;
        for (FXNode c : cursor) {
            PathRecord record = recordMap.get(c);
            if (record != null && record.hash == fullHash) {
                containerInCursor = true;
                break;
            }

            // If a container ends but a cursor has a property waiting for value
            // remove it
            if (isPredicate(c.getAnnotation().getTerm())) {
                // Remove matchings with grieving parent predicates
                this.unresolvable = true;
                break;
            }
        }

        if (!this.unresolvable && containerInCursor) {
            Set<FXNode> unset = cursor;
            cursor = new HashSet<>();
            for (FXNode c : unset) {
                if (c.isRoot()) {
                    this.unresolvable = true;
                    break;
                }
                cursor.add(c.getParent().getParent());
            }
            if (!unresolvable) checkOrphans(unset);
        }
    }

    private void checkOrphans(Set<FXNode> unset) {
        for (FXNode c : unset) {
            for (FXNode c2 : c.getChildren()) {
                if (!recordMap.containsKey(c2)) {
                    this.unresolvable = true;
                    break;
                }
            }
            if (this.unresolvable) break;
        }
    }

    // -----------------------------------------------------------------------
    // Misc
    // -----------------------------------------------------------------------

    public boolean isUnresolvable() {
        return unresolvable;
    }

    public static boolean nodeMatches(Node patternNode, Node dataNode) {
        if (patternNode.isBlank()) return true;
        if (patternNode.isVariable()) return true;
        if (patternNode.isURI() && dataNode.isURI()) return patternNode.equals(dataNode);
        return patternNode.equals(dataNode);
    }

    @Override
    public int hashCode() {
        if (hashDirty) {
            this.cachedHash = recordMap.hashCode();
            this.hashDirty = false;
        }
        return this.cachedHash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Matching) {
            return this.recordMap.equals(((Matching) obj).recordMap);
        }
        return false;
    }

    private void logSpawned(Set<Matching> spawned) {
        if (spawned == null || spawned.isEmpty()) return;
        StringBuilder sb = new StringBuilder();
        for (Matching m : spawned) sb.append(m.hashCode()).append(" ");
        L.debug("  ... {} generates {}.", this.hashCode(), sb.toString());
    }
}
