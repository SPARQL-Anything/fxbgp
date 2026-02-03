package io.github.sparqlanything.fxbgp.stream;

import java.util.ArrayList;
import java.util.List;

public class FXTreeUtils {

    private static void asStringTree(FXNode node, StringBuilder sb){
        sb.append(node.toString());
        if(node.getChildren().size() > 0){
            sb.append(" { ");
            for(FXNode c: node.getChildren()){
                asStringTree(c, sb);
                sb.append(" ");
            }
            sb.append(" } ");
        }
    }

    public static String asTree(FXNode node){
        StringBuilder sb = new StringBuilder();
        asStringTree(node, sb);
        return sb.toString();
    }

    public static <T> List<List<T>> subsets(List<T> in) {
        List<List<T>> out = new ArrayList<>();
        out.add(new ArrayList<>());
        for (T elem : in) {
            int size = out.size();
            for (int i = 0; i < size; i++) {
                List<T> newSubset = new ArrayList<>(out.get(i));
                newSubset.add(elem);
                out.add(newSubset);
            }
        }
        return out;
    }
}
