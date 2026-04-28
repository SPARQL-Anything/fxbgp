package io.github.sparqlanything.fxbgp.stream.performance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JSONGenerator {

    public static void generateJSON(int height, int branchingFactor, List<List<String>> leafContainers, String filename) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Random r = new Random(System.currentTimeMillis());
        int slots = 0;

        ArrayNode root = mapper.createArrayNode();
        List<JsonNode> nextLevel = new ArrayList<>();
        nextLevel.add(root);

        for (int lev = 0; lev < height - 1; lev++) {
            List<JsonNode> children = new ArrayList<>();
            for (JsonNode n : nextLevel) {
                if (n instanceof ArrayNode currentNode) {
                    // next level will be ObjectNodes
                    for (int childNumber = 0; childNumber < branchingFactor; childNumber++) {
                        ObjectNode child = mapper.createObjectNode();
                        currentNode.add(child);
                        children.add(child);
                        slots++;
                    }
                } else if (n instanceof ObjectNode currentNode) {
                    // next level will be Array
                    for (int childNumber = 0; childNumber < branchingFactor; childNumber++) {

                        ArrayNode child = currentNode.putArray("f".concat(String.valueOf(childNumber)));
                        children.add(child);
                        slots++;
                    }
                }
            }
            nextLevel = children;
        }

        // last level
        int leafContainer = 0;

        for (JsonNode n : nextLevel) {
            List<String> container = leafContainers.get(leafContainer % leafContainers.size());
            leafContainer++;
            if (n instanceof ArrayNode currentNode) {
                // next level will be ObjectNodes
                for (String s : container) {
                    currentNode.add(s);
                    slots++;
                }
            } else if (n instanceof ObjectNode currentNode) {
                // next level will be Array
                for (int i = 0; i < container.size(); i++) {
                    currentNode.put("f".concat(String.valueOf(i)), container.get(i));
                    slots++;
                }
            }
        }
        mapper.writeValue(new File(filename), root);
        // System.out.println(slots);
    }
}
