package io.github.sparqlanything.fxbgp.stream;

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
}
