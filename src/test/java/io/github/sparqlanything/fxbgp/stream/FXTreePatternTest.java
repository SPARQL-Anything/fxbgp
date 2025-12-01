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
    public void TPT1() throws IOException, NotAStarException {
        readBGP(name.getMethodName());
        patterns = buildPatterns(annotations());
        printTrees();
    }

    @Test
    public void TPT2() throws IOException, NotAStarException {
        readBGP(name.getMethodName());
        patterns = buildPatterns(annotations());
        printTrees();
    }

    private void printTrees() throws NotAStarException {
        for (FXTreePattern p: patterns) {
            StringBuilder sb = new StringBuilder();
            printTree(sb, p.getRoot());
            System.out.println(sb.toString());
        }
    }

    private void printTree(StringBuilder sb, FXNode focus) {
        int depth = 0;
        FXNode node = focus;
        while(!node.isRoot()) {
            depth++;
            node = node.getParent();
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
            printTree(sb, ch);
        }
    }

    private Set<FXTreePattern> buildPatterns(Set<FXBGPAnnotation> annotations) throws NotAStarException {
        Set<FXTreePattern> patterns = new HashSet<>();
        for(FXBGPAnnotation annotation : annotations) {
            patterns.add(FXTreePattern.make(annotation));
        }
        return Collections.unmodifiableSet(patterns);
    }
}
