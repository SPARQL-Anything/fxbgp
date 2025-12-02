package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.fxbgp.AnalyserGrounder;
import io.github.sparqlanything.fxbgp.AnalyserTest;
import io.github.sparqlanything.fxbgp.BGPTestAbstract;
import io.github.sparqlanything.fxbgp.FXBGPAnnotation;
import io.github.sparqlanything.fxbgp.FXModel;
import org.h2.util.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class FXTreePatternTest extends BGPTestAbstract {

    final protected static Logger L = LoggerFactory.getLogger(AnalyserTest.class);

    @Rule
    public TestName name = new TestName();

    private Set<FXTreePattern> patterns;

    @Override
    public void before() {
        super.before();
        setAnalyser(new AnalyserGrounder(properties, FXM()));
    }

    public FXTreePatternTest() {
        super(FXModel.getFXModel());

    }

    @Test
    public void TPT1() throws IOException {
        loadBGP(name.getMethodName());
        patterns = buildPatterns(annotations());
        printTrees();
    }

    @Test
    public void TPT2() throws IOException, NotATreeException {
        loadBGP(name.getMethodName());
        patterns = buildPatterns(annotations());
        printTrees();
    }

    @Test
    public void TPT3() throws IOException, NotATreeException {
        loadBGP(name.getMethodName());
        patterns = buildPatterns(annotations());
        printTrees();
    }

    private void printTrees() {
        int c = 0;
        for (FXTreePattern p: patterns) {
            StringBuilder sb = new StringBuilder();
            printTree(c++, sb, p.getRoot());
            System.out.println(sb.toString());
        }
    }

    private void printTree(int c, StringBuilder sb, FXNode focus) {
        if(focus.isRoot()){
            L.info("Printing tree {}", c);
        }

        int depth = 0;
        FXNode r = focus;
        while(!r.isRoot()) {
            depth++;
            r = r.getParent();
        }

        if(sb.length() > 0) {
            sb.append("\n");
            sb.append(StringUtils.pad("", depth,"-", false));
        }
        sb.append(focus.getNode());
        sb.append(" [");
        sb.append(focus.getAnnotation());
        sb.append("]");
        for(FXNode ch: focus.getChildren()){
            printTree(c, sb, ch);
        }
    }

    private Set<FXTreePattern> buildPatterns(Set<FXBGPAnnotation> annotations) {
        Set<FXTreePattern> patterns = new HashSet<>();
        for(FXBGPAnnotation annotation : annotations) {
            try {
                patterns.add(FXTreePattern.make(annotation));
            } catch (NotATreeException e) {
                throw new RuntimeException(e);
            }
        }
        return Collections.unmodifiableSet(patterns);
    }
}
