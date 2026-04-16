package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.fxbgp.FX;
import org.apache.jena.graph.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class Matching {
    private static final Logger L = LoggerFactory.getLogger(Matching.class);
    private static final long HASH_PRIME = 1_000_000_007L;

    // Single map replacing the former pair (map + hashMap).
    private Map<FXNode, PathRecord> recordMap;
    private Map<Node, Set<FXNode>> nodesMap;

    private FXNode[] cursorArray;
    private int cursorCount = 0;
    private boolean unresolvable = false;
    private int cachedHash = 0;
    private boolean hashDirty = true;
    private PathAccessor accessor;
    private int maxCursorSize = 0;

    Matching(FXNode cursor, List<Node> nodePath, PathAccessor accessor) {
        if (cursor == null) throw new RuntimeException("cursor is null");
        if (!cursor.isRoot()) throw new RuntimeException("cursor is not root");
        this.recordMap = new HashMap<>();
        this.accessor = accessor;
        populate(cursor);
        this.cursorArray = new FXNode[Math.max(1, maxCursorSize)];
        this.set(cursor, nodePath);
    }

    private Matching(Map<FXNode, PathRecord> recordMap, FXNode[] cursorArray, int cursorCount,
                     PathAccessor accessor, Map<Node, Set<FXNode>> nodesMap, int maxCursorSize) {
        this.recordMap = recordMap;
        this.cursorArray = cursorArray;
        this.cursorCount = cursorCount;
        this.accessor = accessor;
        this.nodesMap = nodesMap;
        this.maxCursorSize = maxCursorSize;
    }

    private void populate(FXNode cursor) {
        if (cursor.isRoot()) {
            nodesMap = new HashMap<>();
        }
        maxCursorSize = Math.max(maxCursorSize, cursor.getChildren().size());
        if (!nodesMap.containsKey(cursor.getNode())) {
            nodesMap.put(cursor.getNode(), new HashSet<>());
        }
        nodesMap.get(cursor.getNode()).add(cursor);
        for (FXNode entry : cursor.getChildren()) {
            populate(entry);
        }
    }

    public Map<FXNode, PathRecord> getMatches() {
        return recordMap;
    }

    public FXNode[] getCursor() {
        return cursorArray;
    }

    // -----------------------------------------------------------------------
    // cursor helpers — fixed-size array; nulls mark empty slots
    // -----------------------------------------------------------------------
    private void cursorAdd(FXNode node) {
        int firstNull = -1;
        for (int i = 0; i < cursorArray.length; i++) {
            if (cursorArray[i] == node) return;          // already present: HashSet semantics
            if (cursorArray[i] == null && firstNull < 0) firstNull = i;
        }
        if (firstNull >= 0) {
            cursorArray[firstNull] = node;
            cursorCount++;
        }
    }

    private void cursorRemove(FXNode node) {
        for (int i = 0; i < cursorArray.length; i++) {
            if (cursorArray[i] == node) {
                cursorArray[i] = null;
                cursorCount--;
                return;
            }
        }
    }

    private void cursorClear() {
        Arrays.fill(cursorArray, null);
        cursorCount = 0;
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
        hashDirty = true;
        recordMap.put(patternNode, new PathRecord(path, hash));
        if (!patternNode.isRoot()) {
            if (isContainer(patternNode)) {
                cursorRemove(patternNode.getParent());
                cursorAdd(patternNode);
            } else if (isValueOrTypeOrRoot(patternNode)) {
                cursorClear(); //cursorRemove(patternNode.getParent());
                cursorAdd(patternNode.getParent().getParent());
            } else if (isPredicate(patternNode)) {
                cursorRemove(patternNode.getParent());
                cursorAdd(patternNode);
            }
        } else {
            cursorAdd(patternNode);
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
        return new Matching(new HashMap<>(recordMap), cursorArray.clone(), cursorCount, accessor, nodesMap, maxCursorSize);
    }

    // -----------------------------------------------------------------------
    // unset
    // -----------------------------------------------------------------------

    private void unset(FXNode patternNode) {
        removeFromMap(patternNode);
        if (isContainer(patternNode)) {
            removeFromMap(patternNode.getParent());
            cursorRemove(patternNode);
            cursorAdd(patternNode.getParent().getParent());
        } else if (isValueOrTypeOrRoot(patternNode)) {
            removeFromMap(patternNode);
            cursorRemove(patternNode);
            removeFromMap(patternNode.getParent());
            cursorAdd(patternNode.getParent().getParent());
        } else if (isPredicate(patternNode)) {
            cursorRemove(patternNode);
            cursorAdd(patternNode.getParent());
        }
    }

    // -----------------------------------------------------------------------
    // check
    // -----------------------------------------------------------------------
    public Set<Matching> check(Node node, FX component, long prefixHash) {
        int expectedDepth = accessor.currentDepth() - 1;
        Set<FXNode> matched = null;

        for (int i = 0; i < cursorArray.length; i++) {
            FXNode c = cursorArray[i];
            if (c == null) continue;
            PathRecord record = recordMap.get(c);
            if (record == null || record.path.size() != expectedDepth) continue;
            if (record.hash != prefixHash) continue;
            for (FXNode newCursor : c.getChildren()) {
                if(recordMap.containsKey(newCursor)) {continue;}
                if (newCursor.getAnnotation().getTerm() == component &&
                        nodeMatches(newCursor.getNode(), node) ) {
                    if (matched == null) matched = new HashSet<>();
                    matched.add(newCursor);
                }
            }
        }

        // No match following this path
        if (matched == null) {
            noMatchOnPath();
            return Collections.emptySet();
        }

        Set<Matching> spawned = null;

        if (matched.size() == 1) {
            if (cursorCount != 1) {
                this.unresolvable = true;
            } else if(component == FX.Root || matched.iterator().next().getNode().isConcrete()) {
                for(FXNode c : matched) {
                    List<Node> currentPath = accessor.copyCurrentPath();
                    long currentHash = accessor.currentFullHash();
                    setRecord(c, currentPath, currentHash);
                }
            }else {
                spawned = setAndSpawn(matched);
            }
        } else if(component == FX.Root){
                // If component is root, set all matches and do not spawn (there will be no more roots...)
                for(FXNode c : matched) {
                    List<Node> currentPath = accessor.copyCurrentPath();
                    long currentHash = accessor.currentFullHash();
                    setRecord(c, currentPath, currentHash);
                }
                return Collections.emptySet();
        }else{

            // matched.size() > 1 — spawn all combinations
            // TODO Optimise according to FX model expectations on Slots, Type, and Root
            // Optimisation on terms
            // 1. If the match is Root, we don't want to allow any more partial matches without it
            // 2. If the match is Slot
            // 2.1 String
            // 2.2 Number
            // 3. If the match is Type and concrete in the BGP, we don't want to allow any more partial matches without it (only 1 type)

            // Filter out fx node types we don't want to reset
            Map<Object, Set<FXNode>> dn = new HashMap<>();
            int splitType = 0;
            Set<FXNode> keep = new HashSet<>();
            for (FXNode c : matched) {
                Object index;
                if(component != FX.TypeProperty && c.getNode().isConcrete()) {
                    // Always keep
                    keep.add(c);
                    index = c.getNode();
                }else if(component == FX.TypeProperty) {
                    // Keep both separate
                    index = splitType;
                    splitType++;
                }else {
                    index = c.getNode();
                }
                if (!dn.containsKey(c.getNode())) dn.put(index, new HashSet<>());
                dn.get(index).add(c);
            }
            List<List> input = new ArrayList<>();
            for (Set<FXNode> nn : dn.values()) { nn.addAll(keep); input.add(new ArrayList<>(nn)); }
            List subsets = FXTreeUtils.subsets(input);
            List<Node> currentPath = accessor.copyCurrentPath();
            long currentHash = accessor.currentFullHash();
            for (Object s : subsets) {
                List sList = (List) s;
                if (sList.isEmpty()) continue;
                Matching m = copy();
                m.cursorClear();
                Set<FXNode> added = new HashSet<>();
                for (Object sn : sList) {
                    for (FXNode c : (List<FXNode>) sn) {
                        m.setRecord(c, currentPath, currentHash);
                        added.add(c);
                    }
                }
                // If component is root or type, remove orphans TypeProperty
                if(component == FX.Type  || component == FX.Value) {
                    for(FXNode ma: matched) {
                        if(!added.contains(ma))
                            m.removeFromMap(ma.getParent());
                    }
                }
                if (spawned == null) spawned = new HashSet<>();
                spawned.add(m);
            }
            this.unresolvable = true;
        }

        // Check if matching is partial: all cursors must be the same type
        if (cursorCount > 1) {
            FX first = null;
            for (int i = 0; i < cursorArray.length; i++) {
                if (cursorArray[i] == null) continue;
                FX term = cursorArray[i].getAnnotation().getTerm();
                if (first == null) { first = term; continue; }
                if (term != first) { this.unresolvable = true; break; }
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

    public void noMatchOnPath() {
        FXNode firstCursor = null;
        for (int i = 0; i < cursorArray.length; i++) {
            if (cursorArray[i] != null) { firstCursor = cursorArray[i]; break; }
        }

        if (cursorCount == 1 && firstCursor.getAnnotation().getTerm() == FX.Container) {
            // single container cursor — stay put, do nothing
        } else if (isPredicate(firstCursor.getAnnotation().getTerm())) {
            // If the cursor is a concrete predicate, the matching is unsatisfiable
            for(FXNode cursor : cursorArray) {
                if(cursor != null && cursor.getNode().isConcrete() && firstCursor.getAnnotation().getTerm() != FX.TypeProperty) {
                    this.unresolvable = true;
                    return;
                }
            }
            // Restore container: predicate value didn't match
            for (int i = 0; i < cursorArray.length; i++) {
                if (cursorArray[i] != null) recordMap.remove(cursorArray[i]);
            }
            cursorClear();
            cursorAdd(firstCursor.getParent());
        } else {
            this.unresolvable = true;
        }
    }

    // -----------------------------------------------------------------------
    // spawn / setAndSpawn
    // -----------------------------------------------------------------------

    private Set<Matching> spawn(Set<FXNode> matched) {
        // TODO Optimise according to FX model expectations on Slots, Type, and Root
        // Optimisation on terms
        // 1. If the match is Root, we don't want to allow any more partial matches without it
        // 2. If the match is Slot
        // 2.1 String (nothing to do here)
        // 2.2 Number (nothing to do here)
        // 3. If the match is Type and concrete in the BGP, we don't want to allow any more partial matches without it (only 1 type)
        Set<Matching> spawned = null;
        for (FXNode match : matched) {
            FX component = match.getAnnotation().getTerm();
            // if(component == FX.Container || isValueOrTypeOrRoot(component)) {
            if(component == FX.Container || component == FX.Value) { // Implements 1 and 3
                // Do not unset if predicate parent is concrete
                if(match.getParent() != null && match.getParent().getNode().isConcrete()){
                    continue;
                }
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
        for (int i = 0; i < cursorArray.length; i++) {
            FXNode c = cursorArray[i];
            if (c == null) continue;
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
            // Snapshot old cursor, clear, rebuild at parent.getParent()
            FXNode[] old = cursorArray.clone();
            cursorClear();
            for (int i = 0; i < old.length; i++) {
                FXNode c = old[i];
                if (c == null) continue;
                if (c.isRoot()) {
                    this.unresolvable = true;
                    break;
                }
                cursorAdd(c.getParent().getParent());
            }
            if (!unresolvable) checkOrphans(old);
        }
    }

    private void checkOrphans(FXNode[] unset) {
        for (int i = 0; i < unset.length; i++) {
            FXNode c = unset[i];
            if (c == null) continue;
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
